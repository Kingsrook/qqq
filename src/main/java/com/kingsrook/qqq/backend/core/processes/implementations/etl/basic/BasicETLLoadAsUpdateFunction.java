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
import com.kingsrook.qqq.backend.core.actions.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.BackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Function body for performing the Load step of a basic ETL process using update.
 *******************************************************************************/
public class BasicETLLoadAsUpdateFunction implements BackendStep
{
   private static final Logger LOG = LogManager.getLogger(BasicETLLoadAsUpdateFunction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepRequest runBackendStepRequest, RunBackendStepResult runBackendStepResult) throws QException
   {
      //////////////////////////////////////////////////////
      // exit early with no-op if no records made it here //
      //////////////////////////////////////////////////////
      List<QRecord> inputRecords = runBackendStepRequest.getRecords();
      LOG.info("Received [" + inputRecords.size() + "] records to load using update");
      if(CollectionUtils.nullSafeIsEmpty(inputRecords))
      {
         runBackendStepResult.addValue(BasicETLProcess.FIELD_RECORD_COUNT, 0);
         return;
      }

      /////////////////////////////////////////////////////////////////
      // put the destination table name in all records being updated //
      /////////////////////////////////////////////////////////////////
      String table = runBackendStepRequest.getValueString(BasicETLProcess.FIELD_DESTINATION_TABLE);
      for(QRecord record : inputRecords)
      {
         record.setTableName(table);
      }

      //////////////////////////////////////////
      // run an update request on the records //
      //////////////////////////////////////////
      int recordsUpdated = 0;
      List<QRecord> outputRecords = new ArrayList<>();
      int pageSize = 1000; // todo - make this a field?

      for(List<QRecord> page : CollectionUtils.getPages(inputRecords, pageSize))
      {
         LOG.info("Updating a page of [" + page.size() + "] records. Progress:  " + recordsUpdated + " loaded out of " + inputRecords.size() + " total");
         UpdateRequest updateRequest = new UpdateRequest(runBackendStepRequest.getInstance());
         updateRequest.setSession(runBackendStepRequest.getSession());
         updateRequest.setTableName(table);
         updateRequest.setRecords(page);

         UpdateAction updateAction = new UpdateAction();
         UpdateResult updateResult = updateAction.execute(updateRequest);
         outputRecords.addAll(updateResult.getRecords());

         recordsUpdated += updateResult.getRecords().size();
      }
      runBackendStepResult.setRecords(outputRecords);
      runBackendStepResult.addValue(BasicETLProcess.FIELD_RECORD_COUNT, recordsUpdated);
   }

}