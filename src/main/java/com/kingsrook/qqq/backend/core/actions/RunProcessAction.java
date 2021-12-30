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
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;


/*******************************************************************************
 ** Action handler for running q-processes (which are a sequence of q-functions).
 *
 *******************************************************************************/
public class RunProcessAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public RunProcessResult execute(RunProcessRequest runProcessRequest) throws QException
   {
      ///////////////////////////////////////////////////////
      // todo - shouldn't meta-data validation catch this? //
      ///////////////////////////////////////////////////////
      QProcessMetaData process = runProcessRequest.getInstance().getProcess(runProcessRequest.getProcessName());
      if(process == null)
      {
         throw new QException("Process [" + runProcessRequest.getProcessName() + "] is not defined in this instance.");
      }

      RunProcessResult runProcessResult = runProcessFunctions(process);

      return (runProcessResult);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunProcessResult runProcessFunctions(QProcessMetaData process)
   {
      List<QFunctionMetaData> functionList = process.getFunctionList();
      for(QFunctionMetaData qFunctionMetaData : functionList)
      {
         // todo - custom routing?
      }
   }

}
