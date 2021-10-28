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
      Assertions.assertEquals(1, queryResult.getRecords().size(), "Equals query should find 1 row");
      Assertions.assertEquals(email, queryResult.getRecords().get(0).getValueString("email"), "Should find expected email address");
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