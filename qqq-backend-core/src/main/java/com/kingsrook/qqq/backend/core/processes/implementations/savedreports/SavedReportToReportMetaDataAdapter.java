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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableDefinition;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportField;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.ReportType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.savedreports.ReportColumn;
import com.kingsrook.qqq.backend.core.model.savedreports.ReportColumns;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import org.apache.commons.lang.BooleanUtils;
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
         QInstance qInstance = QContext.getQInstance();

         QReportMetaData reportMetaData = new QReportMetaData();
         reportMetaData.setLabel(savedReport.getLabel());

         /////////////////////////////////////////////////////
         // set up the data-source - e.g., table and filter //
         /////////////////////////////////////////////////////
         QReportDataSource dataSource = new QReportDataSource();
         reportMetaData.setDataSources(List.of(dataSource));
         dataSource.setName("main");

         QTableMetaData table = qInstance.getTable(savedReport.getTableName());
         dataSource.setSourceTable(savedReport.getTableName());
         dataSource.setQueryFilter(JsonUtils.toObject(savedReport.getQueryFilterJson(), QQueryFilter.class));

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

         ////////////////////////////////////////////////////////////////////////////////////////////////
         // columns in the saved-report should look  like a serialized version of ReportColumns object //
         // map them to a list of QReportField objects                                                 //
         // also keep track of what joinTables we find that we need to select                          //
         ////////////////////////////////////////////////////////////////////////////////////////////////
         ReportColumns columnsObject = JsonUtils.toObject(savedReport.getColumnsJson(), ReportColumns.class, om -> om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

         List<QReportField> reportColumns = new ArrayList<>();
         view.setColumns(reportColumns);

         Set<String> neededJoinTables = new HashSet<>();

         for(ReportColumn column : columnsObject.getColumns())
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////////
            // if isVisible is missing, we assume it to be true - so only if it isFalse do we skip the column //
            ////////////////////////////////////////////////////////////////////////////////////////////////////
            if(BooleanUtils.isFalse(column.getIsVisible()))
            {
               continue;
            }

            ////////////////////////////////////////////////////
            // figure out the field being named by the column //
            ////////////////////////////////////////////////////
            String         fieldName = ValueUtils.getValueAsString(column.getName());
            QFieldMetaData field     = getField(savedReport, fieldName, qInstance, neededJoinTables, table);
            if(field == null)
            {
               continue;
            }

            //////////////////////////////////////////////////
            // make a QReportField based on the table field //
            //////////////////////////////////////////////////
            QReportField reportField = new QReportField();
            reportColumns.add(reportField);

            reportField.setName(fieldName);
            reportField.setLabel(field.getLabel());

            if(StringUtils.hasContent(field.getPossibleValueSourceName()))
            {
               reportField.setShowPossibleValueLabel(true);
            }
         }

         ///////////////////////////////////////////////////////////////////////////////////////////
         // set up joins, if we need any                                                          //
         // note - test coverage here is provided by RDBMS module's GenerateReportActionRDBMSTest //
         ///////////////////////////////////////////////////////////////////////////////////////////
         if(!neededJoinTables.isEmpty())
         {
            List<QueryJoin> queryJoins = new ArrayList<>();
            dataSource.setQueryJoins(queryJoins);

            for(ExposedJoin exposedJoin : CollectionUtils.nonNullList(table.getExposedJoins()))
            {
               if(neededJoinTables.contains(exposedJoin.getJoinTable()))
               {
                  QueryJoin queryJoin = new QueryJoin(exposedJoin.getJoinTable())
                     .withSelect(true)
                     .withType(QueryJoin.Type.LEFT)
                     .withBaseTableOrAlias(null)
                     .withAlias(null);

                  if(exposedJoin.getJoinPath().size() == 1)
                  {
                     //////////////////////////////////////////////////////////////////////////////////////////////////////////////
                     // Note, this is similar logic (and comment) in QFMD ...                                                    //
                     // todo - what about a join with a longer path?  it would be nice to pass such joinNames through there too, //
                     // but what, that would actually be multiple queryJoins?  needs a fair amount of thought.                   //
                     //////////////////////////////////////////////////////////////////////////////////////////////////////////////
                     queryJoin.setJoinMetaData(qInstance.getJoin(exposedJoin.getJoinPath().get(0)));
                  }

                  queryJoins.add(queryJoin);
               }
            }
         }

         ///////////////////////////////////////////////
         // if it's a pivot report, add that view too //
         ///////////////////////////////////////////////
         if(StringUtils.hasContent(savedReport.getPivotTableJson()))
         {
            QReportView pivotView = new QReportView();
            reportMetaData.getViews().add(pivotView);
            pivotView.setName("pivot");
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



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QFieldMetaData getField(SavedReport savedReport, String fieldName, QInstance qInstance, Set<String> neededJoinTables, QTableMetaData table)
   {
      QFieldMetaData field;
      if(fieldName.contains("."))
      {
         String joinTableName = fieldName.replaceAll("\\..*", "");
         String joinFieldName = fieldName.replaceAll(".*\\.", "");

         QTableMetaData joinTable = qInstance.getTable(joinTableName);
         if(joinTable == null)
         {
            LOG.warn("Saved Report has an unrecognized join table name", logPair("savedReportId", savedReport.getId()), logPair("joinTable", joinTable), logPair("fieldName", fieldName));
            return null;
         }

         neededJoinTables.add(joinTableName);

         field = joinTable.getFields().get(joinFieldName);
         if(field == null)
         {
            LOG.warn("Saved Report has an unrecognized join field name", logPair("savedReportId", savedReport.getId()), logPair("fieldName", fieldName));
            return null;
         }
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
            return null;
         }
      }
      return field;
   }

}
