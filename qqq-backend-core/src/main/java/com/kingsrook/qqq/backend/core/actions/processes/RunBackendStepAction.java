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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
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
   public RunBackendStepOutput execute(RunBackendStepInput runBackendStepInput) throws QException
   {
      ActionHelper.validateSession(runBackendStepInput);

      QProcessMetaData process = runBackendStepInput.getInstance().getProcess(runBackendStepInput.getProcessName());
      if(process == null)
      {
         throw new QException("Process [" + runBackendStepInput.getProcessName() + "] is not defined in this instance.");
      }

      QStepMetaData stepMetaData = process.getStep(runBackendStepInput.getStepName());
      if(stepMetaData == null)
      {
         throw new QException("Step [" + runBackendStepInput.getStepName() + "] is not defined in the process [" + process.getName() + "]");
      }

      if(!(stepMetaData instanceof QBackendStepMetaData backendStepMetaData))
      {
         throw new QException("Step [" + runBackendStepInput.getStepName() + "] is not a backend step.");
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // ensure input data is set as needed - use callback object to get anything missing //
      //////////////////////////////////////////////////////////////////////////////////////
      ensureRecordsAreInRequest(runBackendStepInput, backendStepMetaData);
      ensureInputFieldsAreInRequest(runBackendStepInput, backendStepMetaData);

      ////////////////////////////////////////////////////////////////////
      // load and run the user-defined code that actually does the work //
      ////////////////////////////////////////////////////////////////////
      return (runStepCode(backendStepMetaData.getCode(), runBackendStepInput));
   }



   /*******************************************************************************
    ** check if this step needs any input fields - and if so, if we need to get one
    ** via the callback
    **
    *******************************************************************************/
   private void ensureInputFieldsAreInRequest(RunBackendStepInput runBackendStepInput, QBackendStepMetaData step) throws QException
   {
      QFunctionInputMetaData inputMetaData = step.getInputMetaData();
      if(inputMetaData == null)
      {
         return;
      }

      List<QFieldMetaData> fieldsToGet           = new ArrayList<>();
      List<QFieldMetaData> requiredFieldsMissing = new ArrayList<>();
      for(QFieldMetaData field : inputMetaData.getFieldList())
      {
         Serializable value = runBackendStepInput.getValue(field.getName());
         if(value == null)
         {
            if(field.getDefaultValue() != null)
            {
               runBackendStepInput.addValue(field.getName(), field.getDefaultValue());
            }
            else
            {
               fieldsToGet.add(field);
               if(field.getIsRequired())
               {
                  requiredFieldsMissing.add(field);
               }
            }
         }
      }

      if(!fieldsToGet.isEmpty())
      {
         QProcessCallback callback = runBackendStepInput.getCallback();
         if(callback == null)
         {
            if(requiredFieldsMissing.isEmpty())
            {
               ///////////////////////////////////////////////////////////////
               // if no required fields are missing, just return gracefully //
               ///////////////////////////////////////////////////////////////
               return;
            }

            ///////////////////////////////////////////////////////////////////
            // but if any required fields ARE missing, then that's an error. //
            ///////////////////////////////////////////////////////////////////
            LOG.info("Missing value for required fields: " + requiredFieldsMissing.stream().map(QFieldMetaData::getName).collect(Collectors.joining(", ")));
            throw (new QUserFacingException("Missing values for one or more fields",
               new QException("Function is missing values for fields, but no callback was present to request fields from a user")));
         }

         Map<String, Serializable> fieldValues = callback.getFieldValues(fieldsToGet);
         if(fieldValues != null)
         {
            for(Map.Entry<String, Serializable> entry : fieldValues.entrySet())
            {
               runBackendStepInput.addValue(entry.getKey(), entry.getValue());
               // todo - check to make sure got values back?
            }
         }
      }
   }



   /*******************************************************************************
    ** check if this step uses a record list - and if so, if we need to get one
    ** via the callback
    *******************************************************************************/
   private void ensureRecordsAreInRequest(RunBackendStepInput runBackendStepInput, QBackendStepMetaData step) throws QException
   {
      QFunctionInputMetaData inputMetaData = step.getInputMetaData();
      if(inputMetaData != null && inputMetaData.getRecordListMetaData() != null)
      {
         if(CollectionUtils.nullSafeIsEmpty(runBackendStepInput.getRecords()))
         {
            QueryInput queryInput = new QueryInput();
            queryInput.setTableName(inputMetaData.getRecordListMetaData().getTableName());

            // todo - handle this being async (e.g., http)
            // seems like it just needs to throw, breaking this flow, and to send a response to the frontend, directing it to prompt the user for the needed data
            // then this step can re-run, hopefully with the needed data.

            QProcessCallback callback = runBackendStepInput.getCallback();
            if(callback == null)
            {
               throw (new QUserFacingException("Missing input records.",
                  new QException("Function is missing input records, but no callback was present to request fields from a user")));
            }

            queryInput.setFilter(callback.getQueryFilter());

            QueryOutput queryOutput = new QueryAction().execute(queryInput);
            runBackendStepInput.setRecords(queryOutput.getRecords());
            // todo - handle 0 results found?
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunBackendStepOutput runStepCode(QCodeReference code, RunBackendStepInput runBackendStepInput)
   {
      RunBackendStepOutput runBackendStepOutput = new RunBackendStepOutput();
      try
      {
         runBackendStepOutput.seedFromRequest(runBackendStepInput);

         Class<?> codeClass  = Class.forName(code.getName());
         Object   codeObject = codeClass.getConstructor().newInstance();
         if(!(codeObject instanceof BackendStep backendStepCodeObject))
         {
            throw (new QException("The supplied code [" + codeClass.getName() + "] is not an instance of BackendStep"));
         }

         backendStepCodeObject.run(runBackendStepInput, runBackendStepOutput);
      }
      catch(Exception e)
      {
         runBackendStepOutput = new RunBackendStepOutput();
         runBackendStepOutput.setException(e);
         LOG.info("Error running backend step code", e);
      }

      return (runBackendStepOutput);
   }
}
