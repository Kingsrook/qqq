/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.table.TableMetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.metadata.table.TableMetaDataResult;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit test for TableMetaDataAction
 **
 *******************************************************************************/
class TableMetaDataActionTest
{

   /*******************************************************************************
    ** Test basic success case.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      TableMetaDataRequest request = new TableMetaDataRequest(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setTableName("person");
      TableMetaDataResult result = new TableMetaDataAction().execute(request);
      assertNotNull(result);
      assertNotNull(result.getTable());
      assertEquals("person", result.getTable().getName());
      assertEquals("Person", result.getTable().getLabel());
   }



   /*******************************************************************************
    ** Test exeption is thrown for the "not-found" case.
    **
    *******************************************************************************/
   @Test
   public void test_notFound()
   {
      assertThrows(QUserFacingException.class, () -> {
         TableMetaDataRequest request = new TableMetaDataRequest(TestUtils.defineInstance());
         request.setSession(TestUtils.getMockSession());
         request.setTableName("willNotBeFound");
         new TableMetaDataAction().execute(request);
      });
   }

}
