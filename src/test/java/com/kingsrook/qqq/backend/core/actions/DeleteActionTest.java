package com.kingsrook.qqq.backend.core.actions;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.DeleteResult;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for DeleteAction
 **
 *******************************************************************************/
class DeleteActionTest
{

   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      DeleteRequest request = new DeleteRequest(TestUtils.defineInstance());
      request.setTableName("person");
      request.setPrimaryKeys(List.of(1, 2));
      DeleteResult result = new DeleteAction().execute(request);
      assertNotNull(result);
      assertEquals(2, result.getRecords().size());
      assertTrue(result.getRecords().stream().allMatch(r -> r.getErrors() == null));
   }

}
