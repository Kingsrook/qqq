/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for UpdateAction
 **
 *******************************************************************************/
class UpdateActionTest
{

   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      UpdateRequest request = new UpdateRequest(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setTableName("person");
      List<QRecord> records =new ArrayList<>();
      QRecord record = new QRecord();
      record.setValue("id", "47");
      record.setValue("firstName", "James");
      records.add(record);
      request.setRecords(records);
      UpdateResult result = new UpdateAction().execute(request);
      assertNotNull(result);
   }

}
