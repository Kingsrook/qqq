/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.FunctionBody;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;


/*******************************************************************************
 ** Action handler for running q-functions.
 *
 *******************************************************************************/
public class RunFunctionAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public RunFunctionResult execute(RunFunctionRequest runFunctionRequest) throws QException
   {
      ///////////////////////////////////////////////////////
      // todo - shouldn't meta-data validation catch this? //
      ///////////////////////////////////////////////////////
      QProcessMetaData process = runFunctionRequest.getInstance().getProcess(runFunctionRequest.getProcessName());
      if(process == null)
      {
         throw new QException("Process [" + runFunctionRequest.getProcessName() + "] is not defined in this instance.");
      }

      QFunctionMetaData function = process.getFunction(runFunctionRequest.getFunctionName());
      if(function == null)
      {
         throw new QException("Function [" + runFunctionRequest.getFunctionName() + "] is not defined in the process [" + process.getName() + "]");
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // ensure input data is set as needed - use callback object to get anything missing //
      //////////////////////////////////////////////////////////////////////////////////////
      ensureRecordsAreInRequest(runFunctionRequest, function);
      ensureInputFieldsAreInRequest(runFunctionRequest, function);

      ////////////////////////////////////////////////////////////////////
      // load and run the user-defined code that actually does the work //
      ////////////////////////////////////////////////////////////////////
      RunFunctionResult runFunctionResult = runFunctionBodyCode(function.getCode(), runFunctionRequest);

      return (runFunctionResult);
   }



   /*******************************************************************************
    ** check if this function needs any input fields - and if so, if we need to get one
    ** via the callback
    **
    *******************************************************************************/
   private void ensureInputFieldsAreInRequest(RunFunctionRequest runFunctionRequest, QFunctionMetaData function)
   {
      QFunctionInputMetaData inputMetaData = function.getInputMetaData();

      List<QFieldMetaData> fieldsToGet = new ArrayList<>();
      for(QFieldMetaData field : inputMetaData.getFieldList())
      {
         Serializable value = runFunctionRequest.getValue(field.getName());
         if(value == null)
         {
            // todo - check if required?
            fieldsToGet.add(field);
         }
      }

      Map<String, Serializable> fieldValues = runFunctionRequest.getCallback().getFieldValues(fieldsToGet);
      for(Map.Entry<String, Serializable> entry : fieldValues.entrySet())
      {
         runFunctionRequest.addValue(entry.getKey(), entry.getValue());
         // todo - check to make sure got values back?
      }
   }



   /*******************************************************************************
    ** check if this function uses a record list - and if so, if we need to get one
    ** via the callback
    *******************************************************************************/
   private void ensureRecordsAreInRequest(RunFunctionRequest runFunctionRequest, QFunctionMetaData function) throws QException
   {
      QFunctionInputMetaData inputMetaData = function.getInputMetaData();
      QRecordListMetaData recordListMetaData = inputMetaData.getRecordListMetaData();
      if(recordListMetaData != null)
      {
         if(runFunctionRequest.getRecords() == null)
         {
            QueryRequest queryRequest = new QueryRequest(runFunctionRequest.getInstance());
            queryRequest.setTableName(inputMetaData.getRecordListMetaData().getTableName());

            // todo - handle this being async (e.g., http)
            // seems like it just needs to throw, breaking this flow, and to send a response to the frontend, directing it to prompt the user for the needed data
            // then this function can re-run, hopefully with the needed data.
            queryRequest.setFilter(runFunctionRequest.getCallback().getQueryFilter());

            QueryResult queryResult = new QueryAction().execute(queryRequest);
            runFunctionRequest.setRecords(queryResult.getRecords());
            // todo - handle 0 results found?
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunFunctionResult runFunctionBodyCode(QCodeReference code, RunFunctionRequest runFunctionRequest)
   {
      RunFunctionResult runFunctionResult;
      try
      {
         Class<?> codeClass = Class.forName(code.getName());
         Object codeObject = codeClass.getConstructor().newInstance();
         if(!(codeObject instanceof FunctionBody))
         {
            throw (new QException("The supplied code [" + codeClass.getName() + "] is not an instance of FunctionBody"));
         }

         FunctionBody functionBodyCodeObject = (FunctionBody) codeObject;
         runFunctionResult = functionBodyCodeObject.run(runFunctionRequest);
      }
      catch(Exception e)
      {
         runFunctionResult = new RunFunctionResult();
         runFunctionResult.setError("Error running function code: " + e.getMessage());
         e.printStackTrace();
      }

      return (runFunctionResult);
   }
}
