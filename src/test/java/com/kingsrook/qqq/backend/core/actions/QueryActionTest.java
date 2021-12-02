package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.QueryResult;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for QueryAction
 **
 *******************************************************************************/
class QueryActionTest
{

   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      QueryRequest request = new QueryRequest(TestUtils.defineInstance());
      request.setTableName("person");
      QueryResult result = new QueryAction().execute(request);
      assertNotNull(result);
   }
}
