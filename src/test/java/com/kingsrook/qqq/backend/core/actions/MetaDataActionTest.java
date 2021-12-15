/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.MetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.MetaDataResult;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for MetaDataAction
 **
 *******************************************************************************/
class MetaDataActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      MetaDataRequest request = new MetaDataRequest(TestUtils.defineInstance());
      MetaDataResult result = new MetaDataAction().execute(request);
      assertNotNull(result);
      assertNotNull(result.getTables());
      assertNotNull(result.getTables().get("person"));
      assertEquals("person", result.getTables().get("person").getName());
      assertEquals("Person", result.getTables().get("person").getLabel());
   }
}
