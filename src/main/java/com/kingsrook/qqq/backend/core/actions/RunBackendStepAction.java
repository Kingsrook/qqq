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

package com.kingsrook.qqq.backend.core.actions;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.callbacks.QProcessCallback;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.BackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Action handler for running backend steps as part of processes.
 *
 *******************************************************************************/
public class RunBackendStepAction
{
   private static final Logger LOG = LogManager.getLogger(RunBackendStepAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunBackendStepResult execute(RunBackendStepRequest runBackendStepRequest) throws QException
   {
      ActionHelper.validateSession(runBackendStepRequest);

      QProcessMetaData process = runBackendStepRequest.getInstance().getProcess(runBackendStepRequest.getProcessName());
      if(process == null)
      {
         throw new QException("Process [" + runBackendStepRequest.getProcessName() + "] is not defined in this instance.");
      }

      QStepMetaData stepMetaData = process.getStep(runBackendStepRequest.getStepName());
      if(stepMetaData == null)
      {
         throw new QException("Step [" + runBackendStepRequest.getStepName() + "] is not defined in the process [" + process.getName() + "]");
      }

      if(!(stepMetaData instanceof QBackendStepMetaData backendStepMetaData))
      {
         throw new QException("Step [" + runBackendStepRequest.getStepName() + "] is not a backend step.");
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // ensure input data is set as needed - use callback object to get anything missing //
      //////////////////////////////////////////////////////////////////////////////////////
      ensureRecordsAreInRequest(runBackendStepRequest, backendStepMetaData);
      ensureInputFieldsAreInRequest(runBackendStepRequest, backendStepMetaData);

      ////////////////////////////////////////////////////////////////////
      // load and run the user-defined code that actually does the work //
      ////////////////////////////////////////////////////////////////////
      return (runStepCode(backendStepMetaData.getCode(), runBackendStepRequest));
   }



   /*******************************************************************************
    ** check if this step needs any input fields - and if so, if we need to get one
    ** via the callback
    **
    *******************************************************************************/
   private void ensureInputFieldsAreInRequest(RunBackendStepRequest runBackendStepRequest, QBackendStepMetaData step) throws QException
   {
      QFunctionInputMetaData inputMetaData = step.getInputMetaData();
      if(inputMetaData == null)
      {
         return;
      }

      List<QFieldMetaData> fieldsToGet = new ArrayList<>();
      for(QFieldMetaData field : inputMetaData.getFieldList())
      {
         Serializable value = runBackendStepRequest.getValue(field.getName());
         if(value == null)
         {
            if(field.getDefaultValue() != null)
            {
               runBackendStepRequest.addValue(field.getName(), field.getDefaultValue());
            }
            else
            {
               // todo - check if required?
               fieldsToGet.add(field);
            }
         }
      }

      if(!fieldsToGet.isEmpty())
      {
         QProcessCallback callback = runBackendStepRequest.getCallback();
         if(callback == null)
         {
            throw (new QException("Function is missing values for fields, but no callback was present to request fields from a user"));
         }

         Map<String, Serializable> fieldValues = callback.getFieldValues(fieldsToGet);
         for(Map.Entry<String, Serializable> entry : fieldValues.entrySet())
         {
            runBackendStepRequest.addValue(entry.getKey(), entry.getValue());
            // todo - check to make sure got values back?
         }
      }
   }



   /*******************************************************************************
    ** check if this step uses a record list - and if so, if we need to get one
    ** via the callback
    *******************************************************************************/
   private void ensureRecordsAreInRequest(RunBackendStepRequest runBackendStepRequest, QBackendStepMetaData step) throws QException
   {
      QFunctionInputMetaData inputMetaData = step.getInputMetaData();
      if(inputMetaData != null && inputMetaData.getRecordListMetaData() != null)
      {
         if(CollectionUtils.nullSafeIsEmpty(runBackendStepRequest.getRecords()))
         {
            QueryRequest queryRequest = new QueryRequest(runBackendStepRequest.getInstance());
            queryRequest.setSession(runBackendStepRequest.getSession());
            queryRequest.setTableName(inputMetaData.getRecordListMetaData().getTableName());

            // todo - handle this being async (e.g., http)
            // seems like it just needs to throw, breaking this flow, and to send a response to the frontend, directing it to prompt the user for the needed data
            // then this step can re-run, hopefully with the needed data.

            QProcessCallback callback = runBackendStepRequest.getCallback();
            if(callback == null)
            {
               throw (new QException("Function is missing input records, but no callback was present to get a query filter from a user"));
            }

            queryRequest.setFilter(callback.getQueryFilter());

            QueryResult queryResult = new QueryAction().execute(queryRequest);
            runBackendStepRequest.setRecords(queryResult.getRecords());
            // todo - handle 0 results found?
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunBackendStepResult runStepCode(QCodeReference code, RunBackendStepRequest runBackendStepRequest)
   {
      RunBackendStepResult runBackendStepResult = new RunBackendStepResult();
      try
      {
         runBackendStepResult.seedFromRequest(runBackendStepRequest);

         Class<?> codeClass  = Class.forName(code.getName());
         Object   codeObject = codeClass.getConstructor().newInstance();
         if(!(codeObject instanceof BackendStep backendStepCodeObject))
         {
            throw (new QException("The supplied code [" + codeClass.getName() + "] is not an instance of FunctionBody"));
         }

         backendStepCodeObject.run(runBackendStepRequest, runBackendStepResult);
      }
      catch(Exception e)
      {
         runBackendStepResult = new RunBackendStepResult();
         runBackendStepResult.setException(e);
         LOG.info("Error running backend step code", e);
      }

      return (runBackendStepResult);
   }
}
