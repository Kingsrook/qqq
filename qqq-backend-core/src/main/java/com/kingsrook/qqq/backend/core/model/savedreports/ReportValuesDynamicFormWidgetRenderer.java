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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.DynamicFormWidgetData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
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

         if(StringUtils.hasContent(savedReport.getQueryFilterJson()))
         {
            QQueryFilter   queryFilter = SavedReportToReportMetaDataAdapter.getQQueryFilter(savedReport.getQueryFilterJson());
            QTableMetaData table       = QContext.getQInstance().getTable(savedReport.getTableName());

            ///////////////////////////////////////////////////////////////////////////////////////////////
            // find variables in the query filter; convert them to a list of fields for the dynamic form //
            ///////////////////////////////////////////////////////////////////////////////////////////////
            for(QFilterCriteria criteria : CollectionUtils.nonNullList(queryFilter.getCriteria()))
            {
               /////////////////////////////////
               // todo - only variable fields //
               /////////////////////////////////

               ////////////////////////////////
               // todo - twice for "between" //
               ////////////////////////////////

               //////////////////////////
               // todo - join fields!! //
               //////////////////////////
               QFieldMetaData fieldMetaData = table.getField(criteria.getFieldName()).clone();

               /////////////////////////////////
               // make name & label for field //
               /////////////////////////////////
               String operatorHumanish = StringUtils.allCapsToMixedCase(criteria.getOperator().name()); // todo match frontend..?
               String fieldName        = criteria.getFieldName() + operatorHumanish.replaceAll("_", "");
               String label            = fieldMetaData.getLabel() + " " + operatorHumanish.replaceAll("_", " ");
               fieldMetaData.setName(fieldName);
               fieldMetaData.setLabel(label);

               ////////////////////////////////////////////////////////////
               // in this use case, every field is required and editable //
               ////////////////////////////////////////////////////////////
               fieldMetaData.setIsRequired(true);
               fieldMetaData.setIsEditable(true);

               if(defaultValues.containsKey(fieldName))
               {
                  fieldMetaData.setDefaultValue(defaultValues.get(fieldName));
               }

               fieldList.add(fieldMetaData);
            }
         }

         ///////////////////////////////////
         // make output object and return //
         ///////////////////////////////////
         DynamicFormWidgetData widgetData = new DynamicFormWidgetData();
         widgetData.setFieldList(fieldList);
         widgetData.setMergedDynamicFormValuesIntoFieldName("inputValues");

         if(CollectionUtils.nullSafeIsEmpty(fieldList))
         {
            widgetData.setNoFieldsMessage("This Report does not use any Variable Values");
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
