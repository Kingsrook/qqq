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


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportField;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.ReportType;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.testutils.PersonQRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for GenerateReportAction
 *******************************************************************************/
public class GenerateReportActionTest
{
   private static final String REPORT_NAME = "personReport1";



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      ListOfMapsExportStreamer.reset();
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivot1() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      qInstance.addReport(definePersonShoesPivotReport(true));
      insertPersonRecords(qInstance);
      runReport(qInstance, Map.of("startDate", LocalDate.of(1980, Month.JANUARY, 1), "endDate", LocalDate.of(1980, Month.DECEMBER, 31)));

      List<Map<String, String>>     list     = ListOfMapsExportStreamer.getList("pivot");
      Iterator<Map<String, String>> iterator = list.iterator();
      Map<String, String>           row      = iterator.next();
      assertEquals(3, list.size());
      assertThat(list.get(0)).containsOnlyKeys("Last Name", "Report Start Date", "Report End Date", "Person Count", "Quantity", "Revenue", "Cost", "Profit", "Cost Per", "% Total", "Margins", "Revenue Per", "Margin Per");

      assertThat(row.get("Last Name")).isEqualTo("Keller");
      assertThat(row.get("Person Count")).isEqualTo("1");
      assertThat(row.get("Quantity")).isEqualTo("5");
      assertThat(row.get("Report Start Date")).isEqualTo("1980-01-01");
      assertThat(row.get("Report End Date")).isEqualTo("1980-12-31");
      assertThat(row.get("Cost")).isEqualTo("3.50");
      assertThat(row.get("Revenue")).isEqualTo("2.40");
      assertThat(row.get("Cost Per")).isEqualTo("0.70");
      assertThat(row.get("Revenue Per")).isEqualTo("0.48");
      assertThat(row.get("Margin Per")).isEqualTo("-0.22");

      row = iterator.next();
      assertThat(row.get("Last Name")).isEqualTo("Kelkhoff");
      assertThat(row.get("Person Count")).isEqualTo("2");
      assertThat(row.get("Quantity")).isEqualTo("13");
      assertThat(row.get("Cost")).isEqualTo("7.00"); // sum of the 2 Kelkhoff rows' costs
      assertThat(row.get("Revenue")).isEqualTo("8.40"); // sum of the 2 Kelkhoff rows' price
      assertThat(row.get("Cost Per")).isEqualTo("0.54"); // sum cost / quantity
      assertThat(row.get("Revenue Per")).isEqualTo("0.65"); // sum price (Revenue) / quantity
      assertThat(row.get("Margin Per")).isEqualTo("0.11"); // Revenue Per - Cost Per

