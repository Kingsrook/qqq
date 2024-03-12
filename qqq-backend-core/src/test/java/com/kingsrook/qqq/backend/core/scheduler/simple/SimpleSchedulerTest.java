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

package com.kingsrook.qqq.backend.core.scheduler.simple;


import java.util.List;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ScheduleManager
 *******************************************************************************/
class SimpleSchedulerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      SimpleScheduler.resetSingleton();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStartAndStop()
   {
      QInstance       qInstance       = QContext.getQInstance();
      SimpleScheduler simpleScheduler = SimpleScheduler.getInstance(qInstance);
      simpleScheduler.setSchedulerName(TestUtils.SIMPLE_SCHEDULER_NAME);
      simpleScheduler.start();

      assertThat(simpleScheduler.getExecutors()).isNotEmpty();

      simpleScheduler.stopAsync();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testScheduledProcess() throws QInstanceValidationException
   {
      QInstance qInstance = QContext.getQInstance();
      new QInstanceValidator().validate(qInstance);
      qInstance.getAutomationProviders().clear();
      qInstance.getQueueProviders().clear();

      qInstance.addProcess(
         new QProcessMetaData()
            .withName("testScheduledProcess")
            .withSchedule(new QScheduleMetaData().withRepeatMillis(2).withInitialDelaySeconds(0))
            .withStepList(List.of(new QBackendStepMetaData()
               .withName("step")
               .withCode(new QCodeReference(BasicStep.class))))
      );

      BasicStep.counter = 0;

      SimpleScheduler simpleScheduler = SimpleScheduler.getInstance(qInstance);
      simpleScheduler.setSchedulerName(TestUtils.SIMPLE_SCHEDULER_NAME);
      simpleScheduler.setSessionSupplier(QSession::new);
      simpleScheduler.start();
      SleepUtils.sleep(50, TimeUnit.MILLISECONDS);
      simpleScheduler.stopAsync();

      System.out.println("Ran: " + BasicStep.counter + " times");
      assertTrue(BasicStep.counter > 1, "Scheduled process should have ran at least twice");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class BasicStep implements BackendStep
   {
      static int counter = 0;



      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         counter++;
      }
   }

}