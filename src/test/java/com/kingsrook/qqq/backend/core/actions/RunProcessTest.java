/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.actions;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.callbacks.QProcessCallback;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunProcessTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      TestCallback callback = new TestCallback();
      RunProcessRequest request = new RunProcessRequest(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setProcessName("addToPeoplesAge");
      request.setCallback(callback);
      RunProcessResult result = new RunProcessAction().execute(request);
      assertNotNull(result);
      assertNull(result.getError());
      assertTrue(result.getRecords().stream().allMatch(r -> r.getValues().containsKey("age")), "records should have a value set by the process");
      assertTrue(result.getValues().containsKey("maxAge"), "process result object should have a value set by the first function in the process");
      assertTrue(result.getValues().containsKey("totalYearsAdded"), "process result object should have a value set by the second function in the process");
      assertTrue(callback.wasCalledForQueryFilter, "callback was used for query filter");
      assertTrue(callback.wasCalledForFieldValues, "callback was used for field values");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static class TestCallback implements QProcessCallback
   {
      private boolean wasCalledForQueryFilter = false;
      private boolean wasCalledForFieldValues = false;

      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public QQueryFilter getQueryFilter()
      {
         wasCalledForQueryFilter = true;
         return (new QQueryFilter());
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public Map<String, Serializable> getFieldValues(List<QFieldMetaData> fields)
      {
         wasCalledForFieldValues = true;
         Map<String, Serializable> rs = new HashMap<>();
         if (fields.stream().anyMatch(f -> f.getName().equals("yearsToAdd")))
         {
            rs.put("yearsToAdd", 42);
         }
         return (rs);
      }
   }
}