      row = iterator.next();
      assertThat(row.get("Last Name")).isEqualTo("Totals");
      assertThat(row.get("Person Count")).isEqualTo("3");
      assertThat(row.get("Quantity")).isEqualTo("18");
      assertThat(row.get("Cost")).isEqualTo("10.50");
      assertThat(row.get("Cost Per")).startsWith("0.58");
      assertThat(row.get("Cost")).isEqualTo("10.50"); // sum of all 3 matching rows' costs
      assertThat(row.get("Revenue")).isEqualTo("10.80"); // sum of all 3 matching rows' price
      assertThat(row.get("Profit")).isEqualTo("0.30"); // Revenue - Cost
      assertThat(row.get("Margins")).isEqualTo("0.03"); // 100*Profit / Revenue
      assertThat(row.get("Cost Per")).isEqualTo("0.58"); // sum cost / quantity
      assertThat(row.get("Revenue Per")).isEqualTo("0.60"); // sum price (Revenue) / quantity
      assertThat(row.get("Margin Per")).isEqualTo("0.02"); // Revenue Per - Cost Per
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivot2() throws QException
   {
      QInstance       qInstance = TestUtils.defineInstance();
      QReportMetaData report    = definePersonShoesPivotReport(false);

      //////////////////////////////////////////////
      // change from the default to sort reversed //
      //////////////////////////////////////////////
      report.getViews().get(0).getOrderByFields().get(0).setIsAscending(false);
      qInstance.addReport(report);
      insertPersonRecords(qInstance);
      runReport(qInstance, Map.of("startDate", LocalDate.of(1980, Month.JANUARY, 1), "endDate", LocalDate.of(1980, Month.DECEMBER, 31)));

      List<Map<String, String>>     list     = ListOfMapsExportStreamer.getList("pivot");
      Iterator<Map<String, String>> iterator = list.iterator();
      Map<String, String>           row      = iterator.next();
      assertEquals(2, list.size());

      assertThat(row.get("Last Name")).isEqualTo("Kelkhoff");
      assertThat(row.get("Quantity")).isEqualTo("13");

      row = iterator.next();
      assertThat(row.get("Last Name")).isEqualTo("Keller");
      assertThat(row.get("Quantity")).isEqualTo("5");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivot3() throws QException
   {
      QInstance       qInstance = TestUtils.defineInstance();
      QReportMetaData report    = definePersonShoesPivotReport(false);

      //////////////////////////////////////////////////////////////////////////////////////////////
      // remove the filters, change to sort by personCount (to get some ties), then sumPrice desc //
      // this also shows the behavior of a null value in an order by                              //
      //////////////////////////////////////////////////////////////////////////////////////////////
      report.getDataSources().get(0).getQueryFilter().setCriteria(null);
      report.getViews().get(0).setOrderByFields(List.of(new QFilterOrderBy("personCount"), new QFilterOrderBy("sumPrice", false)));
      qInstance.addReport(report);
      insertPersonRecords(qInstance);
      runReport(qInstance, Map.of("startDate", LocalDate.now(), "endDate", LocalDate.now()));

      List<Map<String, String>>     list     = ListOfMapsExportStreamer.getList("pivot");
      Iterator<Map<String, String>> iterator = list.iterator();
      Map<String, String>           row      = iterator.next();

      assertEquals(5, list.size());
      assertThat(row.get("Last Name")).isEqualTo("Keller");
      assertThat(row.get("Person Count")).isEqualTo("1");
      assertThat(row.get("Revenue")).isEqualTo("2.40");

      row = iterator.next();
      assertThat(row.get("Last Name")).isEqualTo("Kelly");
      assertThat(row.get("Person Count")).isEqualTo("1");
      assertThat(row.get("Revenue")).isEqualTo("1.20");

      row = iterator.next();
      assertThat(row.get("Last Name")).isEqualTo("Jones");
      assertThat(row.get("Person Count")).isEqualTo("1");
      assertThat(row.get("Revenue")).isEqualTo("1.00");

      row = iterator.next();
      assertThat(row.get("Last Name")).isEqualTo("Jonson");
      assertThat(row.get("Person Count")).isEqualTo("1");
      assertThat(row.get("Revenue")).isNull();

      row = iterator.next();
      assertThat(row.get("Last Name")).isEqualTo("Kelkhoff");
      assertThat(row.get("Person Count")).isEqualTo("2");
      assertThat(row.get("Revenue")).isEqualTo("8.40");

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivot4() throws QException
   {
      QInstance       qInstance = TestUtils.defineInstance();
      QReportMetaData report    = definePersonShoesPivotReport(false);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // remove the filter, change to have 2 pivot columns - homeStateId and lastName - we should get no roll-up like this. //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      report.getDataSources().get(0).getQueryFilter().setCriteria(null);
      report.getViews().get(0).setPivotFields(List.of(
         "homeStateId",
         "lastName"
      ));
      qInstance.addReport(report);
      insertPersonRecords(qInstance);
      runReport(qInstance, Map.of("startDate", LocalDate.now(), "endDate", LocalDate.now()));

      List<Map<String, String>>     list     = ListOfMapsExportStreamer.getList("pivot");
      Iterator<Map<String, String>> iterator = list.iterator();
      Map<String, String>           row      = iterator.next();
      assertEquals(6, list.size());

      assertThat(row.get("Home State Id")).isEqualTo("IL");
      assertThat(row.get("Last Name")).isEqualTo("Jonson");
      assertThat(row.get("Quantity")).isNull();

      row = iterator.next();
      assertThat(row.get("Home State Id")).isEqualTo("IL");
      assertThat(row.get("Last Name")).isEqualTo("Jones");
      assertThat(row.get("Quantity")).isEqualTo("3");

      row = iterator.next();
      assertThat(row.get("Home State Id")).isEqualTo("IL");
      assertThat(row.get("Last Name")).isEqualTo("Kelly");
      assertThat(row.get("Quantity")).isEqualTo("4");

      row = iterator.next();
      assertThat(row.get("Home State Id")).isEqualTo("IL");
      assertThat(row.get("Last Name")).isEqualTo("Keller");
      assertThat(row.get("Quantity")).isEqualTo("5");

      row = iterator.next();
      assertThat(row.get("Home State Id")).isEqualTo("IL");
      assertThat(row.get("Last Name")).isEqualTo("Kelkhoff");
      assertThat(row.get("Quantity")).isEqualTo("6");

      row = iterator.next();
      assertThat(row.get("Home State Id")).isEqualTo("MO");
      assertThat(row.get("Last Name")).isEqualTo("Kelkhoff");
      assertThat(row.get("Quantity")).isEqualTo("7");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivot5() throws QException
   {
      QInstance       qInstance = TestUtils.defineInstance();
      QReportMetaData report    = definePersonShoesPivotReport(false);

      /////////////////////////////////////////////////////////////////////////////////////
      // remove the filter, and just pivot on homeStateId - should aggregate differently //
      /////////////////////////////////////////////////////////////////////////////////////
      report.getDataSources().get(0).getQueryFilter().setCriteria(null);
      report.getViews().get(0).setPivotFields(List.of("homeStateId"));
      qInstance.addReport(report);
      insertPersonRecords(qInstance);
      runReport(qInstance, Map.of("startDate", LocalDate.now(), "endDate", LocalDate.now()));

      List<Map<String, String>>     list     = ListOfMapsExportStreamer.getList("pivot");
      Iterator<Map<String, String>> iterator = list.iterator();
      Map<String, String>           row      = iterator.next();
      assertEquals(2, list.size());
      assertThat(row.get("Home State Id")).isEqualTo("MO");
      assertThat(row.get("Last Name")).isNull();
      assertThat(row.get("Quantity")).isEqualTo("7");

      row = iterator.next();
      assertThat(row.get("Home State Id")).isEqualTo("IL");
      assertThat(row.get("Last Name")).isNull();
      assertThat(row.get("Quantity")).isEqualTo("18");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void runToCsv() throws Exception
   {
      String name = "/tmp/report.csv";
      try(FileOutputStream fileOutputStream = new FileOutputStream(name))
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.addReport(definePersonShoesPivotReport(true));
         insertPersonRecords(qInstance);

         ReportInput reportInput = new ReportInput(qInstance);
         reportInput.setSession(new QSession());
         reportInput.setReportName(REPORT_NAME);
         reportInput.setReportFormat(ReportFormat.CSV);
         reportInput.setReportOutputStream(fileOutputStream);
         reportInput.setInputValues(Map.of("startDate", LocalDate.of(1970, Month.MAY, 15), "endDate", LocalDate.now()));
         new GenerateReportAction().execute(reportInput);
         System.out.println("Wrote File: " + name);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void runToXlsx() throws Exception
   {
      String name = "/tmp/report.xlsx";
      try(FileOutputStream fileOutputStream = new FileOutputStream(name))
      {
         QInstance qInstance = TestUtils.defineInstance();
         qInstance.addReport(definePersonShoesPivotReport(true));
         insertPersonRecords(qInstance);

         ReportInput reportInput = new ReportInput(qInstance);
         reportInput.setSession(new QSession());
         reportInput.setReportName(REPORT_NAME);
         reportInput.setReportFormat(ReportFormat.XLSX);
         reportInput.setReportOutputStream(fileOutputStream);
         reportInput.setInputValues(Map.of("startDate", LocalDate.of(1970, Month.MAY, 15), "endDate", LocalDate.now()));
         new GenerateReportAction().execute(reportInput);
         System.out.println("Wrote File: " + name);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runReport(QInstance qInstance, Map<String, Serializable> inputValues) throws QException
   {
      ReportInput reportInput = new ReportInput(qInstance);
      reportInput.setSession(new QSession());
      reportInput.setReportName(REPORT_NAME);
      reportInput.setReportFormat(ReportFormat.LIST_OF_MAPS);
      reportInput.setReportOutputStream(new ByteArrayOutputStream());
      reportInput.setInputValues(inputValues);
      new GenerateReportAction().execute(reportInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void insertPersonRecords(QInstance qInstance) throws QException
   {
      TestUtils.insertRecords(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new PersonQRecord().withLastName("Jonson").withBirthDate(LocalDate.of(1980, Month.JANUARY, 31)).withNoOfShoes(null).withHomeStateId(1).withPrice(null).withCost(new BigDecimal("0.50")), // wrong last initial
         new PersonQRecord().withLastName("Jones").withBirthDate(LocalDate.of(1980, Month.JANUARY, 31)).withNoOfShoes(3).withHomeStateId(1).withPrice(new BigDecimal("1.00")).withCost(new BigDecimal("0.50")), // wrong last initial
         new PersonQRecord().withLastName("Kelly").withBirthDate(LocalDate.of(1979, Month.DECEMBER, 30)).withNoOfShoes(4).withHomeStateId(1).withPrice(new BigDecimal("1.20")).withCost(new BigDecimal("0.50")), // bad birthdate
         new PersonQRecord().withLastName("Keller").withBirthDate(LocalDate.of(1980, Month.JANUARY, 7)).withNoOfShoes(5).withHomeStateId(1).withPrice(new BigDecimal("2.40")).withCost(new BigDecimal("3.50")),
         new PersonQRecord().withLastName("Kelkhoff").withBirthDate(LocalDate.of(1980, Month.FEBRUARY, 15)).withNoOfShoes(6).withHomeStateId(1).withPrice(new BigDecimal("3.60")).withCost(new BigDecimal("3.50")),
         new PersonQRecord().withLastName("Kelkhoff").withBirthDate(LocalDate.of(1980, Month.MARCH, 20)).withNoOfShoes(7).withHomeStateId(2).withPrice(new BigDecimal("4.80")).withCost(new BigDecimal("3.50"))
      ));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QReportMetaData definePersonShoesPivotReport(boolean includeTotalRow)
   {
      return new QReportMetaData()
         .withName(REPORT_NAME)
         .withDataSources(List.of(
            new QReportDataSource()
               .withName("persons")
               .withSourceTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
               .withQueryFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("lastName", QCriteriaOperator.STARTS_WITH, List.of("K")))
                  .withCriteria(new QFilterCriteria("birthDate", QCriteriaOperator.BETWEEN, List.of("${input.startDate}", "${input.endDate}")))
               )
         ))
         .withInputFields(List.of(
            new QFieldMetaData("startDate", QFieldType.DATE_TIME),
            new QFieldMetaData("endDate", QFieldType.DATE_TIME)
         ))
         .withViews(List.of(
            new QReportView()
               .withName("pivot")
               .withLabel("pivot")
               .withDataSourceName("persons")
               .withType(ReportType.SUMMARY)
               .withPivotFields(List.of("lastName"))
               .withIncludeTotalRow(includeTotalRow)
               .withTitleFormat("Number of shoes - people born between %s and %s - pivot on LastName, sort by Quantity, Revenue DESC")
               .withTitleFields(List.of("${input.startDate}", "${input.endDate}"))
               .withOrderByFields(List.of(new QFilterOrderBy("shoeCount"), new QFilterOrderBy("sumPrice", false)))
               .withColumns(List.of(
                  new QReportField().withName("reportStartDate").withLabel("Report Start Date").withFormula("${input.startDate}"),
                  new QReportField().withName("reportEndDate").withLabel("Report End Date").withFormula("${input.endDate}"),
                  new QReportField().withName("personCount").withLabel("Person Count").withFormula("${pivot.count.id}").withDisplayFormat(DisplayFormat.COMMAS),
                  new QReportField().withName("shoeCount").withLabel("Quantity").withFormula("${pivot.sum.noOfShoes}").withDisplayFormat(DisplayFormat.COMMAS),
                  // new QReportField().withName("percentOfTotal").withLabel("% Total").withFormula("=MULTIPLY(100,DIVIDE(${pivot.sum.noOfShoes},${total.sum.noOfShoes}))").withDisplayFormat(DisplayFormat.PERCENT_POINT2),
                  new QReportField().withName("percentOfTotal").withLabel("% Total").withFormula("=DIVIDE(${pivot.sum.noOfShoes},${total.sum.noOfShoes})").withDisplayFormat(DisplayFormat.PERCENT_POINT2),
                  new QReportField().withName("sumCost").withLabel("Cost").withFormula("${pivot.sum.cost}").withDisplayFormat(DisplayFormat.CURRENCY),
                  new QReportField().withName("sumPrice").withLabel("Revenue").withFormula("${pivot.sum.price}").withDisplayFormat(DisplayFormat.CURRENCY),
                  new QReportField().withName("profit").withLabel("Profit").withFormula("=MINUS(${pivot.sum.price},${pivot.sum.cost})").withDisplayFormat(DisplayFormat.CURRENCY),
                  // new QReportField().withName("margin").withLabel("Margins").withFormula("=SCALE(MULTIPLY(100,DIVIDE(MINUS(${pivot.sum.price},${pivot.sum.cost}),${pivot.sum.price})),0)").withDisplayFormat(DisplayFormat.PERCENT),
                  new QReportField().withName("margin").withLabel("Margins").withFormula("=SCALE(DIVIDE(MINUS(${pivot.sum.price},${pivot.sum.cost}),${pivot.sum.price}),2)").withDisplayFormat(DisplayFormat.PERCENT),
                  new QReportField().withName("costPerShoe").withLabel("Cost Per").withFormula("=DIVIDE_SCALE(${pivot.sum.cost},${pivot.sum.noOfShoes},2)").withDisplayFormat(DisplayFormat.CURRENCY),
                  new QReportField().withName("revenuePerShoe").withLabel("Revenue Per").withFormula("=DIVIDE_SCALE(${pivot.sum.price},${pivot.sum.noOfShoes},2)").withDisplayFormat(DisplayFormat.CURRENCY),
                  new QReportField().withName("marginPer").withLabel("Margin Per").withFormula("=MINUS(DIVIDE_SCALE(${pivot.sum.price},${pivot.sum.noOfShoes},2),DIVIDE_SCALE(${pivot.sum.cost},${pivot.sum.noOfShoes},2))").withDisplayFormat(DisplayFormat.CURRENCY)
               ))
         ));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableOnlyReport() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      QReportMetaData report = new QReportMetaData()
         .withName(REPORT_NAME)
         .withDataSources(List.of(
            new QReportDataSource()
               .withName("persons")
               .withSourceTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
               .withQueryFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("birthDate", QCriteriaOperator.GREATER_THAN, List.of("${input.startDate}")))
               )
         ))
         .withInputFields(List.of(
            new QFieldMetaData("startDate", QFieldType.DATE_TIME)
         ))
         .withViews(List.of(
            new QReportView()
               .withName("table1")
               .withLabel("table1")
               .withDataSourceName("persons")
               .withType(ReportType.TABLE)
               .withColumns(List.of(
                  new QReportField().withName("id"),
                  new QReportField().withName("firstName"),
                  new QReportField().withName("lastName")
               ))
         ));

      qInstance.addReport(report);

      insertPersonRecords(qInstance);
      runReport(qInstance, Map.of("startDate", LocalDate.of(1980, Month.JANUARY, 1)));

      List<Map<String, String>>     list     = ListOfMapsExportStreamer.getList("table1");
      Iterator<Map<String, String>> iterator = list.iterator();
      Map<String, String>           row      = iterator.next();
      assertEquals(5, list.size());
      assertThat(row).containsOnlyKeys("Id", "First Name", "Last Name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTwoTableViewsOneDataSourceReport() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      QReportMetaData report = new QReportMetaData()
         .withName(REPORT_NAME)
         .withDataSources(List.of(
            new QReportDataSource()
               .withName("persons")
               .withSourceTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
               .withQueryFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("birthDate", QCriteriaOperator.GREATER_THAN, List.of("${input.startDate}")))
               )
         ))
         .withInputFields(List.of(
            new QFieldMetaData("startDate", QFieldType.DATE_TIME)
         ))
         .withViews(List.of(
            new QReportView()
               .withName("table1")
               .withLabel("table1")
               .withDataSourceName("persons")
               .withType(ReportType.TABLE)
               .withColumns(List.of(
                  new QReportField().withName("id"),
                  new QReportField().withName("firstName"),
                  new QReportField().withName("lastName")
               )),
            new QReportView()
               .withName("table2")
               .withLabel("table2")
               .withDataSourceName("persons")
               .withType(ReportType.TABLE)
               .withColumns(List.of(
                  new QReportField().withName("birthDate")
               ))
         ));

      qInstance.addReport(report);

      insertPersonRecords(qInstance);
      runReport(qInstance, Map.of("startDate", LocalDate.of(1980, Month.JANUARY, 1)));

      List<Map<String, String>>     list     = ListOfMapsExportStreamer.getList("table1");
      Iterator<Map<String, String>> iterator = list.iterator();
      Map<String, String>           row      = iterator.next();
      assertEquals(5, list.size());
      assertThat(row).containsOnlyKeys("Id", "First Name", "Last Name");

      list = ListOfMapsExportStreamer.getList("table2");
      iterator = list.iterator();
      row = iterator.next();
      assertEquals(5, list.size());
      assertThat(row).containsOnlyKeys("Birth Date");
   }

}