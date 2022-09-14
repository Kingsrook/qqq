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
   Map<String, Map<PivotKey, Map<String, AggregatesInterface<?>>>> pivotAggregates = new HashMap<>();

   Map<String, AggregatesInterface<?>> totalAggregates = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public void execute(ReportInput reportInput) throws QException
   {
      gatherData(reportInput);
      output(reportInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void gatherData(ReportInput reportInput) throws QException
   {
      QReportMetaData report      = reportInput.getInstance().getReport(reportInput.getReportName());
      QQueryFilter    queryFilter = report.getQueryFilter();

      setInputValuesInQueryFilter(reportInput, queryFilter);

      RecordPipe recordPipe = new RecordPipe();
      new AsyncRecordPipeLoop().run("Report[" + reportInput.getReportName() + "]", null, recordPipe, (callback) ->
      {
         QueryInput queryInput = new QueryInput(reportInput.getInstance());
         queryInput.setSession(reportInput.getSession());
         queryInput.setRecordPipe(recordPipe);
         queryInput.setTableName(report.getSourceTable());
         queryInput.setFilter(queryFilter);
         return (new QueryAction().execute(queryInput));
      }, () -> consumeRecords(report, reportInput, recordPipe));
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
   private Integer consumeRecords(QReportMetaData report, ReportInput reportInput, RecordPipe recordPipe)
   {
      // todo - stream to output if report has a simple type output
      List<QRecord> records = recordPipe.consumeAvailableRecords();

      //////////////////////////////
      // do aggregates for pivots //
      //////////////////////////////
      QTableMetaData table = reportInput.getInstance().getTable(report.getSourceTable());
      report.getViews().stream().filter(v -> v.getType().equals(ReportType.PIVOT)).forEach((view) ->
      {
         doPivotAggregates(view, table, records);
      });

      return (records.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void doPivotAggregates(QReportView view, QTableMetaData table, List<QRecord> records)
   {
      Map<PivotKey, Map<String, AggregatesInterface<?>>> viewAggregates = pivotAggregates.computeIfAbsent(view.getName(), (name) -> new HashMap<>());

      for(QRecord record : records)
      {
         PivotKey key = new PivotKey();
         for(String pivotField : view.getPivotFields())
         {
            key.add(pivotField, record.getValue(pivotField));
         }

         Map<String, AggregatesInterface<?>> keyAggregates = viewAggregates.computeIfAbsent(key, (name) -> new HashMap<>());

         for(QFieldMetaData field : table.getFields().values())
         {
            if(field.getType().equals(QFieldType.INTEGER))
            {
               @SuppressWarnings("unchecked")
               AggregatesInterface<Integer> fieldAggregates = (AggregatesInterface<Integer>) keyAggregates.computeIfAbsent(field.getName(), (name) -> new IntegerAggregates());
               fieldAggregates.add(record.getValueInteger(field.getName()));

               @SuppressWarnings("unchecked")
               AggregatesInterface<Integer> fieldTotalAggregates = (AggregatesInterface<Integer>) totalAggregates.computeIfAbsent(field.getName(), (name) -> new IntegerAggregates());
               fieldTotalAggregates.add(record.getValueInteger(field.getName()));
            }
            else if(field.getType().equals(QFieldType.DECIMAL))
            {
               @SuppressWarnings("unchecked")
               AggregatesInterface<BigDecimal> fieldAggregates = (AggregatesInterface<BigDecimal>) keyAggregates.computeIfAbsent(field.getName(), (name) -> new BigDecimalAggregates());
               fieldAggregates.add(record.getValueBigDecimal(field.getName()));

               @SuppressWarnings("unchecked")
               AggregatesInterface<BigDecimal> fieldTotalAggregates = (AggregatesInterface<BigDecimal>) totalAggregates.computeIfAbsent(field.getName(), (name) -> new BigDecimalAggregates());
               fieldTotalAggregates.add(record.getValueBigDecimal(field.getName()));
            }
            // todo - more types (dates, at least?)
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void output(ReportInput reportInput) throws QReportingException, QFormulaException
   {
      QReportMetaData report = reportInput.getInstance().getReport(reportInput.getReportName());
      QTableMetaData  table  = reportInput.getInstance().getTable(report.getSourceTable());

      List<QReportView> reportViews = report.getViews().stream().filter(v -> v.getType().equals(ReportType.PIVOT)).toList();
      for(QReportView view : reportViews)
      {
         PivotOutput  pivotOutput  = outputPivot(reportInput, view, table);
         ReportFormat reportFormat = reportInput.getReportFormat();

         ExportInput exportInput = new ExportInput(reportInput.getInstance());
         exportInput.setSession(reportInput.getSession());
         exportInput.setReportFormat(reportFormat);
         exportInput.setFilename(reportInput.getFilename());
         exportInput.setTitleRow(pivotOutput.titleRow);
         exportInput.setReportOutputStream(reportInput.getReportOutputStream());

         ExportStreamerInterface reportStreamer = reportFormat.newReportStreamer();
         reportStreamer.setDisplayFormats(getDisplayFormatMap(view));
         reportStreamer.start(exportInput, getFields(table, view));

         RecordPipe recordPipe = new RecordPipe(); // todo - make it an unlimited pipe or something...
         recordPipe.addRecords(pivotOutput.pivotRows);
         reportStreamer.takeRecordsFromPipe(recordPipe);

         if(pivotOutput.totalRow != null)
         {
            reportStreamer.addTotalsRow(pivotOutput.totalRow);
         }

         reportStreamer.finish();
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
   private PivotOutput outputPivot(ReportInput reportInput, QReportView view, QTableMetaData table) throws QReportingException, QFormulaException
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

         QRecord pivotRow = new QRecord();
         pivotRows.add(pivotRow);
         for(Pair<String, Serializable> key : pivotKey.getKeys())
         {
            pivotRow.setValue(key.getA(), key.getB());
         }

         for(QReportField column : view.getColumns())
         {
            Serializable serializable = getValueForColumn(variableInterpreter, column);
            pivotRow.setValue(column.getName(), serializable);
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
      String       formula      = column.getFormula();
      Serializable serializable = variableInterpreter.interpretForObject(formula);
      if(formula.startsWith("=") && formula.length() > 1)
      {
         // serializable = interpretFormula(variableInterpreter, formula);
         serializable = FormulaInterpreter.interpretFormula(variableInterpreter, formula.substring(1));
      }
      return serializable;
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
