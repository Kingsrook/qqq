/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
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

package com.kingsrook.qqq.backend.core.actions.processes;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.processes.implementations.mock.MockBackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessState;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.state.StateType;
import com.kingsrook.qqq.backend.core.state.UUIDAndTypeStateKey;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


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
      TestCallback    callback = new TestCallback();
      RunProcessInput request  = new RunProcessInput(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setProcessName("addToPeoplesAge");
      request.setCallback(callback);
      RunProcessOutput result = new RunProcessAction().execute(request);
      assertNotNull(result);
      assertTrue(result.getRecords().stream().allMatch(r -> r.getValues().containsKey("age")), "records should have a value set by the process");
      assertTrue(result.getValues().containsKey("maxAge"), "process result object should have a value set by the first function in the process");
      assertTrue(result.getValues().containsKey("totalYearsAdded"), "process result object should have a value set by the second function in the process");
      assertTrue(callback.wasCalledForQueryFilter, "callback was used for query filter");
      assertTrue(callback.wasCalledForFieldValues, "callback was used for field values");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testBreakOnFrontendSteps() throws QException
   {
      TestCallback      callback    = new TestCallback();
      QInstance       instance    = TestUtils.defineInstance();
      RunProcessInput request     = new RunProcessInput(instance);
      String          processName = TestUtils.PROCESS_NAME_GREET_PEOPLE_INTERACTIVE;

      request.setSession(TestUtils.getMockSession());
      request.setProcessName(processName);
      request.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);
      request.setCallback(callback);
      RunProcessOutput result0 = new RunProcessAction().execute(request);

      assertNotNull(result0);

      ///////////////////////////////////////////////////////////////
      // make sure we were told that we broke at a (frontend) step //
      ///////////////////////////////////////////////////////////////
      Optional<String> breakingAtStep0 = result0.getProcessState().getNextStepName();
      assertTrue(breakingAtStep0.isPresent());
      assertInstanceOf(QFrontendStepMetaData.class, instance.getProcessStep(processName, breakingAtStep0.get()));
      assertNull(result0.getValues().get(MockBackendStep.FIELD_MOCK_VALUE));

      //////////////////////////////////////////////
      // now run again, proceeding from this step //
      //////////////////////////////////////////////
      request.setStartAfterStep(breakingAtStep0.get());
      RunProcessOutput result1 = new RunProcessAction().execute(request);

      ////////////////////////////////////////////////////////////////////
      // make sure we were told that we broke at the next frontend step //
      ////////////////////////////////////////////////////////////////////
      Optional<String> breakingAtStep1 = result1.getProcessState().getNextStepName();
      assertTrue(breakingAtStep1.isPresent());
      assertInstanceOf(QFrontendStepMetaData.class, instance.getProcessStep(processName, breakingAtStep1.get()));
      assertNotEquals(breakingAtStep0.get(), breakingAtStep1.get());
      assertEquals(MockBackendStep.MOCK_VALUE, result1.getValues().get(MockBackendStep.FIELD_MOCK_VALUE));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testSkipFrontendSteps() throws QException
   {
      TestCallback      callback    = new TestCallback();
      QInstance       instance    = TestUtils.defineInstance();
      RunProcessInput request     = new RunProcessInput(instance);
      String          processName = TestUtils.PROCESS_NAME_GREET_PEOPLE_INTERACTIVE;

      request.setSession(TestUtils.getMockSession());
      request.setProcessName(processName);
      request.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      request.setCallback(callback);

      RunProcessOutput runProcessOutput = new RunProcessAction().execute(request);
      assertTrue(runProcessOutput.getException().isEmpty());
      assertEquals(MockBackendStep.MOCK_VALUE, runProcessOutput.getValues().get(MockBackendStep.FIELD_MOCK_VALUE));
      assertTrue(runProcessOutput.getProcessState().getNextStepName().isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testFailOnFrontendSteps()
   {
      TestCallback      callback    = new TestCallback();
      QInstance       instance    = TestUtils.defineInstance();
      RunProcessInput request     = new RunProcessInput(instance);
      String          processName = TestUtils.PROCESS_NAME_GREET_PEOPLE_INTERACTIVE;

      request.setSession(TestUtils.getMockSession());
      request.setProcessName(processName);
      request.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.FAIL);
      request.setCallback(callback);

      try
      {
         new RunProcessAction().execute(request);
         fail("This should have thrown...");
      }
      catch(Exception e)
      {
         assertTrue(e.getMessage().contains("fail on frontend steps"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPrimeProcessRequestNewProcess() throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////
      // this is a flow where it's a new process - so, we should create a new state //
      ////////////////////////////////////////////////////////////////////////////////
      RunProcessInput     runProcessInput = new RunProcessInput();
      UUIDAndTypeStateKey stateKey        = new UUIDAndTypeStateKey(UUID.randomUUID(), StateType.PROCESS_STATUS);
      ProcessState        processState    = new RunProcessAction().primeProcessState(runProcessInput, stateKey);
      assertNotNull(processState);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPrimeProcessRequestAttemptToContinueButStateNotFound() throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////////
      // this is a flow where it's a continue, but we don't have a state stored, so it should throw //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setStartAfterStep("setupStep");
      UUIDAndTypeStateKey stateKey = new UUIDAndTypeStateKey(UUID.randomUUID(), StateType.PROCESS_STATUS);

      assertThrows(QException.class, () ->
      {
         new RunProcessAction().primeProcessState(runProcessInput, stateKey);
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPrimeProcessRequestAttemptToContinueAndStateIsFound() throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////////
      // this is a flow where it's a continue, but we don't have a state stored, so it should throw //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setStartAfterStep("setupStep");
      runProcessInput.addValue("foo", "bar");
      runProcessInput.addValue("alpha", "beta");
      UUIDAndTypeStateKey stateKey = new UUIDAndTypeStateKey(UUID.randomUUID(), StateType.PROCESS_STATUS);

      ////////////////////////////////////////////////
      // simulate the state being previously stored //
      ////////////////////////////////////////////////
      ProcessState oldProcessState = new ProcessState();
      oldProcessState.getValues().put("key", "myValue");
      oldProcessState.getValues().put("foo", "fubu");
      RunProcessAction.getStateProvider().put(stateKey, oldProcessState);

      ProcessState primedProcessState = new RunProcessAction().primeProcessState(runProcessInput, stateKey);
      assertEquals("myValue", primedProcessState.getValues().get("key"));

      /////////////////////////////////////////////////////////////////////////////////////////////
      // make sure values that were in the original request trump values that had been in state. //
      /////////////////////////////////////////////////////////////////////////////////////////////
      assertEquals("bar", primedProcessState.getValues().get("foo"));
      assertEquals("beta", primedProcessState.getValues().get("alpha"));
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
         if(fields.stream().anyMatch(f -> f.getName().equals("yearsToAdd")))
         {
            rs.put("yearsToAdd", 42);
         }
         return (rs);
      }
   }
}