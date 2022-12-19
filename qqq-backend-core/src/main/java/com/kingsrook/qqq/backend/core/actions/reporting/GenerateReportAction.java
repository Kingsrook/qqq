/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.reporting;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.async.AsyncRecordPipeLoop;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.reporting.customizers.ReportViewCustomizer;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QFormulaException;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportField;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.ReportType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.aggregates.AggregatesInterface;
import com.kingsrook.qqq.backend.core.utils.aggregates.BigDecimalAggregates;
import com.kingsrook.qqq.backend.core.utils.aggregates.IntegerAggregates;


/*******************************************************************************
 ** Action to generate a report.
 **
 ** A report can contain 1 or more Data Sources - e.g., tables + filters that define
 ** data that goes into the report, or simple data-supplier lambdas.
 **
 ** A report can also contain 1 or more Views - e.g., sheets in a spreadsheet workbook.
 ** (how do those work in non-XLSX formats??). Views can either be:
 ** - plain tables,
 ** - summaries (like pivot tables, but called summary to avoid confusion with "native" pivot tables),
 ** - native pivot tables (not initially supported, due to lack of support in fastexcel...).
 *******************************************************************************/
public class GenerateReportAction
{
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // summaryAggregates and varianceAggregates are multi-level maps, ala:                                     //
   // viewName > SummaryKey > fieldName > Aggregates                                                          //
   // e.g.:                                                                                                   //
   // viewName: salesSummaryReport                                                                            //
   // SummaryKey: [(state:MO),(city:St.Louis)]                                                                //
   // fieldName: salePrice                                                                                    //
   // Aggregates: (count:47;sum:10,000;max:2,000;min:15)                                                      //
   // salesSummaryReport > [(state:MO),(city:St.Louis)] > salePrice  > (count:47;sum:10,000;max:2,000;min:15) //
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////
   Map<String, Map<SummaryKey, Map<String, AggregatesInterface<?>>>> summaryAggregates  = new HashMap<>();
   Map<String, Map<SummaryKey, Map<String, AggregatesInterface<?>>>> varianceAggregates = new HashMap<>();

   Map<String, AggregatesInterface<?>> totalAggregates         = new HashMap<>();
   Map<String, AggregatesInterface<?>> varianceTotalAggregates = new HashMap<>();

