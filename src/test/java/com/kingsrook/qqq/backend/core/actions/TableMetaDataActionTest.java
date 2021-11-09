package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.TableMetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.TableMetaDataResult;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 **
 *******************************************************************************/
class TableMetaDataActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      TableMetaDataRequest request = new TableMetaDataRequest(TestUtils.defineInstance());
      request.setTableName("person");
      TableMetaDataResult result = new TableMetaDataAction().execute(request);
      assertNotNull(result);
      assertNotNull(result.getTable());
      assertEquals("person", result.getTable().getName());
      assertEquals("Person", result.getTable().getLabel());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_notFound()
   {
      assertThrows(QUserFacingException.class, () -> {
         TableMetaDataRequest request = new TableMetaDataRequest(TestUtils.defineInstance());
         request.setTableName("willNotBeFound");
         new TableMetaDataAction().execute(request);
      });
   }

}