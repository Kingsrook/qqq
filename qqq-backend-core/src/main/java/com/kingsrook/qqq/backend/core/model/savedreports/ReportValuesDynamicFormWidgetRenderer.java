/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.core.model.savedreports;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.FilterVariableExpression;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.DynamicFormWidgetData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAndJoinTable;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.savedreports.SavedReportToReportMetaDataAdapter;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Note - exists under 2 names, for the RenderSavedReport process, and for the
 ** ScheduledReport table
 *******************************************************************************/
public class ReportValuesDynamicFormWidgetRenderer extends AbstractWidgetRenderer
{
   private static final QLogger LOG = QLogger.getLogger(ReportValuesDynamicFormWidgetRenderer.class);

   private QPossibleValueTranslator qPossibleValueTranslator;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      try
      {
         List<QFieldMetaData> fieldList     = new ArrayList<>();
         Map<String, String>  defaultValues = new HashMap<>();

         //////////////////////////////////////////////////////////////////////////////
         // read params to ultimately find the query filter that has variables in it //
         //////////////////////////////////////////////////////////////////////////////
         SavedReport savedReport = null;
         if(input.getQueryParams().containsKey("savedReportId"))
         {
            QRecord record = new GetAction().executeForRecord(new GetInput(SavedReport.TABLE_NAME).withPrimaryKey(ValueUtils.getValueAsInteger(input.getQueryParams().get("savedReportId"))));
            savedReport = new SavedReport(record);
         }
         else if(input.getQueryParams().containsKey("id"))
         {
            QRecord scheduledReportRecord = new GetAction().executeForRecord(new GetInput(ScheduledReport.TABLE_NAME).withPrimaryKey(ValueUtils.getValueAsInteger(input.getQueryParams().get("id"))));
            QRecord record                = new GetAction().executeForRecord(new GetInput(SavedReport.TABLE_NAME).withPrimaryKey(ValueUtils.getValueAsInteger(scheduledReportRecord.getValueInteger("savedReportId"))));
            savedReport = new SavedReport(record);

            String inputValues = scheduledReportRecord.getValueString("inputValues");
            if(StringUtils.hasContent(inputValues))
            {
               JSONObject jsonObject = JsonUtils.toJSONObject(inputValues);
               for(String key : jsonObject.keySet())
               {
                  defaultValues.put(key, jsonObject.optString(key));
               }
            }
         }
         else
         {
            //////////////////////////////////
            // return quietly w/ nothing... //
            //////////////////////////////////
            DynamicFormWidgetData widgetData = new DynamicFormWidgetData();
            return new RenderWidgetOutput(widgetData);
         }

         QRecord recordOfFieldValues = new QRecord();

         if(StringUtils.hasContent(savedReport.getQueryFilterJson()))
         {
            QQueryFilter   queryFilter = SavedReportToReportMetaDataAdapter.getQQueryFilter(savedReport.getQueryFilterJson());
            QTableMetaData table       = QContext.getQInstance().getTable(savedReport.getTableName());

            ///////////////////////////////////////////////////////////////////////////////////////////////
            // find variables in the query filter; convert them to a list of fields for the dynamic form //
            ///////////////////////////////////////////////////////////////////////////////////////////////
            for(QFilterCriteria criteria : CollectionUtils.nonNullList(queryFilter.getCriteria()))
            {
               for(Serializable criteriaValue : CollectionUtils.nonNullList(criteria.getValues()))
               {
                  if(criteriaValue instanceof FilterVariableExpression filterVariableExpression)
                  {
                     FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(table, criteria.getFieldName());
                     QFieldMetaData    fieldMetaData     = fieldAndJoinTable.field().clone();

                     /////////////////////////////////
                     // make name & label for field //
                     /////////////////////////////////
                     String fieldName = filterVariableExpression.getVariableName();
                     fieldMetaData.setName(fieldName);
                     fieldMetaData.setLabel(QInstanceEnricher.nameToLabel(filterVariableExpression.getVariableName()));

                     ////////////////////////////////////////////////////////////
                     // in this use case, every field is required and editable //
                     ////////////////////////////////////////////////////////////
                     fieldMetaData.setIsRequired(true);
                     fieldMetaData.setIsEditable(true);

                     ///////////////////////////////////////////////////////////////////////
                     // if we're in a context where there are values, then populate those //
                     // e.g., a view screen instead of an edit screen, i think            //
                     ///////////////////////////////////////////////////////////////////////
                     if(defaultValues.containsKey(fieldName))
                     {
                        String value = defaultValues.get(fieldName);

                        fieldMetaData.setDefaultValue(value);
                        recordOfFieldValues.setValue(fieldName, value);

                        //////////////////////////////////////////////////////
                        // look up display values for possible value fields //
                        //////////////////////////////////////////////////////
                        if(StringUtils.hasContent(fieldMetaData.getPossibleValueSourceName()))
                        {
                           if(qPossibleValueTranslator == null)
                           {
                              qPossibleValueTranslator = new QPossibleValueTranslator();
                           }
                           String displayValue = qPossibleValueTranslator.translatePossibleValue(fieldMetaData, value);
                           recordOfFieldValues.setDisplayValue(fieldName, displayValue);
                        }
                     }

                     fieldList.add(fieldMetaData);
                  }
               }
            }
         }

         ///////////////////////////////////
         // make output object and return //
         ///////////////////////////////////
         DynamicFormWidgetData widgetData = new DynamicFormWidgetData();
         widgetData.setFieldList(fieldList);
         widgetData.setRecordOfFieldValues(recordOfFieldValues);
         widgetData.setMergedDynamicFormValuesIntoFieldName("inputValues");

         if(CollectionUtils.nullSafeIsEmpty(fieldList))
         {
            ///////////////////////////////////////////////
            // actually don't show this for process mode //
            ///////////////////////////////////////////////
            if(!input.getQueryParams().containsKey("processName"))
            {
               widgetData.setNoFieldsMessage("This Report does not use any Variable Values");
            }
         }

         return new RenderWidgetOutput(widgetData);
      }
      catch(Exception e)
      {
         LOG.warn("Error rendering scheduled report values dynamic form widget", e, logPair("queryParams", String.valueOf(input.getQueryParams())));
         throw (new QException("Error rendering scheduled report values dynamic form widget", e));
      }
   }

}
