/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.tracing;


import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;


/*******************************************************************************
 ** Implementation of ProcessTracerInterface that does nothing (no-op).
 *******************************************************************************/
public class NoopProcessTracer implements ProcessTracerInterface
{


   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleProcessStart(RunProcessInput runProcessInput)
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleProcessResume(RunProcessInput runProcessInput)
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleStepStart(RunBackendStepInput runBackendStepInput)
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleMessage(RunBackendStepInput runBackendStepInput, ProcessTracerMessage message)
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleStepFinish(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput)
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleProcessBreak(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput, Exception processException)
   {
   }




   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleProcessFinish(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput, Exception processException)
   {
   }

}
