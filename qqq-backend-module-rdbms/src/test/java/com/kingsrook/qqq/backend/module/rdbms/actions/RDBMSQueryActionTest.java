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

package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSQueryActionTest extends RDBMSActionTest
{

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
   public void testUnfilteredQuery() throws QException
   {
      QueryInput  queryInput  = initQueryRequest();
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size(), "Unfiltered query should find all rows");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testEqualsQuery() throws QException
   {
      String email = "darin.kelkhoff@gmail.com";

      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.EQUALS)
            .withValues(List.of(email)))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
      assertEquals(email, queryOutput.getRecords().get(0).getValueString("email"), "Should find expected email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotEqualsQuery() throws QException
   {
      String email = "darin.kelkhoff@gmail.com";

      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_EQUALS)
            .withValues(List.of(email)))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueString("email").equals(email)), "Should NOT find expected email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testInQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.IN)
            .withValues(List.of(2, 4)))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(2) || r.getValueInteger("id").equals(4)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotInQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.NOT_IN)
            .withValues(List.of(2, 3, 4)))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testStartsWith() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.STARTS_WITH)
            .withValues(List.of("darin")))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueString("email").matches("darin.*")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testContains() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.CONTAINS)
            .withValues(List.of("kelkhoff")))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueString("email").matches(".*kelkhoff.*")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testEndsWith() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.ENDS_WITH)
            .withValues(List.of("gmail.com")))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueString("email").matches(".*gmail.com")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotStartsWith() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_STARTS_WITH)
            .withValues(List.of("darin")))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueString("email").matches("darin.*")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotContains() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_CONTAINS)
            .withValues(List.of("kelkhoff")))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueString("email").matches(".*kelkhoff.*")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotEndsWith() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_ENDS_WITH)
            .withValues(List.of("gmail.com")))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueString("email").matches(".*gmail.com")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testLessThanQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.LESS_THAN)
            .withValues(List.of(3)))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(2)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testLessThanOrEqualsQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.LESS_THAN_OR_EQUALS)
            .withValues(List.of(2)))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(2)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGreaterThanQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.GREATER_THAN)
            .withValues(List.of(3)))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(4) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGreaterThanOrEqualsQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.GREATER_THAN_OR_EQUALS)
            .withValues(List.of(4)))
      );
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(4) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testIsBlankQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("birthDate")
            .withOperator(QCriteriaOperator.IS_BLANK)
         ));
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValue("birthDate") == null), "Should find expected row");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testIsNotBlankQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("firstName")
            .withOperator(QCriteriaOperator.IS_NOT_BLANK)
         ));
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValue("firstName") != null), "Should find expected rows");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testBetweenQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.BETWEEN)
            .withValues(List.of(2, 4))
         ));
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(3, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(2) || r.getValueInteger("id").equals(3) || r.getValueInteger("id").equals(4)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotBetweenQuery() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.NOT_BETWEEN)
            .withValues(List.of(2, 4))
         ));
      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QueryInput initQueryRequest()
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setInstance(TestUtils.defineInstance());
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      queryInput.setSession(new QSession());
      return queryInput;
   }



   /*******************************************************************************
    ** This doesn't really test any RDBMS code, but is a checkpoint that the core
    ** module is populating displayValues when it performs the system-level query action
    ** (if so requested by input field).
    *******************************************************************************/
   @Test
   public void testThatDisplayValuesGetSetGoingThroughQueryAction() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setShouldGenerateDisplayValues(true);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size(), "Unfiltered query should find all rows");

      for(QRecord record : queryOutput.getRecords())
      {
         assertThat(record.getValues()).isNotEmpty();
         assertThat(record.getDisplayValues()).isNotEmpty();
         assertThat(record.getErrors()).isEmpty();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLookInsideTransaction() throws QException
   {
      InsertInput insertInput = new InsertInput(TestUtils.defineInstance());
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON);

      InsertAction        insertAction = new InsertAction();
      QBackendTransaction transaction  = insertAction.openTransaction(insertInput);

      insertInput.setTransaction(transaction);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "George").withValue("lastName", "Washington").withValue("email", "gw@kingsrook.com")
      ));

      insertAction.execute(insertInput);

      QueryInput  queryInput  = initQueryRequest();
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size(), "Query without the transaction should not see the new row.");

      queryInput = initQueryRequest();
      queryInput.setTransaction(transaction);
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(6, queryOutput.getRecords().size(), "Query with the transaction should see the new row.");

      transaction.rollback();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEmptyInList() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria("firstName", QCriteriaOperator.IN, List.of())));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(0, queryOutput.getRecords().size(), "IN empty list should find nothing.");

      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria("firstName", QCriteriaOperator.NOT_IN, List.of())));
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size(), "NOT_IN empty list should find everything.");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOr() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withBooleanOperator(QQueryFilter.BooleanOperator.OR)
         .withCriteria(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, List.of("Darin")))
         .withCriteria(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, List.of("Tim")))
      );
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "OR should find 2 rows");
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Darin"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Tim"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNestedFilterAndOrOr() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withBooleanOperator(QQueryFilter.BooleanOperator.OR)
         .withSubFilters(List.of(
            new QQueryFilter()
               .withBooleanOperator(QQueryFilter.BooleanOperator.AND)
               .withCriteria(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, List.of("James")))
               .withCriteria(new QFilterCriteria("lastName", QCriteriaOperator.EQUALS, List.of("Maes"))),
            new QQueryFilter()
               .withBooleanOperator(QQueryFilter.BooleanOperator.AND)
               .withCriteria(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, List.of("Darin")))
               .withCriteria(new QFilterCriteria("lastName", QCriteriaOperator.EQUALS, List.of("Kelkhoff")))
         ))
      );
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Complex query should find 2 rows");
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("James") && r.getValueString("lastName").equals("Maes"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Darin") && r.getValueString("lastName").equals("Kelkhoff"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNestedFilterOrAndAnd() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withBooleanOperator(QQueryFilter.BooleanOperator.AND)
         .withSubFilters(List.of(
            new QQueryFilter()
               .withBooleanOperator(QQueryFilter.BooleanOperator.OR)
               .withCriteria(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, List.of("James")))
               .withCriteria(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, List.of("Tim"))),
            new QQueryFilter()
               .withBooleanOperator(QQueryFilter.BooleanOperator.OR)
               .withCriteria(new QFilterCriteria("lastName", QCriteriaOperator.EQUALS, List.of("Kelkhoff")))
               .withCriteria(new QFilterCriteria("lastName", QCriteriaOperator.EQUALS, List.of("Chamberlain")))
         ))
      );
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "Complex query should find 1 row");
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Tim") && r.getValueString("lastName").equals("Chamberlain"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOneToOneInnerJoinWithoutWhere() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withSelect(true));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(3, queryOutput.getRecords().size(), "Join query should find 3 rows");
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Darin") && r.getValueString("personalIdCard.idNumber").equals("19800531"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("James") && r.getValueString("personalIdCard.idNumber").equals("19800515"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Tim") && r.getValueString("personalIdCard.idNumber").equals("19760528"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOneToOneLeftJoinWithoutWhere() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withType(QueryJoin.Type.LEFT).withSelect(true));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size(), "Left Join query should find 5 rows");
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Darin") && r.getValueString("personalIdCard.idNumber").equals("19800531"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("James") && r.getValueString("personalIdCard.idNumber").equals("19800515"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Tim") && r.getValueString("personalIdCard.idNumber").equals("19760528"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Garret") && r.getValue("personalIdCard.idNumber") == null);
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Tyler") && r.getValue("personalIdCard.idNumber") == null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOneToOneRightJoinWithoutWhere() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withType(QueryJoin.Type.RIGHT).withSelect(true));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(6, queryOutput.getRecords().size(), "Right Join query should find 6 rows");
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Darin") && r.getValueString("personalIdCard.idNumber").equals("19800531"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("James") && r.getValueString("personalIdCard.idNumber").equals("19800515"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Tim") && r.getValueString("personalIdCard.idNumber").equals("19760528"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValue("firstName") == null && r.getValueString("personalIdCard.idNumber").equals("123123123"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValue("firstName") == null && r.getValueString("personalIdCard.idNumber").equals("987987987"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValue("firstName") == null && r.getValueString("personalIdCard.idNumber").equals("456456456"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOneToOneInnerJoinWithWhere() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withSelect(true));
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria(TestUtils.TABLE_NAME_PERSONAL_ID_CARD + ".idNumber", QCriteriaOperator.STARTS_WITH, "1980")));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Join query should find 2 rows");
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("Darin") && r.getValueString("personalIdCard.idNumber").equals("19800531"));
      assertThat(queryOutput.getRecords()).anyMatch(r -> r.getValueString("firstName").equals("James") && r.getValueString("personalIdCard.idNumber").equals("19800515"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOneToOneInnerJoinWithOrderBy() throws QException
   {
      QInstance  qInstance  = TestUtils.defineInstance();
      QueryInput queryInput = initQueryRequest();
      queryInput.withQueryJoin(new QueryJoin(qInstance.getJoin(TestUtils.TABLE_NAME_PERSON + "Join" + StringUtils.ucFirst(TestUtils.TABLE_NAME_PERSONAL_ID_CARD))).withSelect(true));
      queryInput.setFilter(new QQueryFilter().withOrderBy(new QFilterOrderBy(TestUtils.TABLE_NAME_PERSONAL_ID_CARD + ".idNumber")));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(3, queryOutput.getRecords().size(), "Join query should find 3 rows");
      List<String> idNumberListFromQuery = queryOutput.getRecords().stream().map(r -> r.getValueString(TestUtils.TABLE_NAME_PERSONAL_ID_CARD + ".idNumber")).toList();
      assertEquals(List.of("19760528", "19800515", "19800531"), idNumberListFromQuery);

      /////////////////////////
      // repeat, sorted desc //
      /////////////////////////
      queryInput.setFilter(new QQueryFilter().withOrderBy(new QFilterOrderBy(TestUtils.TABLE_NAME_PERSONAL_ID_CARD + ".idNumber", false)));
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(3, queryOutput.getRecords().size(), "Join query should find 3 rows");
      idNumberListFromQuery = queryOutput.getRecords().stream().map(r -> r.getValueString(TestUtils.TABLE_NAME_PERSONAL_ID_CARD + ".idNumber")).toList();
      assertEquals(List.of("19800531", "19800515", "19760528"), idNumberListFromQuery);
   }



   /*******************************************************************************
    ** In the prime data, we've got 1 order line set up with an item from a different
    ** store than its order.  Write a query to find such a case.
    *******************************************************************************/
   @Test
   void testFiveTableOmsJoinFindMismatchedStoreId() throws Exception
   {
      QueryInput queryInput = new QueryInput(TestUtils.defineInstance(), new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_ORDER, TestUtils.TABLE_NAME_STORE).withAlias("orderStore").withSelect(true));
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_ORDER, TestUtils.TABLE_NAME_ORDER_LINE).withSelect(true));
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_ORDER_LINE, TestUtils.TABLE_NAME_ITEM).withSelect(true));
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_ITEM, TestUtils.TABLE_NAME_STORE).withAlias("itemStore").withSelect(true));

      queryInput.setFilter(new QQueryFilter(new QFilterCriteria().withFieldName("orderStore.id").withOperator(QCriteriaOperator.NOT_EQUALS).withOtherFieldName("item.storeId")));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "# of rows found by query");
      QRecord qRecord = queryOutput.getRecords().get(0);
      assertEquals(2, qRecord.getValueInteger("id"));
      assertEquals(1, qRecord.getValueInteger("orderStore.id"));
      assertEquals(2, qRecord.getValueInteger("itemStore.id"));

      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      // run the same setup, but this time, use the other-field-name as itemStore.id, instead of item.storeId //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria().withFieldName("orderStore.id").withOperator(QCriteriaOperator.NOT_EQUALS).withOtherFieldName("itemStore.id")));
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "# of rows found by query");
      qRecord = queryOutput.getRecords().get(0);
      assertEquals(2, qRecord.getValueInteger("id"));
      assertEquals(1, qRecord.getValueInteger("orderStore.id"));
      assertEquals(2, qRecord.getValueInteger("itemStore.id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOmsQueryByOrderLines() throws Exception
   {
      AtomicInteger orderLineCount = new AtomicInteger();
      runTestSql("SELECT COUNT(*) from order_line", (rs) ->
      {
         rs.next();
         orderLineCount.set(rs.getInt(1));
      });

      QueryInput queryInput = new QueryInput(TestUtils.defineInstance(), new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_ORDER_LINE);
      queryInput.withQueryJoin(new QueryJoin(TestUtils.TABLE_NAME_ORDER).withSelect(true));

      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(orderLineCount.get(), queryOutput.getRecords().size(), "# of rows found by query");
      assertEquals(3, queryOutput.getRecords().stream().filter(r -> r.getValueInteger("order.id").equals(1)).count());
      assertEquals(1, queryOutput.getRecords().stream().filter(r -> r.getValueInteger("order.id").equals(2)).count());
      assertEquals(1, queryOutput.getRecords().stream().filter(r -> r.getValueInteger("orderId").equals(3)).count());
      assertEquals(2, queryOutput.getRecords().stream().filter(r -> r.getValueInteger("orderId").equals(4)).count());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOmsQueryByPersons() throws Exception
   {
      QInstance  instance   = TestUtils.defineInstance();
      QueryInput queryInput = new QueryInput(instance, new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_ORDER);

      /////////////////////////////////////////////////////
      // inner join on bill-to person should find 6 rows //
      /////////////////////////////////////////////////////
      queryInput.withQueryJoins(List.of(new QueryJoin(TestUtils.TABLE_NAME_PERSON).withJoinMetaData(instance.getJoin("orderJoinBillToPerson")).withSelect(true)));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(6, queryOutput.getRecords().size(), "# of rows found by query");

      /////////////////////////////////////////////////////
      // inner join on ship-to person should find 7 rows //
      /////////////////////////////////////////////////////
      queryInput.withQueryJoins(List.of(new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withSelect(true)));
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(7, queryOutput.getRecords().size(), "# of rows found by query");

      /////////////////////////////////////////////////////////////////////////////
      // inner join on both bill-to person and ship-to person should find 5 rows //
      /////////////////////////////////////////////////////////////////////////////
      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withAlias("shipToPerson").withSelect(true),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withAlias("billToPerson").withSelect(true)
      ));
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size(), "# of rows found by query");

      /////////////////////////////////////////////////////////////////////////////
      // left join on both bill-to person and ship-to person should find 8 rows //
      /////////////////////////////////////////////////////////////////////////////
      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withType(QueryJoin.Type.LEFT).withAlias("shipToPerson").withSelect(true),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withType(QueryJoin.Type.LEFT).withAlias("billToPerson").withSelect(true)
      ));
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(8, queryOutput.getRecords().size(), "# of rows found by query");

      //////////////////////////////////////////////////
      // now join through to personalIdCard table too //
      //////////////////////////////////////////////////
      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withAlias("shipToPerson").withSelect(true),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withAlias("billToPerson").withSelect(true),
         new QueryJoin("billToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("billToIdCard").withSelect(true),
         new QueryJoin("shipToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("shipToIdCard").withSelect(true)
      ));
      queryInput.setFilter(new QQueryFilter()
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // look for billToPersons w/ idNumber starting with 1980 - should only be James and Darin (assert on that below). //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         .withCriteria(new QFilterCriteria("billToIdCard.idNumber", QCriteriaOperator.STARTS_WITH, "1980"))
      );
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(3, queryOutput.getRecords().size(), "# of rows found by query");
      assertThat(queryOutput.getRecords().stream().map(r -> r.getValueString("billToPerson.firstName")).toList()).allMatch(p -> p.equals("Darin") || p.equals("James"));

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // ensure we throw if either of the ambiguous joins from person to id-card doesn't specify its left-table //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withAlias("shipToPerson").withSelect(true),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withAlias("billToPerson").withSelect(true),
         new QueryJoin(TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("billToIdCard").withSelect(true),
         new QueryJoin("shipToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("shipToIdCard").withSelect(true)
      ));
      assertThatThrownBy(() -> new QueryAction().execute(queryInput))
         .hasRootCauseMessage("Could not find a table or alias [personTable] in query.  May need to be more specific setting up QueryJoins.");

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // ensure we throw if either of the ambiguous joins from person to id-card doesn't specify its left-table //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withAlias("shipToPerson").withSelect(true),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withAlias("billToPerson").withSelect(true),
         new QueryJoin("billToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("billToIdCard").withSelect(true),
         new QueryJoin(TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("shipToIdCard").withSelect(true)
      ));
      assertThatThrownBy(() -> new QueryAction().execute(queryInput))
         .hasRootCauseMessage("Could not find a table or alias [personTable] in query.  May need to be more specific setting up QueryJoins.");

      ////////////////////////////////////////////////////////////////////////
      // ensure we throw if we have a bogus alias name given as a left-side //
      ////////////////////////////////////////////////////////////////////////
      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withAlias("shipToPerson").withSelect(true),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withAlias("billToPerson").withSelect(true),
         new QueryJoin("notATable", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("billToIdCard").withSelect(true),
         new QueryJoin("shipToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("shipToIdCard").withSelect(true)
      ));
      assertThatThrownBy(() -> new QueryAction().execute(queryInput))
         .hasRootCauseMessage("Could not find a join between tables [notATable][personalIdCard]");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOmsQueryByPersonsExtraKelkhoffOrder() throws Exception
   {
      QInstance  instance   = TestUtils.defineInstance();
      QueryInput queryInput = new QueryInput(instance, new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_ORDER);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // insert a second person w/ last name Kelkhoff, then an order for Darin Kelkhoff and this new Kelkhoff - //
      // then query for orders w/ bill to person & ship to person both lastname = Kelkhoff, but different ids.  //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      Integer specialOrderId = 1701;
      runTestSql("INSERT INTO person (id, first_name, last_name, email) VALUES (6, 'Jimmy', 'Kelkhoff', 'dk@gmail.com')", null);
      runTestSql("INSERT INTO `order` (id, store_id, bill_to_person_id, ship_to_person_id) VALUES (" + specialOrderId + ", 1, 1, 6)", null);
      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withType(QueryJoin.Type.LEFT).withAlias("shipToPerson").withSelect(true),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withType(QueryJoin.Type.LEFT).withAlias("billToPerson").withSelect(true)
      ));
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria().withFieldName("shipToPerson.lastName").withOperator(QCriteriaOperator.EQUALS).withOtherFieldName("billToPerson.lastName"))
         .withCriteria(new QFilterCriteria().withFieldName("shipToPerson.id").withOperator(QCriteriaOperator.NOT_EQUALS).withOtherFieldName("billToPerson.id"))
      );
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "# of rows found by query");
      assertEquals(specialOrderId, queryOutput.getRecords().get(0).getValueInteger("id"));

      ////////////////////////////////////////////////////////////
      // re-run that query using personIds from the order table //
      ////////////////////////////////////////////////////////////
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria().withFieldName("shipToPerson.lastName").withOperator(QCriteriaOperator.EQUALS).withOtherFieldName("billToPerson.lastName"))
         .withCriteria(new QFilterCriteria().withFieldName("order.shipToPersonId").withOperator(QCriteriaOperator.NOT_EQUALS).withOtherFieldName("order.billToPersonId"))
      );
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "# of rows found by query");
      assertEquals(specialOrderId, queryOutput.getRecords().get(0).getValueInteger("id"));

      ///////////////////////////////////////////////////////////////////////////////////////////////
      // re-run that query using personIds from the order table, but not specifying the table name //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria().withFieldName("shipToPerson.lastName").withOperator(QCriteriaOperator.EQUALS).withOtherFieldName("billToPerson.lastName"))
         .withCriteria(new QFilterCriteria().withFieldName("shipToPersonId").withOperator(QCriteriaOperator.NOT_EQUALS).withOtherFieldName("billToPersonId"))
      );
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "# of rows found by query");
      assertEquals(specialOrderId, queryOutput.getRecords().get(0).getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDuplicateAliases()
   {
      QInstance  instance   = TestUtils.defineInstance();
      QueryInput queryInput = new QueryInput(instance, new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_ORDER);

      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withAlias("shipToPerson"),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withAlias("billToPerson"),
         new QueryJoin("billToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withSelect(true),
         new QueryJoin("shipToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withSelect(true) // w/o alias, should get exception here - dupe table.
      ));
      assertThatThrownBy(() -> new QueryAction().execute(queryInput))
         .hasRootCauseMessage("Duplicate table name or alias: personalIdCard");

      queryInput.withQueryJoins(List.of(
         new QueryJoin(instance.getJoin("orderJoinShipToPerson")).withAlias("shipToPerson"),
         new QueryJoin(instance.getJoin("orderJoinBillToPerson")).withAlias("billToPerson"),
         new QueryJoin("shipToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("shipToPerson").withSelect(true), // dupe alias, should get exception here
         new QueryJoin("billToPerson", TestUtils.TABLE_NAME_PERSONAL_ID_CARD).withAlias("billToPerson").withSelect(true)
      ));
      assertThatThrownBy(() -> new QueryAction().execute(queryInput))
         .hasRootCauseMessage("Duplicate table name or alias: shipToPerson");
   }

}