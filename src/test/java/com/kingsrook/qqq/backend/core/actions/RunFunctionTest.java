/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.callbacks.QProcessCallback;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunFunctionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      TestCallback callback = new TestCallback();
      RunFunctionRequest request = new RunFunctionRequest(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setProcessName("greet");
      request.setFunctionName("prepare");
      request.setCallback(callback);
      RunFunctionResult result = new RunFunctionAction().execute(request);
      assertNotNull(result);
      assertNull(result.getError());
      assertTrue(result.getRecords().stream().allMatch(r -> r.getValues().containsKey("mockValue")), "records should have a mock value");
      assertTrue(result.getValues().containsKey("mockValue"), "result object should have a mock value");
      assertEquals("ABC", result.getValues().get("greetingPrefix"), "result object should have value from our callback");
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
         for(QFieldMetaData field : fields)
         {
            rs.put(field.getName(), switch(field.getType())
               {
                  case STRING -> "ABC";
                  case INTEGER -> 42;
                  case DECIMAL -> new BigDecimal("47");
                  case DATE, DATE_TIME -> null;
                  case TEXT -> """
                     ABC
                     XYZ""";
                  case HTML -> "<b>Oh my</b>";
                  case PASSWORD -> "myPa**word";
               });
         }
         return (rs);
      }
   }
}