   private QReportMetaData         report;
   private ReportFormat            reportFormat;
   private ExportStreamerInterface reportStreamer;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void execute(ReportInput reportInput) throws QException
   {
      report = reportInput.getInstance().getReport(reportInput.getReportName());
      reportFormat = reportInput.getReportFormat();
      reportStreamer = reportFormat.newReportStreamer();

      ////////////////////////////////////////////////////////////////////////////////////////////////
      // foreach data source, do a query (possibly more than 1, if it goes to multiple table views) //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      for(QReportDataSource dataSource : report.getDataSources())
      {
         //////////////////////////////////////////////////////////////////////////////
         // make a list of the views that use this data source for various purposes. //
         //////////////////////////////////////////////////////////////////////////////
         List<QReportView> dataSourceTableViews = report.getViews().stream()
            .filter(v -> v.getType().equals(ReportType.TABLE))
            .filter(v -> v.getDataSourceName().equals(dataSource.getName()))
            .toList();

         List<QReportView> dataSourceSummaryViews = report.getViews().stream()
            .filter(v -> v.getType().equals(ReportType.SUMMARY))
            .filter(v -> v.getDataSourceName().equals(dataSource.getName()))
            .toList();

         List<QReportView> dataSourceVariantViews = report.getViews().stream()
            .filter(v -> v.getType().equals(ReportType.SUMMARY))
            .filter(v -> v.getVarianceDataSourceName() != null && v.getVarianceDataSourceName().equals(dataSource.getName()))
            .toList();

         /////////////////////////////////////////////////////////////////////////////////////////////
         // if this data source isn't used for any table views, but it is used for one or           //
         // more summary views (possibly as a variant), then run the query, gathering summary data. //
         /////////////////////////////////////////////////////////////////////////////////////////////
         if(dataSourceTableViews.isEmpty())
         {
            if(!dataSourceSummaryViews.isEmpty() || !dataSourceVariantViews.isEmpty())
            {
               gatherData(reportInput, dataSource, null, dataSourceSummaryViews, dataSourceVariantViews);
            }
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////////////
            // else, foreach table view this data source is used for, run the data source's query //
            ////////////////////////////////////////////////////////////////////////////////////////
            for(QReportView dataSourceTableView : dataSourceTableViews)
            {
               /////////////////////////////////////////////////////////////////////////////////////////
               // if there's a view customizer, run it (e.g., to customize the columns in the report) //
               /////////////////////////////////////////////////////////////////////////////////////////
               if(dataSourceTableView.getViewCustomizer() != null)
               {
                  Function<QReportView, QReportView> viewCustomizerFunction = QCodeLoader.getFunction(dataSourceTableView.getViewCustomizer());
                  if(viewCustomizerFunction instanceof ReportViewCustomizer reportViewCustomizer)
                  {
                     reportViewCustomizer.setReportInput(reportInput);
                  }
                  dataSourceTableView = viewCustomizerFunction.apply(dataSourceTableView.clone());
               }

               ////////////////////////////////////////////////////////////////////////////////////
               // start the table-view (e.g., open this tab in xlsx) and then run the query-loop //
               ////////////////////////////////////////////////////////////////////////////////////
               startTableView(reportInput, dataSource, dataSourceTableView);
               gatherData(reportInput, dataSource, dataSourceTableView, dataSourceSummaryViews, dataSourceVariantViews);
            }
         }
      }

      outputSummaries(reportInput);

      reportStreamer.finish();

      try
      {
         reportInput.getReportOutputStream().close();
      }
      catch(Exception e)
      {
         throw (new QReportingException("Error completing report", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void startTableView(ReportInput reportInput, QReportDataSource dataSource, QReportView reportView) throws QException
   {
      QTableMetaData table = reportInput.getInstance().getTable(dataSource.getSourceTable());

      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", reportInput.getInputValues());

      ExportInput exportInput = new ExportInput(reportInput.getInstance());
      exportInput.setSession(reportInput.getSession());
      exportInput.setReportFormat(reportFormat);
      exportInput.setFilename(reportInput.getFilename());
      exportInput.setTitleRow(getTitle(reportView, variableInterpreter));
      exportInput.setIncludeHeaderRow(reportView.getIncludeHeaderRow());
      exportInput.setReportOutputStream(reportInput.getReportOutputStream());

      JoinsContext joinsContext = new JoinsContext(exportInput.getInstance(), dataSource.getSourceTable(), dataSource.getQueryJoins());

      List<QFieldMetaData> fields;
      if(CollectionUtils.nullSafeHasContents(reportView.getColumns()))
      {
         fields = new ArrayList<>();
         for(QReportField column : reportView.getColumns())
         {
            if(column.getIsVirtual())
            {
               fields.add(column.toField());
            }
            else
            {
               JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(column.getName());
               if(fieldAndTableNameOrAlias.field() == null)
               {
                  throw new QReportingException("Could not find field named [" + column.getName() + "] on table [" + table.getName() + "]");
               }

               QFieldMetaData field = fieldAndTableNameOrAlias.field().clone();
               field.setName(column.getName());
               if(StringUtils.hasContent(column.getLabel()))
               {
                  field.setLabel(column.getLabel());
               }
               fields.add(field);
            }
         }
      }
      else
      {
         fields = new ArrayList<>(table.getFields().values());
      }
      reportStreamer.setDisplayFormats(getDisplayFormatMap(fields));
      reportStreamer.start(exportInput, fields, reportView.getLabel());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void gatherData(ReportInput reportInput, QReportDataSource dataSource, QReportView tableView, List<QReportView> summaryViews, List<QReportView> variantViews) throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////
      // check if this view has a transform step - if so, set it up now and run its pre-run //
      ////////////////////////////////////////////////////////////////////////////////////////
      AbstractTransformStep transformStep       = null;
      RunBackendStepInput   transformStepInput  = null;
      RunBackendStepOutput  transformStepOutput = null;
      if(tableView != null && tableView.getRecordTransformStep() != null)
      {
         transformStep = QCodeLoader.getBackendStep(AbstractTransformStep.class, tableView.getRecordTransformStep());

         transformStepInput = new RunBackendStepInput(reportInput.getInstance());
         transformStepInput.setSession(reportInput.getSession());
         transformStepInput.setValues(reportInput.getInputValues());

         transformStepOutput = new RunBackendStepOutput();

         transformStep.preRun(transformStepInput, transformStepOutput);
      }

      ////////////////////////////////////////////////////////////////////
      // create effectively-final versions of these vars for the lambda //
      ////////////////////////////////////////////////////////////////////
      AbstractTransformStep finalTransformStep       = transformStep;
      RunBackendStepInput   finalTransformStepInput  = transformStepInput;
      RunBackendStepOutput  finalTransformStepOutput = transformStepOutput;

      /////////////////////////////////////////////////////////////////
      // run a record pipe loop, over the query for this data source //
      /////////////////////////////////////////////////////////////////
      RecordPipe recordPipe = new RecordPipe();
      new AsyncRecordPipeLoop().run("Report[" + reportInput.getReportName() + "]", null, recordPipe, (callback) ->
      {
         if(dataSource.getSourceTable() != null)
         {
            QQueryFilter queryFilter = dataSource.getQueryFilter() == null ? new QQueryFilter() : dataSource.getQueryFilter().clone();
            setInputValuesInQueryFilter(reportInput, queryFilter);

            QueryInput queryInput = new QueryInput(reportInput.getInstance());
            queryInput.setSession(reportInput.getSession());
            queryInput.setRecordPipe(recordPipe);
            queryInput.setTableName(dataSource.getSourceTable());
            queryInput.setFilter(queryFilter);
            queryInput.setQueryJoins(dataSource.getQueryJoins());
            queryInput.setShouldTranslatePossibleValues(true); // todo - any limits or conditions on this?
            return (new QueryAction().execute(queryInput));
         }
         else if(dataSource.getStaticDataSupplier() != null)
         {
            @SuppressWarnings("unchecked")
            Supplier<List<List<Serializable>>> supplier = QCodeLoader.getAdHoc(Supplier.class, dataSource.getStaticDataSupplier());
            List<List<Serializable>> lists = supplier.get();
            for(List<Serializable> list : lists)
            {
               QRecord record = new QRecord();
               int     index  = 0;
               for(Serializable value : list)
               {
                  record.setValue("column" + (index++), value);
               }
               recordPipe.addRecord(record);
            }
            return (true);
         }
         else
         {
            throw (new IllegalStateException("Misconfigured data source [" + dataSource.getName() + "]."));
         }
      }, () ->
      {
         List<QRecord> records = recordPipe.consumeAvailableRecords();
         if(finalTransformStep != null)
         {
            finalTransformStepInput.setRecords(records);
            finalTransformStep.run(finalTransformStepInput, finalTransformStepOutput);
            records = finalTransformStepOutput.getRecords();
         }

         return (consumeRecords(reportInput, dataSource, records, tableView, summaryViews, variantViews));
      });

      ////////////////////////////////////////////////
      // if there's a transformer, run its post-run //
      ////////////////////////////////////////////////
      if(transformStep != null)
      {
         transformStep.postRun(transformStepInput, transformStepOutput);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setInputValuesInQueryFilter(ReportInput reportInput, QQueryFilter queryFilter)
   {
      if(queryFilter == null || queryFilter.getCriteria() == null)
      {
         return;
      }

      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", reportInput.getInputValues());
      for(QFilterCriteria criterion : queryFilter.getCriteria())
      {
         if(criterion.getValues() != null)
         {
            List<Serializable> newValues = new ArrayList<>();

            for(Serializable value : criterion.getValues())
            {
               String       valueAsString    = ValueUtils.getValueAsString(value);
               Serializable interpretedValue = variableInterpreter.interpret(valueAsString);
               newValues.add(interpretedValue);
            }
            criterion.setValues(newValues);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Integer consumeRecords(ReportInput reportInput, QReportDataSource dataSource, List<QRecord> records, QReportView tableView, List<QReportView> summaryViews, List<QReportView> variantViews) throws QException
   {
      QTableMetaData table = reportInput.getInstance().getTable(dataSource.getSourceTable());

      ////////////////////////////////////////////////////////////////////////////
      // if this record goes on a table view, add it to the report streamer now //
      ////////////////////////////////////////////////////////////////////////////
      if(tableView != null)
      {
         reportStreamer.addRecords(records);
      }

      /////////////////////////////////
      // do aggregates for summaries //
      /////////////////////////////////
      if(summaryViews != null)
      {
         for(QReportView summaryView : summaryViews)
         {
            addRecordsToSummaryAggregates(summaryView, table, records, summaryAggregates);
         }
      }

      if(variantViews != null)
      {
         for(QReportView variantView : variantViews)
         {
            addRecordsToSummaryAggregates(variantView, table, records, varianceAggregates);
         }
      }

      ///////////////////////////////////////////
      // do totals too, if any views want them //
      ///////////////////////////////////////////
      if(summaryViews != null && summaryViews.stream().anyMatch(QReportView::getIncludeTotalRow))
      {
         for(QRecord record : records)
         {
            addRecordToAggregatesMap(table, record, totalAggregates);
         }
      }

      if(variantViews != null && variantViews.stream().anyMatch(QReportView::getIncludeTotalRow))
      {
         for(QRecord record : records)
         {
            addRecordToAggregatesMap(table, record, varianceTotalAggregates);
         }
      }

      return (records.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecordsToSummaryAggregates(QReportView view, QTableMetaData table, List<QRecord> records, Map<String, Map<SummaryKey, Map<String, AggregatesInterface<?>>>> aggregatesMap)
   {
      Map<SummaryKey, Map<String, AggregatesInterface<?>>> viewAggregates = aggregatesMap.computeIfAbsent(view.getName(), (name) -> new HashMap<>());

      for(QRecord record : records)
      {
         SummaryKey key = new SummaryKey();
         for(String summaryField : view.getPivotFields())
         {
            Serializable summaryValue = record.getValue(summaryField);
            if(table.getField(summaryField).getPossibleValueSourceName() != null)
            {
               summaryValue = record.getDisplayValue(summaryField);
            }
            key.add(summaryField, summaryValue);

            if(view.getIncludePivotSubTotals() && key.getKeys().size() < view.getPivotFields().size())
            {
               /////////////////////////////////////////////////////////////////////////////////////////
               // be careful here, with these key objects, and their identity, being used as map keys //
               /////////////////////////////////////////////////////////////////////////////////////////
               SummaryKey subKey = key.clone();
               addRecordToSummaryKeyAggregates(table, record, viewAggregates, subKey);
            }
         }

         addRecordToSummaryKeyAggregates(table, record, viewAggregates, key);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecordToSummaryKeyAggregates(QTableMetaData table, QRecord record, Map<SummaryKey, Map<String, AggregatesInterface<?>>> viewAggregates, SummaryKey key)
   {
      Map<String, AggregatesInterface<?>> keyAggregates = viewAggregates.computeIfAbsent(key, (name) -> new HashMap<>());
      addRecordToAggregatesMap(table, record, keyAggregates);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecordToAggregatesMap(QTableMetaData table, QRecord record, Map<String, AggregatesInterface<?>> aggregatesMap)
   {
      for(QFieldMetaData field : table.getFields().values())
      {
         if(field.getType().equals(QFieldType.INTEGER))
         {
            @SuppressWarnings("unchecked")
            AggregatesInterface<Integer> fieldAggregates = (AggregatesInterface<Integer>) aggregatesMap.computeIfAbsent(field.getName(), (name) -> new IntegerAggregates());
            fieldAggregates.add(record.getValueInteger(field.getName()));
         }
         else if(field.getType().equals(QFieldType.DECIMAL))
         {
            @SuppressWarnings("unchecked")
            AggregatesInterface<BigDecimal> fieldAggregates = (AggregatesInterface<BigDecimal>) aggregatesMap.computeIfAbsent(field.getName(), (name) -> new BigDecimalAggregates());
            fieldAggregates.add(record.getValueBigDecimal(field.getName()));
         }
         // todo - more types (dates, at least?)
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void outputSummaries(ReportInput reportInput) throws QReportingException, QFormulaException
   {
      List<QReportView> reportViews = report.getViews().stream().filter(v -> v.getType().equals(ReportType.SUMMARY)).toList();
      for(QReportView view : reportViews)
      {
         QReportDataSource dataSource    = report.getDataSource(view.getDataSourceName());
         QTableMetaData    table         = reportInput.getInstance().getTable(dataSource.getSourceTable());
         SummaryOutput     summaryOutput = computeSummaryRowsForView(reportInput, view, table);

         ExportInput exportInput = new ExportInput(reportInput.getInstance());
         exportInput.setSession(reportInput.getSession());
         exportInput.setReportFormat(reportFormat);
         exportInput.setFilename(reportInput.getFilename());
         exportInput.setTitleRow(summaryOutput.titleRow);
         exportInput.setIncludeHeaderRow(view.getIncludeHeaderRow());
         exportInput.setReportOutputStream(reportInput.getReportOutputStream());

         reportStreamer.setDisplayFormats(getDisplayFormatMap(view));
         reportStreamer.start(exportInput, getFields(table, view), view.getLabel());

         reportStreamer.addRecords(summaryOutput.summaryRows); // todo - what if this set is huge?

         if(summaryOutput.totalRow != null)
         {
            reportStreamer.addTotalsRow(summaryOutput.totalRow);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<String, String> getDisplayFormatMap(QReportView view)
   {
      return (view.getColumns().stream()
         .filter(c -> c.getDisplayFormat() != null)
         .collect(Collectors.toMap(QReportField::getName, QReportField::getDisplayFormat)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<String, String> getDisplayFormatMap(List<QFieldMetaData> fields)
   {
      return (fields.stream()
         .filter(f -> f.getDisplayFormat() != null)
         .collect(Collectors.toMap(QFieldMetaData::getName, QFieldMetaData::getDisplayFormat)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QFieldMetaData> getFields(QTableMetaData table, QReportView view)
   {
      List<QFieldMetaData> fields = new ArrayList<>();
      for(String pivotField : view.getPivotFields())
      {
         QFieldMetaData field = table.getField(pivotField);
         fields.add(new QFieldMetaData(pivotField, field.getType()).withLabel(field.getLabel())); // todo do we need the type?  if so need table as input here
      }
      for(QReportField column : view.getColumns())
      {
         fields.add(new QFieldMetaData().withName(column.getName()).withLabel(column.getLabel())); // todo do we need the type?  if so need table as input here
      }
      return (fields);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private SummaryOutput computeSummaryRowsForView(ReportInput reportInput, QReportView view, QTableMetaData table) throws QReportingException, QFormulaException
   {
      QValueFormatter              valueFormatter      = new QValueFormatter();
      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", reportInput.getInputValues());
      variableInterpreter.addValueMap("total", getSummaryValuesForInterpreter(totalAggregates));

      ///////////
      // title //
      ///////////
      String title = getTitle(view, variableInterpreter);

      /////////////////////////
      // create summary rows //
      /////////////////////////
      List<QRecord> summaryRows = new ArrayList<>();
      for(Map.Entry<SummaryKey, Map<String, AggregatesInterface<?>>> entry : summaryAggregates.getOrDefault(view.getName(), Collections.emptyMap()).entrySet())
      {
         SummaryKey                          summaryKey      = entry.getKey();
         Map<String, AggregatesInterface<?>> fieldAggregates = entry.getValue();
         Map<String, Serializable>           summaryValues   = getSummaryValuesForInterpreter(fieldAggregates);
         variableInterpreter.addValueMap("pivot", summaryValues);
         variableInterpreter.addValueMap("summary", summaryValues);

         HashMap<String, Serializable> thisRowValues = new HashMap<>();
         variableInterpreter.addValueMap("thisRow", thisRowValues);

         if(!varianceAggregates.isEmpty())
         {
            Map<SummaryKey, Map<String, AggregatesInterface<?>>> varianceMap    = varianceAggregates.getOrDefault(view.getName(), Collections.emptyMap());
            Map<String, AggregatesInterface<?>>                  varianceSubMap = varianceMap.getOrDefault(summaryKey, Collections.emptyMap());
            Map<String, Serializable>                            varianceValues = getSummaryValuesForInterpreter(varianceSubMap);
            variableInterpreter.addValueMap("variancePivot", varianceValues);
            variableInterpreter.addValueMap("variance", varianceValues);
         }

         QRecord summaryRow = new QRecord();
         summaryRows.add(summaryRow);

         ////////////////////////////
         // add the summary values //
         ////////////////////////////
         for(Pair<String, Serializable> key : summaryKey.getKeys())
         {
            summaryRow.setValue(key.getA(), key.getB());
         }

         ///////////////////////////////////////////////////////////////////////////////
         // for summary subtotals, add the text "Total" to the last field in this key //
         ///////////////////////////////////////////////////////////////////////////////
         if(summaryKey.getKeys().size() < view.getPivotFields().size())
         {
            String fieldName = summaryKey.getKeys().get(summaryKey.getKeys().size() - 1).getA();
            summaryRow.setValue(fieldName, summaryRow.getValueString(fieldName) + " Total");
         }

         ///////////////////////////
         // add the column values //
         ///////////////////////////
         for(QReportField column : view.getColumns())
         {
            Serializable serializable = getValueForColumn(variableInterpreter, column);
            summaryRow.setValue(column.getName(), serializable);
            thisRowValues.put(column.getName(), serializable);
         }
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // sort the summary rows                                                            //
      // Note - this will NOT work correctly if there's more than 1 pivot field, as we're //
      // not doing anything to keep related rows them together (e.g., all MO state rows)  //
      //////////////////////////////////////////////////////////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(view.getOrderByFields()))
      {
         summaryRows.sort((o1, o2) ->
         {
            return summaryRowComparator(view, o1, o2);
         });
      }

      ////////////////
      // totals row //
      ////////////////
      QRecord totalRow = null;
      if(view.getIncludeTotalRow())
      {
         totalRow = new QRecord();

         for(String pivotField : view.getPivotFields())
         {
            if(totalRow.getValues().isEmpty())
            {
               totalRow.setValue(pivotField, "Totals");
            }
         }

         Map<String, Serializable> totalValues = getSummaryValuesForInterpreter(totalAggregates);
         variableInterpreter.addValueMap("pivot", totalValues);
         variableInterpreter.addValueMap("summary", totalValues);

         Map<String, Serializable> varianceTotalValues = getSummaryValuesForInterpreter(varianceTotalAggregates);
         variableInterpreter.addValueMap("variancePivot", varianceTotalValues);
         variableInterpreter.addValueMap("variance", varianceTotalValues);

         HashMap<String, Serializable> thisRowValues = new HashMap<>();
         variableInterpreter.addValueMap("thisRow", thisRowValues);

         for(QReportField column : view.getColumns())
         {
            Serializable serializable = getValueForColumn(variableInterpreter, column);
            totalRow.setValue(column.getName(), serializable);
            thisRowValues.put(column.getName(), serializable);

            String formatted = valueFormatter.formatValue(column.getDisplayFormat(), serializable);
         }
      }

      return (new SummaryOutput(summaryRows, title, totalRow));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getTitle(QReportView view, QMetaDataVariableInterpreter variableInterpreter)
   {
      String title = null;
      if(view.getTitleFields() != null && StringUtils.hasContent(view.getTitleFormat()))
      {
         List<String> titleValues = new ArrayList<>();
         for(String titleField : view.getTitleFields())
         {
            titleValues.add(variableInterpreter.interpret(titleField));
         }

         title = new QValueFormatter().formatStringWithValues(view.getTitleFormat(), titleValues);
      }
      else if(StringUtils.hasContent(view.getTitleFormat()))
      {
         title = view.getTitleFormat();
      }

      return title;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Serializable getValueForColumn(QMetaDataVariableInterpreter variableInterpreter, QReportField column) throws QFormulaException
   {
      String       formula = column.getFormula();
      Serializable result;
      if(formula.startsWith("=") && formula.length() > 1)
      {
         result = FormulaInterpreter.interpretFormula(variableInterpreter, formula.substring(1));
      }
      else
      {
         result = variableInterpreter.interpretForObject(formula, null);
      }
      return (result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings({ "rawtypes", "unchecked" })
   private int summaryRowComparator(QReportView view, QRecord o1, QRecord o2)
   {
      if(o1 == o2)
      {
         return (0);
      }

      for(QFilterOrderBy orderByField : view.getOrderByFields())
      {
         Comparable c1 = (Comparable) o1.getValue(orderByField.getFieldName());
         Comparable c2 = (Comparable) o2.getValue(orderByField.getFieldName());

         if(c1 == null && c2 == null)
         {
            continue;
         }
         if(c1 == null)
         {
            return (orderByField.getIsAscending() ? -1 : 1);
         }
         if(c2 == null)
         {
            return (orderByField.getIsAscending() ? 1 : -1);
         }

         int comp = orderByField.getIsAscending() ? c1.compareTo(c2) : c2.compareTo(c1);
         if(comp != 0)
         {
            return (comp);
         }
      }

      return (0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<String, Serializable> getSummaryValuesForInterpreter(Map<String, AggregatesInterface<?>> fieldAggregates)
   {
      Map<String, Serializable> summaryValuesForInterpreter = new HashMap<>();
      for(Map.Entry<String, AggregatesInterface<?>> subEntry : fieldAggregates.entrySet())
      {
         String                 fieldName  = subEntry.getKey();
         AggregatesInterface<?> aggregates = subEntry.getValue();
         summaryValuesForInterpreter.put("sum." + fieldName, aggregates.getSum());
         summaryValuesForInterpreter.put("count." + fieldName, aggregates.getCount());
         summaryValuesForInterpreter.put("min." + fieldName, aggregates.getMin());
         summaryValuesForInterpreter.put("max." + fieldName, aggregates.getMax());
         summaryValuesForInterpreter.put("average." + fieldName, aggregates.getAverage());
      }
      return summaryValuesForInterpreter;
   }



   /*******************************************************************************
    ** record to serve as tuple/multi-value output of computeSummaryRowsForView method.
    *******************************************************************************/
   private record SummaryOutput(List<QRecord> summaryRows, String titleRow, QRecord totalRow)
   {
   }

}
