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
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


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
      Assertions.assertEquals(5, queryOutput.getRecords().size(), "Unfiltered query should find all rows");
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
      Assertions.assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertEquals(email, queryOutput.getRecords().get(0).getValueString("email"), "Should find expected email address");
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
      Assertions.assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValue("birthDate") == null), "Should find expected row");
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
      Assertions.assertEquals(3, queryOutput.getRecords().size(), "Expected # of rows");
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
      Assertions.assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QueryInput initQueryRequest()
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setInstance(TestUtils.defineInstance());
      queryInput.setTableName(TestUtils.defineTablePerson().getName());
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
      QueryInput  queryInput  = initQueryRequest();
      queryInput.setShouldGenerateDisplayValues(true);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      Assertions.assertEquals(5, queryOutput.getRecords().size(), "Unfiltered query should find all rows");

      for(QRecord record : queryOutput.getRecords())
      {
         assertThat(record.getValues()).isNotEmpty();
         assertThat(record.getDisplayValues()).isNotEmpty();
         assertThat(record.getErrors()).isEmpty();
      }
   }

}