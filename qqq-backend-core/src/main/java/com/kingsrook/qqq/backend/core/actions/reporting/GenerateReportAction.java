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
import java.util.Optional;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.async.AsyncRecordPipeLoop;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QFormulaException;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportField;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.ReportType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.aggregates.AggregatesInterface;
import com.kingsrook.qqq.backend.core.utils.aggregates.BigDecimalAggregates;
import com.kingsrook.qqq.backend.core.utils.aggregates.IntegerAggregates;


/*******************************************************************************
 ** Action to generate a report!!
 *******************************************************************************/
public class GenerateReportAction
{
   //////////////////////////////////////////////////
   // viewName > PivotKey > fieldName > Aggregates //
   //////////////////////////////////////////////////
   Map<String, Map<PivotKey, Map<String, AggregatesInterface<?>>>> pivotAggregates         = new HashMap<>();
   Map<String, Map<PivotKey, Map<String, AggregatesInterface<?>>>> variancePivotAggregates = new HashMap<>();

   Map<String, AggregatesInterface<?>> totalAggregates         = new HashMap<>();
   Map<String, AggregatesInterface<?>> varianceTotalAggregates = new HashMap<>();

   private boolean                 includeTableView = false;
   private QReportMetaData         report;
   private ReportFormat            reportFormat;
   private ExportStreamerInterface reportStreamer;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void execute(ReportInput reportInput) throws QException
   {
      report = reportInput.getInstance().getReport(reportInput.getReportName());
      Optional<QReportView> tableView = report.getViews().stream().filter(v -> v.getType().equals(ReportType.TABLE)).findFirst();

      reportFormat = reportInput.getReportFormat();
      reportStreamer = reportFormat.newReportStreamer();

      if(tableView.isPresent())
      {
         includeTableView = true;
         startTableView(reportInput, tableView.get());
      }

      gatherData(reportInput);
      gatherVarianceData(reportInput);

      outputPivots(reportInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void startTableView(ReportInput reportInput, QReportView reportView) throws QReportingException
   {
      QTableMetaData table = reportInput.getInstance().getTable(report.getSourceTable());

      ExportInput exportInput = new ExportInput(reportInput.getInstance());
      exportInput.setSession(reportInput.getSession());
      exportInput.setReportFormat(reportFormat);
      exportInput.setFilename(reportInput.getFilename());
      exportInput.setReportOutputStream(reportInput.getReportOutputStream());

      // todo! reportStreamer.setDisplayFormats(getDisplayFormatMap(view));

      List<QFieldMetaData> fields;
      if(CollectionUtils.nullSafeHasContents(reportView.getColumns()))
      {
         fields = new ArrayList<>();
         for(QReportField column : reportView.getColumns())
         {
            fields.add(table.getField(column.getName()));
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
   private void gatherData(ReportInput reportInput) throws QException
   {
      QQueryFilter queryFilter = report.getQueryFilter();
      setInputValuesInQueryFilter(reportInput, queryFilter);

      RecordPipe recordPipe = new RecordPipe();
      new AsyncRecordPipeLoop().run("Report[" + reportInput.getReportName() + "]", null, recordPipe, (callback) ->
      {
         QueryInput queryInput = new QueryInput(reportInput.getInstance());
         queryInput.setSession(reportInput.getSession());
         queryInput.setRecordPipe(recordPipe);
         queryInput.setTableName(report.getSourceTable());
         queryInput.setFilter(queryFilter);
         queryInput.setShouldTranslatePossibleValues(true); // todo - any limits or conditions on this?
         return (new QueryAction().execute(queryInput));
      }, () -> consumeRecords(reportInput, recordPipe, false));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void gatherVarianceData(ReportInput reportInput) throws QException
   {
      QQueryFilter varianceQueryFilter = report.getVarianceQueryFilter();
      if(varianceQueryFilter == null)
      {
         return;
      }

      setInputValuesInQueryFilter(reportInput, varianceQueryFilter);

      RecordPipe recordPipe = new RecordPipe();
      new AsyncRecordPipeLoop().run("Report[" + reportInput.getReportName() + "]", null, recordPipe, (callback) ->
      {
         QueryInput queryInput = new QueryInput(reportInput.getInstance());
         queryInput.setSession(reportInput.getSession());
         queryInput.setRecordPipe(recordPipe);
         queryInput.setTableName(report.getSourceTable());
         queryInput.setFilter(varianceQueryFilter);
         queryInput.setShouldTranslatePossibleValues(true); // todo - any limits or conditions on this?
         return (new QueryAction().execute(queryInput));
      }, () -> consumeRecords(reportInput, recordPipe, true));
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
   private Integer consumeRecords(ReportInput reportInput, RecordPipe recordPipe, boolean isForVariance) throws QReportingException
   {
      List<QRecord> records = recordPipe.consumeAvailableRecords();

      if(includeTableView && !isForVariance)
      {
         reportStreamer.addRecords(records);
      }

      //////////////////////////////
      // do aggregates for pivots //
      //////////////////////////////
      QTableMetaData table = reportInput.getInstance().getTable(report.getSourceTable());
      report.getViews().stream().filter(v -> v.getType().equals(ReportType.PIVOT)).forEach((view) ->
      {
         addRecordsToPivotAggregates(view, table, records, isForVariance ? variancePivotAggregates : pivotAggregates);
      });

      ///////////////////////////////////////////
      // do totals too, if any views want them //
      ///////////////////////////////////////////
      if(report.getViews().stream().filter(v -> v.getType().equals(ReportType.PIVOT)).anyMatch(QReportView::getTotalRow))
      {
         for(QRecord record : records)
         {
            addRecordToAggregatesMap(table, record, isForVariance ? varianceTotalAggregates : totalAggregates);
         }
      }

      return (records.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecordsToPivotAggregates(QReportView view, QTableMetaData table, List<QRecord> records, Map<String, Map<PivotKey, Map<String, AggregatesInterface<?>>>> aggregatesMap)
   {
      Map<PivotKey, Map<String, AggregatesInterface<?>>> viewAggregates = aggregatesMap.computeIfAbsent(view.getName(), (name) -> new HashMap<>());

      for(QRecord record : records)
      {
         PivotKey key = new PivotKey();
         for(String pivotField : view.getPivotFields())
         {
            Serializable pivotValue = record.getValue(pivotField);
            if(table.getField(pivotField).getPossibleValueSourceName() != null)
            {
               pivotValue = record.getDisplayValue(pivotField);
            }
            key.add(pivotField, pivotValue);

            if(view.getPivotSubTotals() && key.getKeys().size() < view.getPivotFields().size())
            {
               /////////////////////////////////////////////////////////////////////////////////////////
               // be careful here, with these key objects, and their identity, being used as map keys //
               /////////////////////////////////////////////////////////////////////////////////////////
               PivotKey subKey = key.clone();
               addRecordToPivotKeyAggregates(table, record, viewAggregates, subKey);
            }
         }

         addRecordToPivotKeyAggregates(table, record, viewAggregates, key);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecordToPivotKeyAggregates(QTableMetaData table, QRecord record, Map<PivotKey, Map<String, AggregatesInterface<?>>> viewAggregates, PivotKey key)
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
   private void outputPivots(ReportInput reportInput) throws QReportingException, QFormulaException
   {
      QTableMetaData table = reportInput.getInstance().getTable(report.getSourceTable());

      List<QReportView> reportViews = report.getViews().stream().filter(v -> v.getType().equals(ReportType.PIVOT)).toList();
      for(QReportView view : reportViews)
      {
         PivotOutput pivotOutput = computePivotRowsForView(reportInput, view, table);

         ExportInput exportInput = new ExportInput(reportInput.getInstance());
         exportInput.setSession(reportInput.getSession());
         exportInput.setReportFormat(reportFormat);
         exportInput.setFilename(reportInput.getFilename());
         exportInput.setTitleRow(pivotOutput.titleRow);
         exportInput.setReportOutputStream(reportInput.getReportOutputStream());

         reportStreamer.setDisplayFormats(getDisplayFormatMap(view));
         reportStreamer.start(exportInput, getFields(table, view), view.getLabel());

         reportStreamer.addRecords(pivotOutput.pivotRows); // todo - what if this set is huge?

         if(pivotOutput.totalRow != null)
         {
            reportStreamer.addTotalsRow(pivotOutput.totalRow);
         }
      }

      reportStreamer.finish();
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
   private PivotOutput computePivotRowsForView(ReportInput reportInput, QReportView view, QTableMetaData table) throws QReportingException, QFormulaException
   {
      QValueFormatter              valueFormatter      = new QValueFormatter();
      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", reportInput.getInputValues());
      variableInterpreter.addValueMap("total", getPivotValuesForInterpreter(totalAggregates));

      ///////////
      // title //
      ///////////
      String title = null;
      if(view.getTitleFields() != null && StringUtils.hasContent(view.getTitleFormat()))
      {
         List<String> titleValues = new ArrayList<>();
         for(String titleField : view.getTitleFields())
         {
            titleValues.add(variableInterpreter.interpret(titleField));
         }

         title = valueFormatter.formatStringWithValues(view.getTitleFormat(), titleValues);
      }
      else if(StringUtils.hasContent(view.getTitleFormat()))
      {
         title = view.getTitleFormat();
      }

      if(StringUtils.hasContent(title))
      {
         System.out.println(title);
      }

      /////////////
      // headers //
      /////////////
      for(String field : view.getPivotFields())
      {
         System.out.printf("%-15s", table.getField(field).getLabel());
      }

      for(QReportField column : view.getColumns())
      {
         System.out.printf("%25s", column.getLabel());
      }
      System.out.println();

      ///////////////////////
      // create pivot rows //
      ///////////////////////
      List<QRecord> pivotRows = new ArrayList<>();
      for(Map.Entry<PivotKey, Map<String, AggregatesInterface<?>>> entry : pivotAggregates.getOrDefault(view.getName(), Collections.emptyMap()).entrySet())
      {
         PivotKey                            pivotKey        = entry.getKey();
         Map<String, AggregatesInterface<?>> fieldAggregates = entry.getValue();
         variableInterpreter.addValueMap("pivot", getPivotValuesForInterpreter(fieldAggregates));

         HashMap<String, Serializable> thisRowValues = new HashMap<>();
         variableInterpreter.addValueMap("thisRow", thisRowValues);

         if(!variancePivotAggregates.isEmpty())
         {
            Map<PivotKey, Map<String, AggregatesInterface<?>>> varianceMap    = variancePivotAggregates.getOrDefault(view.getName(), Collections.emptyMap());
            Map<String, AggregatesInterface<?>>                varianceSubMap = varianceMap.getOrDefault(pivotKey, Collections.emptyMap());
            variableInterpreter.addValueMap("variancePivot", getPivotValuesForInterpreter(varianceSubMap));
         }

         QRecord pivotRow = new QRecord();
         pivotRows.add(pivotRow);

         //////////////////////////
         // add the pivot values //
         //////////////////////////
         for(Pair<String, Serializable> key : pivotKey.getKeys())
         {
            pivotRow.setValue(key.getA(), key.getB());
         }

         /////////////////////////////////////////////////////////////////////////////
         // for pivot subtotals, add the text "Total" to the last field in this key //
         /////////////////////////////////////////////////////////////////////////////
         if(pivotKey.getKeys().size() < view.getPivotFields().size())
         {
            String fieldName = pivotKey.getKeys().get(pivotKey.getKeys().size() - 1).getA();
            pivotRow.setValue(fieldName, pivotRow.getValueString(fieldName) + " Total");
         }

         ///////////////////////////
         // add the column values //
         ///////////////////////////
         for(QReportField column : view.getColumns())
         {
            Serializable serializable = getValueForColumn(variableInterpreter, column);
            pivotRow.setValue(column.getName(), serializable);
            thisRowValues.put(column.getName(), serializable);
         }
      }

      /////////////////////////
      // sort the pivot rows //
      /////////////////////////
      if(CollectionUtils.nullSafeHasContents(view.getOrderByFields()))
      {
         pivotRows.sort((o1, o2) ->
         {
            return pivotRowComparator(view, o1, o2);
         });
      }

      /////////////////////////////////////////////
      // print the rows (just debugging i think) //
      /////////////////////////////////////////////
      for(QRecord pivotRow : pivotRows)
      {
         for(String pivotField : view.getPivotFields())
         {
            System.out.printf("%-15s", pivotRow.getValue(pivotField));
         }

         for(QReportField column : view.getColumns())
         {
            Serializable serializable = pivotRow.getValue(column.getName());
            String       formatted    = valueFormatter.formatValue(column.getDisplayFormat(), serializable);
            System.out.printf("%25s", formatted);
         }

         System.out.println();
      }

      ////////////////
      // totals row //
      ////////////////
      QRecord totalRow = null;
      if(view.getTotalRow())
      {
         totalRow = new QRecord();

         for(String pivotField : view.getPivotFields())
         {
            if(totalRow.getValues().isEmpty())
            {
               totalRow.setValue(pivotField, "Totals");
               System.out.printf("%-15s", "Totals");
            }
            else
            {
               System.out.printf("%-15s", "");
            }
         }

         variableInterpreter.addValueMap("pivot", getPivotValuesForInterpreter(totalAggregates));
         variableInterpreter.addValueMap("variancePivot", getPivotValuesForInterpreter(varianceTotalAggregates));

         for(QReportField column : view.getColumns())
         {
            Serializable serializable = getValueForColumn(variableInterpreter, column);
            totalRow.setValue(column.getName(), serializable);

            String formatted = valueFormatter.formatValue(column.getDisplayFormat(), serializable);
            System.out.printf("%25s", formatted);
         }

         System.out.println();
      }

      return (new PivotOutput(pivotRows, title, totalRow));
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
   private int pivotRowComparator(QReportView view, QRecord o1, QRecord o2)
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
   private Map<String, Serializable> getPivotValuesForInterpreter(Map<String, AggregatesInterface<?>> fieldAggregates)
   {
      Map<String, Serializable> pivotValuesForInterpreter = new HashMap<>();
      for(Map.Entry<String, AggregatesInterface<?>> subEntry : fieldAggregates.entrySet())
      {
         String                 fieldName  = subEntry.getKey();
         AggregatesInterface<?> aggregates = subEntry.getValue();
         pivotValuesForInterpreter.put("sum." + fieldName, aggregates.getSum());
         pivotValuesForInterpreter.put("count." + fieldName, aggregates.getCount());
         // todo min, max, avg
      }
      return pivotValuesForInterpreter;
   }



   /*******************************************************************************
    ** record to serve as tuple/multi-value output of outputPivot method.
    *******************************************************************************/
   private record PivotOutput(List<QRecord> pivotRows, String titleRow, QRecord totalRow)
   {
   }

}
