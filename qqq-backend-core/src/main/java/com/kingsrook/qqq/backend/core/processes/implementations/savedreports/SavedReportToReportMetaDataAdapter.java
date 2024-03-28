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

package com.kingsrook.qqq.backend.core.processes.implementations.savedreports;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableDefinition;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportField;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.ReportType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class SavedReportToReportMetaDataAdapter
{
   private static final QLogger LOG = QLogger.getLogger(SavedReportToReportMetaDataAdapter.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public QReportMetaData adapt(SavedReport savedReport) throws QException
   {
      try
      {
         QReportMetaData reportMetaData = new QReportMetaData();
         reportMetaData.setLabel(savedReport.getLabel());

         ////////////////////////////
         // set up the data-source //
         ////////////////////////////
         QReportDataSource dataSource = new QReportDataSource();
         reportMetaData.setDataSources(List.of(dataSource));
         dataSource.setName("main");

         QTableMetaData table = QContext.getQInstance().getTable(savedReport.getTableName());
         dataSource.setSourceTable(savedReport.getTableName());

         dataSource.setQueryFilter(JsonUtils.toObject(savedReport.getQueryFilterJson(), QQueryFilter.class));

         // todo!!! oh my.
         List<QueryJoin> queryJoins = null;
         dataSource.setQueryJoins(queryJoins);

         //////////////////////////
         // set up the main view //
         //////////////////////////
         QReportView view = new QReportView();
         reportMetaData.setViews(ListBuilder.of(view));
         view.setName("main");
         view.setType(ReportType.TABLE);
         view.setDataSourceName(dataSource.getName());
         view.setLabel(savedReport.getLabel()); // todo eh?
         view.setIncludeHeaderRow(true);

         // don't need:
         // view.setOrderByFields(); - only used for summary reports
         // view.setTitleFormat(); - not using at this time
         // view.setTitleFields(); - not using at this time
         // view.setRecordTransformStep();
         // view.setViewCustomizer();

         ///////////////////////////////////////////////////////////////////////////////////////////////////////////
         // columns in the saved-report look like a JSON object, w/ a key "columns", which is an array of objects //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////
         Map<String, Object>       columnsObject = JsonUtils.toObject(savedReport.getColumnsJson(), new TypeReference<>() {});
         List<Map<String, Object>> columns       = (List<Map<String, Object>>) columnsObject.get("columns");
         List<QReportField>        reportColumns = new ArrayList<>();

         for(Map<String, Object> column : columns)
         {
            if(column.containsKey("isVisible") && !"true".equals(ValueUtils.getValueAsString(column.get("isVisible"))))
            {
               continue;
            }

            QFieldMetaData field = null;
            String fieldName = ValueUtils.getValueAsString(column.get("name"));
            if(fieldName.contains("."))
            {
               // todo - join!
            }
            else
            {
               field = table.getFields().get(fieldName);
               if(field == null)
               {
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // frontend may often pass __checked__ (or maybe other __ prefixes in the future - so - don't warn that. //
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                  if(!fieldName.startsWith("__"))
                  {
                     LOG.warn("Saved Report has an unexpected unrecognized field name", logPair("savedReportId", savedReport.getId()), logPair("table", table.getName()), logPair("fieldName", fieldName));
                  }
                  continue;
               }
            }

            QReportField reportField = new QReportField();
            reportColumns.add(reportField);

            reportField.setName(fieldName);
            reportField.setLabel(field.getLabel());

            if(StringUtils.hasContent(field.getPossibleValueSourceName()))
            {
               reportField.setShowPossibleValueLabel(true);
            }
         }

         view.setColumns(reportColumns);

         ///////////////////////////////////////////////
         // if it's a pivot report, add that view too //
         ///////////////////////////////////////////////
         if(StringUtils.hasContent(savedReport.getPivotTableJson()))
         {
            QReportView pivotView = new QReportView();
            reportMetaData.getViews().add(pivotView);
            pivotView.setName("pivot"); // does this appear?
            pivotView.setType(ReportType.PIVOT);
            pivotView.setPivotTableSourceViewName(view.getName());
            pivotView.setPivotTableDefinition(JsonUtils.toObject(savedReport.getPivotTableJson(), PivotTableDefinition.class));
         }

         /////////////////////////////////////////////////////
         // add input fields, if they're in the savedReport //
         /////////////////////////////////////////////////////
         if(StringUtils.hasContent(savedReport.getInputFieldsJson()))
         {
            ////////////////////////////////////
            // todo turn on when implementing //
            ////////////////////////////////////
            // reportMetaData.setInputFields(JsonUtils.toObject(savedReport.getInputFieldsJson(), new TypeReference<>() {}));
            throw (new IllegalStateException("Input Fields are not yet implemented"));
         }

         return (reportMetaData);
      }
      catch(Exception e)
      {
         LOG.warn("Error adapting savedReport to reportMetaData", e, logPair("savedReportId", savedReport.getId()));
         throw (new QException("Error adapting savedReport to reportMetaData", e));
      }
   }

}
