/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.scheduler.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.WidgetHtmlLine;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.NoCodeWidgetFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.scheduler.QScheduleManager;


/*******************************************************************************
 ** Management process to reschedule all scheduled jobs (in all schedulers).
 *******************************************************************************/
public class RescheduleAllJobsProcess implements BackendStep, MetaDataProducerInterface<QProcessMetaData>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(getClass().getSimpleName())
         .withLabel("Reschedule all Scheduled Jobs")
         .withIcon(new QIcon("update"))
         .withStepList(List.of(
            new QFrontendStepMetaData()
               .withName("confirm")
               .withComponent(new NoCodeWidgetFrontendComponentMetaData()
                  .withOutput(new WidgetHtmlLine().withVelocityTemplate("Please confirm you wish to reschedule all jobs."))),
            new QBackendStepMetaData()
               .withName("execute")
               .withCode(new QCodeReference(getClass())),
            new QFrontendStepMetaData()
               .withName("results")
               .withComponent(new NoCodeWidgetFrontendComponentMetaData()
                  .withOutput(new WidgetHtmlLine().withVelocityTemplate("All jobs have been rescheduled.")))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      try
      {
         QScheduleManager.getInstance().setupAllSchedules();
      }
      catch(Exception e)
      {
         throw (new QException("Error setting up all scheduled jobs.", e));
      }
   }

}
