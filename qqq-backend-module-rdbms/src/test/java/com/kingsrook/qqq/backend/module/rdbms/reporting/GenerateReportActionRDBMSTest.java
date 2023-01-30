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

package com.kingsrook.qqq.backend.module.rdbms.reporting;


import java.io.ByteArrayOutputStream;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportDataSource;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportField;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.ReportType;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSActionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Do some tests on the qqq-backend-core GenerateReportAction, that are kinda
 ** hard to do in a backend that doesn't support joins, but that we can do in
 ** RDBMS.
 *******************************************************************************/
public class GenerateReportActionRDBMSTest extends RDBMSActionTest
{
   private static final String TEST_REPORT = "testReport";



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      super.primeTestDatabase();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTwoJoinsToSameTable() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      QReportMetaData report = new QReportMetaData()
         .withName(TEST_REPORT)
         .withDataSource(new QReportDataSource()
            .withSourceTable(TestUtils.TABLE_NAME_ORDER)
            .withQueryJoin(new QueryJoin(qInstance.getJoin("orderJoinBillToPerson")).withAlias("billToPerson").withType(QueryJoin.Type.LEFT).withSelect(true))
            .withQueryJoin(new QueryJoin(qInstance.getJoin("orderJoinShipToPerson")).withAlias("shipToPerson").withType(QueryJoin.Type.LEFT).withSelect(true))
         )
         .withView(new QReportView()
            .withType(ReportType.TABLE)
            .withColumn(new QReportField("id"))
            .withColumn(new QReportField("storeId").withLabel("Store Id"))
            .withColumn(new QReportField("storeName").withShowPossibleValueLabel(true).withSourceFieldName("storeId").withLabel("Store Name"))
            .withColumn(new QReportField("billToPerson.id"))
            .withColumn(new QReportField("billToPerson.firstName").withLabel("Bill To First Name"))
            .withColumn(new QReportField("billToPersonName").withShowPossibleValueLabel(true).withSourceFieldName("billToPersonId"))
            .withColumn(new QReportField("shipToPerson.id"))
            .withColumn(new QReportField("shipToPerson.firstName").withLabel("Ship To First Name"))
            .withColumn(new QReportField("shipToPersonName").withShowPossibleValueLabel(true).withSourceFieldName("billToPersonId"))
         );
      qInstance.addReport(report);
      reInitInstanceInContext(qInstance);

      String csv = runReport(qInstance);
      // System.out.println(csv);

      assertEquals("""
         "Id","Store Id","Store Name","Id","Bill To First Name","Bill To Person","Id","Ship To First Name","Bill To Person"
         "1","1","Q-Mart","1","Darin","Darin Kelkhoff","1","Darin","Darin Kelkhoff"
         "2","1","Q-Mart","1","Darin","Darin Kelkhoff","2","James","Darin Kelkhoff"
         "3","1","Q-Mart","2","James","James Maes","3","Tim","James Maes"
         "4","2","QQQ 'R' Us","4","Tyler","Tyler Samples","5","Garret","Tyler Samples"
         "5","2","QQQ 'R' Us","5","Garret","Garret Richardson","4","Tyler","Garret Richardson"
         "6","3","QDepot","5","Garret","Garret Richardson","","","Garret Richardson"
         "7","3","QDepot","","","","5","Garret",""
         "8","3","QDepot","","","","5","Garret",""
         """, csv);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTwoTablesWithSamePossibleValue() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      QReportMetaData report = new QReportMetaData()
         .withName(TEST_REPORT)
         .withDataSource(new QReportDataSource()
            .withSourceTable(TestUtils.TABLE_NAME_ORDER_LINE)
            .withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_ORDER).withSelect(true))
            .withQueryFilter(new QQueryFilter(new QFilterCriteria("storeId", QCriteriaOperator.NOT_EQUALS).withOtherFieldName("order.storeId")))
         )
         .withView(new QReportView()
            .withType(ReportType.TABLE)
            .withColumn(new QReportField("storeId").withLabel("Line Item Store Id"))
            .withColumn(new QReportField("storeName").withShowPossibleValueLabel(true).withSourceFieldName("storeId").withLabel("Line Item Store Name"))
            .withColumn(new QReportField("order.storeId").withLabel("Order Store Id"))
            .withColumn(new QReportField("order.storeName").withShowPossibleValueLabel(true).withSourceFieldName("order.storeId").withLabel("Order Store Name"))
         );
      qInstance.addReport(report);
      reInitInstanceInContext(qInstance);

      String csv = runReport(qInstance);
      // System.out.println(csv);

      assertEquals("""
         "Line Item Store Id","Line Item Store Name","Order Store Id","Order Store Name"
         "2","QQQ 'R' Us","1","Q-Mart"
         """, csv);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueThroughAlias() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      QReportMetaData report = new QReportMetaData()
         .withName(TEST_REPORT)
         .withDataSource(new QReportDataSource()
            .withSourceTable(TestUtils.TABLE_NAME_ORDER_LINE)
            .withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_ITEM).withAlias("i").withSelect(true))
         )
         .withView(new QReportView()
            .withType(ReportType.TABLE)
            .withColumn(new QReportField("id").withLabel("Line Item Id"))
            .withColumn(new QReportField("sku").withLabel("Item SKU"))
            .withColumn(new QReportField("i.storeId").withLabel("Item Store Id"))
            .withColumn(new QReportField("i.storeName").withShowPossibleValueLabel(true).withSourceFieldName("i.storeId").withLabel("Item Store Name"))
         );
      qInstance.addReport(report);
      reInitInstanceInContext(qInstance);

      String csv = runReport(qInstance);
      System.out.println(csv);

      assertEquals("""
         "Line Item Id","Item SKU","Item Store Id","Item Store Name"
         "1","QM-1","1","Q-Mart"
         "5","QM-1","1","Q-Mart"
         "2","QM-2","1","Q-Mart"
         "3","QM-3","1","Q-Mart"
         "4","QRU-1","2","QQQ 'R' Us"
         "6","QRU-1","2","QQQ 'R' Us"
         "8","QRU-1","2","QQQ 'R' Us"
         "7","QRU-2","2","QQQ 'R' Us"
         "9","QD-1","3","QDepot"
         "10","QD-1","3","QDepot"
         "11","QD-1","3","QDepot"
         """, csv);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String runReport(QInstance qInstance) throws QException
   {
      ReportInput reportInput = new ReportInput();
      QContext.setQSession(new QSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_STORE_ALL_ACCESS, true));
      reportInput.setReportName(TEST_REPORT);
      reportInput.setReportFormat(ReportFormat.CSV);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      reportInput.setReportOutputStream(outputStream);
      new GenerateReportAction().execute(reportInput);
      return (outputStream.toString());
   }

}
