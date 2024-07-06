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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Base class for the Load (aka, store) logic of Streamed ETL processes.
 **
 ** Records are to be read out of the input object's Records field, and after storing,
 ** should be written to the output object's Records, noting that when running
 ** as a streamed-ETL process, those input & output objects will be instances of
 ** the StreamedBackendStep{Input,Output} classes, that will be associated with
 ** a page of records flowing through a pipe.
 **
 ** Also - use the transaction member variable!!!
 *******************************************************************************/
public abstract class AbstractLoadStep
{
   private   Optional<QBackendTransaction> transaction = Optional.empty();
   protected QSession                      session;

   private AbstractTransformStep transformStep;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Deprecated
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      runOnePage(runBackendStepInput, runBackendStepOutput);
   }


   /*******************************************************************************
    ** todo - make abstract when run is deleted.
    *******************************************************************************/
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {

   }


   /*******************************************************************************
    ** Allow subclasses to do an action before the run is complete - before any
    ** pages of records are passed in.
    *******************************************************************************/
   public void preRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      this.session = runBackendStepInput.getSession();
   }



   /*******************************************************************************
    ** Allow subclasses to do an action after the run is complete - after the last
    ** page of records is passed in.
    *******************************************************************************/
   public void postRun(BackendStepPostRunInput runBackendStepInput, BackendStepPostRunOutput runBackendStepOutput) throws QException
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Optional<QBackendTransaction> openTransaction(RunBackendStepInput runBackendStepInput) throws QException
   {
      return (Optional.empty());
   }



   /*******************************************************************************
    ** Setter for transaction
    **
    *******************************************************************************/
   public void setTransaction(Optional<QBackendTransaction> transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    ** Getter for transaction
    **
    *******************************************************************************/
   public Optional<QBackendTransaction> getTransaction()
   {
      return (transaction);
   }



   /*******************************************************************************
    ** Allow this load step to specify the capacity of the pipe being used by the process.
    **
    ** The specific use-case for which this is being added is, in the case of a process
    ** with many records being extracted, if the load job is too slow, then the pipe
    ** can get filled, and the extractor (who puts records into the pipe) can time out
    ** waiting for capacity in the pipe to open up, while a slow loader is consuming
    ** the records.
    **
    ** In other words, for a slow loader, setting a lower pipe capacity can help prevent
    ** time-out errors ("Giving up adding record to pipe...")
    *******************************************************************************/
   public Integer getOverrideRecordPipeCapacity(RunBackendStepInput runBackendStepInput)
   {
      return (null);
   }



   /*******************************************************************************
    ** Getter for transformStep
    *******************************************************************************/
   public AbstractTransformStep getTransformStep()
   {
      return (this.transformStep);
   }



   /*******************************************************************************
    ** Setter for transformStep
    *******************************************************************************/
   public void setTransformStep(AbstractTransformStep transformStep)
   {
      this.transformStep = transformStep;
   }



   /*******************************************************************************
    ** Fluent setter for transformStep
    *******************************************************************************/
   public AbstractLoadStep withTransformStep(AbstractTransformStep transformStep)
   {
      this.transformStep = transformStep;
      return (this);
   }

}
