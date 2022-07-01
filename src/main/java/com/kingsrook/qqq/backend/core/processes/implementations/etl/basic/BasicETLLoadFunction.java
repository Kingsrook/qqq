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
import com.kingsrook.qqq.backend.core.actions.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.FunctionBody;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertResult;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Function body for performing the Load step of a basic ETL process.
 *******************************************************************************/
public class BasicETLLoadFunction implements FunctionBody
{
   private static final Logger LOG = LogManager.getLogger(BasicETLLoadFunction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunFunctionRequest runFunctionRequest, RunFunctionResult runFunctionResult) throws QException
   {
      //////////////////////////////////////////////////////
      // exit early with no-op if no records made it here //
      //////////////////////////////////////////////////////
      List<QRecord> inputRecords = runFunctionRequest.getRecords();
      LOG.info("Received [" + inputRecords.size() + "] records to load");
      if(CollectionUtils.nullSafeIsEmpty(inputRecords))
      {
         runFunctionResult.addValue(BasicETLProcess.FIELD_RECORD_COUNT, 0);
         return;
      }

      //////////////////////////////////////////////////////////////////
      // put the destination table name in all records being inserted //
      //////////////////////////////////////////////////////////////////
      String table = runFunctionRequest.getValueString(BasicETLProcess.FIELD_DESTINATION_TABLE);
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
         InsertRequest insertRequest = new InsertRequest(runFunctionRequest.getInstance());
         insertRequest.setSession(runFunctionRequest.getSession());
         insertRequest.setTableName(table);
         insertRequest.setRecords(page);

         InsertAction insertAction = new InsertAction();
         InsertResult insertResult = insertAction.execute(insertRequest);
         outputRecords.addAll(insertResult.getRecords());

         recordsInserted += insertResult.getRecords().size();
      }
      runFunctionResult.setRecords(outputRecords);
      runFunctionResult.addValue(BasicETLProcess.FIELD_RECORD_COUNT, recordsInserted);
   }

}
