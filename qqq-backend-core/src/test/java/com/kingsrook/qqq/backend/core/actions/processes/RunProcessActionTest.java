/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReferenceLambda;
import com.kingsrook.qqq.backend.core.model.metadata.processes.ProcessStepFlow;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStateMachineStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for RunProcessAction 
 *******************************************************************************/
class RunProcessActionTest extends BaseTest
{
   private static List<String> log = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach()
   {
      log.clear();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStateMachineTwoBackendSteps() throws QException
   {
      QProcessMetaData process = new QProcessMetaData().withName("test")

         /////////////////////////////////////////////////////////////////
         // two-steps - a, points at b; b has no next-step, so it exits //
         /////////////////////////////////////////////////////////////////
         .addStep(QStateMachineStep.backendOnly("a", new QBackendStepMetaData().withName("aBackend")
            .withCode(new QCodeReferenceLambda<BackendStep>((runBackendStepInput, runBackendStepOutput) ->
            {
               log.add("in StepA");
               runBackendStepOutput.getProcessState().setNextStepName("b");
            }))))

         .addStep(QStateMachineStep.backendOnly("b", new QBackendStepMetaData().withName("bBackend")
            .withCode(new QCodeReferenceLambda<BackendStep>((runBackendStepInput, runBackendStepOutput) ->
            {
               log.add("in StepB");
            }))))

         .withStepFlow(ProcessStepFlow.STATE_MACHINE);

      QContext.getQInstance().addProcess(process);

      RunProcessInput input = new RunProcessInput();
      input.setProcessName("test");
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);

      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);
      assertEquals(List.of("in StepA", "in StepB"), log);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStateMachineTwoFrontendOnlySteps() throws QException
   {
      QProcessMetaData process = new QProcessMetaData().withName("test")

         .addStep(QStateMachineStep.frontendOnly("a", new QFrontendStepMetaData().withName("aFrontend")).withDefaultNextStepName("b"))
         .addStep(QStateMachineStep.frontendOnly("b", new QFrontendStepMetaData().withName("bFrontend")))

         .withStepFlow(ProcessStepFlow.STATE_MACHINE);

      QContext.getQInstance().addProcess(process);

      RunProcessInput input = new RunProcessInput();
      input.setProcessName("test");
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);

      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);
      assertThat(runProcessOutput.getProcessState().getNextStepName())
         .isPresent().get()
         .isEqualTo("aFrontend");

      /////////////////////////////
      // resume after a, go to b //
      /////////////////////////////
      input.setStartAfterStep("aFrontend");
      runProcessOutput = new RunProcessAction().execute(input);
      assertThat(runProcessOutput.getProcessState().getNextStepName())
         .isPresent().get()
         .isEqualTo("bFrontend");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStateMachineOneBackendStepReferencingItselfDoesNotInfiniteLoop() throws QException
   {
      QProcessMetaData process = new QProcessMetaData().withName("test")

         ///////////////////////////////////////////////////////////////
         // set up step that always points back at itself.            //
         // since it never goes to the frontend, it'll stack overflow //
         // (though we'll catch it ourselves before JVM does)         //
         ///////////////////////////////////////////////////////////////
         .addStep(QStateMachineStep.backendOnly("a", new QBackendStepMetaData().withName("aBackend")
            .withCode(new QCodeReferenceLambda<BackendStep>((runBackendStepInput, runBackendStepOutput) ->
            {
               log.add("in StepA");
               runBackendStepOutput.getProcessState().setNextStepName("a");
            }))))

         .withStepFlow(ProcessStepFlow.STATE_MACHINE);

      QContext.getQInstance().addProcess(process);

      RunProcessInput input = new RunProcessInput();
      input.setProcessName("test");
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);

      assertThatThrownBy(() -> new RunProcessAction().execute(input))
         .isInstanceOf(QException.class)
         .hasMessageContaining("maxStateMachineProcessStepFlowStackDepth of 20");

      ///////////////////////////////////////////////////
      // make sure we can set a custom max-stack-depth //
      ///////////////////////////////////////////////////
      input.addValue("maxStateMachineProcessStepFlowStackDepth", 5);
      assertThatThrownBy(() -> new RunProcessAction().execute(input))
         .isInstanceOf(QException.class)
         .hasMessageContaining("maxStateMachineProcessStepFlowStackDepth of 5");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStateMachineTwoBackendStepsReferencingEachOtherDoesNotInfiniteLoop() throws QException
   {
      QProcessMetaData process = new QProcessMetaData().withName("test")

         ///////////////////////////////////////////////////////////////
         // set up two steps that always points back at each other.   //
         // since it never goes to the frontend, it'll stack overflow //
         // (though we'll catch it ourselves before JVM does)         //
         ///////////////////////////////////////////////////////////////
         .addStep(QStateMachineStep.backendOnly("a", new QBackendStepMetaData().withName("aBackend")
            .withCode(new QCodeReferenceLambda<BackendStep>((runBackendStepInput, runBackendStepOutput) ->
            {
               log.add("in StepA");
               runBackendStepOutput.getProcessState().setNextStepName("b");
            }))))

         .addStep(QStateMachineStep.backendOnly("b", new QBackendStepMetaData().withName("bBackend")
            .withCode(new QCodeReferenceLambda<BackendStep>((runBackendStepInput, runBackendStepOutput) ->
            {
               log.add("in StepB");
               runBackendStepOutput.getProcessState().setNextStepName("a");
            }))))

         .withStepFlow(ProcessStepFlow.STATE_MACHINE);

      QContext.getQInstance().addProcess(process);

      RunProcessInput input = new RunProcessInput();
      input.setProcessName("test");
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);

      assertThatThrownBy(() -> new RunProcessAction().execute(input))
         .isInstanceOf(QException.class)
         .hasMessageContaining("maxStateMachineProcessStepFlowStackDepth of 20");

      ///////////////////////////////////////////////////
      // make sure we can set a custom max-stack-depth //
      ///////////////////////////////////////////////////
      input.addValue("maxStateMachineProcessStepFlowStackDepth", 5);
      assertThatThrownBy(() -> new RunProcessAction().execute(input))
         .isInstanceOf(QException.class)
         .hasMessageContaining("maxStateMachineProcessStepFlowStackDepth of 5");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStateSequenceOfFrontendAndBackendSteps() throws QException
   {
      QProcessMetaData process = new QProcessMetaData().withName("test")

         .addStep(QStateMachineStep.frontendThenBackend("a",
            new QFrontendStepMetaData().withName("aFrontend"),
            new QBackendStepMetaData().withName("aBackend")
               .withCode(new QCodeReferenceLambda<BackendStep>((runBackendStepInput, runBackendStepOutput) ->
               {
                  log.add("in StepA");
                  runBackendStepOutput.getProcessState().setNextStepName("b");
               }))))

         .addStep(QStateMachineStep.frontendThenBackend("b",
            new QFrontendStepMetaData().withName("bFrontend"),
            new QBackendStepMetaData().withName("bBackend")
               .withCode(new QCodeReferenceLambda<BackendStep>((runBackendStepInput, runBackendStepOutput) ->
               {
                  log.add("in StepB");
                  runBackendStepOutput.getProcessState().setNextStepName("c");
               }))))

         .addStep(QStateMachineStep.frontendThenBackend("c",
            new QFrontendStepMetaData().withName("cFrontend"),
            new QBackendStepMetaData().withName("cBackend")
               .withCode(new QCodeReferenceLambda<BackendStep>((runBackendStepInput, runBackendStepOutput) ->
               {
                  log.add("in StepC");
                  runBackendStepOutput.getProcessState().setNextStepName("d");
               }))))

         .addStep(QStateMachineStep.frontendOnly("d",
            new QFrontendStepMetaData().withName("dFrontend")))

         .withStepFlow(ProcessStepFlow.STATE_MACHINE);

      QContext.getQInstance().addProcess(process);

      RunProcessInput input = new RunProcessInput();
      input.setProcessName("test");
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);

      ////////////////////////////////////////////////////////
      // start the process - we should be sent to aFrontend //
      ////////////////////////////////////////////////////////
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);
      assertThat(runProcessOutput.getProcessState().getNextStepName())
         .isPresent().get()
         .isEqualTo("aFrontend");

      ///////////////////////////////////////////////////////////////////////////////////////////
      // resume after aFrontend - we should run StepA (backend), and then be sent to bFrontend //
      ///////////////////////////////////////////////////////////////////////////////////////////
      input.setStartAfterStep("aFrontend");
      runProcessOutput = new RunProcessAction().execute(input);
      assertEquals(List.of("in StepA"), log);
      assertThat(runProcessOutput.getProcessState().getNextStepName())
         .isPresent().get()
         .isEqualTo("bFrontend");

      ///////////////////////////////////////////////////////////////////////////////////////////
      // resume after bFrontend - we should run StepB (backend), and then be sent to cFrontend //
      ///////////////////////////////////////////////////////////////////////////////////////////
      input.setStartAfterStep("bFrontend");
      runProcessOutput = new RunProcessAction().execute(input);
      assertEquals(List.of("in StepA", "in StepB"), log);
      assertThat(runProcessOutput.getProcessState().getNextStepName())
         .isPresent().get()
         .isEqualTo("cFrontend");

      ///////////////////////////////////////////////////////////////////////////////////////////
      // resume after cFrontend - we should run StepC (backend), and then be sent to dFrontend //
      ///////////////////////////////////////////////////////////////////////////////////////////
      input.setStartAfterStep("cFrontend");
      runProcessOutput = new RunProcessAction().execute(input);
      assertEquals(List.of("in StepA", "in StepB", "in StepC"), log);
      assertThat(runProcessOutput.getProcessState().getNextStepName())
         .isPresent().get()
         .isEqualTo("dFrontend");

      ////////////////////////////////////////////////////////////////////////////////////
      // if we resume again here, we'll be past the end of the process, so no next-step //
      ////////////////////////////////////////////////////////////////////////////////////
      input.setStartAfterStep("dFrontend");
      runProcessOutput = new RunProcessAction().execute(input);
      assertEquals(List.of("in StepA", "in StepB", "in StepC"), log);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isEmpty();

   }

}