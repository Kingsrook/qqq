package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.QueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


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
      QueryRequest queryRequest = initQueryRequest();
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(5, queryResult.getRecords().size(), "Unfiltered query should find all rows");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testEqualsQuery() throws QException
   {
      String email = "darin.kelkhoff@gmail.com";

      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.EQUALS)
            .withValues(List.of(email)))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(1, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertEquals(email, queryResult.getRecords().get(0).getValueString("email"), "Should find expected email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotEqualsQuery() throws QException
   {
      String email = "darin.kelkhoff@gmail.com";

      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_EQUALS)
            .withValues(List.of(email)))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(4, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().noneMatch(r -> r.getValueString("email").equals(email)), "Should NOT find expected email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testInQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.IN)
            .withValues(List.of(2, 4)))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(2, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(2) || r.getValueInteger("id").equals(4)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotInQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.NOT_IN)
            .withValues(List.of(2, 3, 4)))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(2, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testStartsWith() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.STARTS_WITH)
            .withValues(List.of("darin")))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(1, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueString("email").matches("darin.*")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testContains() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.CONTAINS)
            .withValues(List.of("kelkhoff")))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(1, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueString("email").matches(".*kelkhoff.*")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testEndsWith() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.ENDS_WITH)
            .withValues(List.of("gmail.com")))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(1, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueString("email").matches(".*gmail.com")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotStartsWith() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_STARTS_WITH)
            .withValues(List.of("darin")))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(4, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().noneMatch(r -> r.getValueString("email").matches("darin.*")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotContains() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_CONTAINS)
            .withValues(List.of("kelkhoff")))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(4, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().noneMatch(r -> r.getValueString("email").matches(".*kelkhoff.*")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotEndsWith() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_ENDS_WITH)
            .withValues(List.of("gmail.com")))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(4, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().noneMatch(r -> r.getValueString("email").matches(".*gmail.com")), "Should find matching email address");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testLessThanQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.LESS_THAN)
            .withValues(List.of(3)))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(2, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(2)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testLessThanOrEqualsQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.LESS_THAN_OR_EQUALS)
            .withValues(List.of(2)))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(2, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(2)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGreaterThanQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.GREATER_THAN)
            .withValues(List.of(3)))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(2, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(4) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGreaterThanOrEqualsQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.GREATER_THAN_OR_EQUALS)
            .withValues(List.of(4)))
      );
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(2, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(4) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testIsBlankQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("birthDate")
            .withOperator(QCriteriaOperator.IS_BLANK)
         ));
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(1, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValue("birthDate") == null), "Should find expected row");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testBetweenQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.BETWEEN)
            .withValues(List.of(2, 4))
         ));
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(3, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(2) || r.getValueInteger("id").equals(3) || r.getValueInteger("id").equals(4)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotBetweenQuery() throws QException
   {
      QueryRequest queryRequest = initQueryRequest();
      queryRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.NOT_BETWEEN)
            .withValues(List.of(2, 4))
         ));
      QueryResult queryResult = new RDBMSQueryAction().execute(queryRequest);
      Assertions.assertEquals(2, queryResult.getRecords().size(), "Expected # of rows");
      Assertions.assertTrue(queryResult.getRecords().stream().allMatch(r -> r.getValueInteger("id").equals(1) || r.getValueInteger("id").equals(5)), "Should find expected ids");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QueryRequest initQueryRequest()
   {
      QueryRequest queryRequest = new QueryRequest();
      queryRequest.setInstance(defineInstance());
      queryRequest.setTableName(defineTablePerson().getName());
      return queryRequest;
   }

}