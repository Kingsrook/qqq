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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.basic;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Function body for performing the Load step of a basic ETL process.
 *******************************************************************************/
public class BasicETLLoadFunction implements BackendStep
{
   private static final Logger LOG = LogManager.getLogger(BasicETLLoadFunction.class);

   private QBackendTransaction transaction;
   private boolean             returnStoredRecords = true;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      //////////////////////////////////////////////////////
      // exit early with no-op if no records made it here //
      //////////////////////////////////////////////////////
      List<QRecord> inputRecords = runBackendStepInput.getRecords();
      LOG.info("Received [" + inputRecords.size() + "] records to load");
      if(CollectionUtils.nullSafeIsEmpty(inputRecords))
      {
         runBackendStepOutput.addValue(BasicETLProcess.FIELD_RECORD_COUNT, 0);
         return;
      }

      //////////////////////////////////////////////////////////////////
      // put the destination table name in all records being inserted //
      //////////////////////////////////////////////////////////////////
      String table = runBackendStepInput.getValueString(BasicETLProcess.FIELD_DESTINATION_TABLE);
      for(QRecord record : inputRecords)
      {
         record.setTableName(table);
      }

      //////////////////////////////////////////
      // run an insert request on the records //
      //////////////////////////////////////////
      int           recordsInserted = 0;
      List<QRecord> outputRecords   = new ArrayList<>();
      int           pageSize        = 1000; // todo - make this a field?

      for(List<QRecord> page : CollectionUtils.getPages(inputRecords, pageSize))
      {
         LOG.info("Inserting a page of [" + page.size() + "] records. Progress:  " + recordsInserted + " loaded out of " + inputRecords.size() + " total");
         InsertInput insertInput = new InsertInput(runBackendStepInput.getInstance());
         insertInput.setSession(runBackendStepInput.getSession());
         insertInput.setTableName(table);
         insertInput.setRecords(page);
         insertInput.setTransaction(transaction);

         InsertAction insertAction = new InsertAction();
         InsertOutput insertOutput = insertAction.execute(insertInput);

         if(returnStoredRecords)
         {
            outputRecords.addAll(insertOutput.getRecords());
         }

         recordsInserted += insertOutput.getRecords().size();
      }
      runBackendStepOutput.setRecords(outputRecords);
      runBackendStepOutput.addValue(BasicETLProcess.FIELD_RECORD_COUNT, recordsInserted);
   }



   /*******************************************************************************
    ** Setter for transaction
    **
    *******************************************************************************/
   public void setTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    ** Setter for returnStoredRecords
    **
    *******************************************************************************/
   public void setReturnStoredRecords(boolean returnStoredRecords)
   {
      this.returnStoredRecords = returnStoredRecords;
   }
}
