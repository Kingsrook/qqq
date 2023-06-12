package com.kingsrook.qqq.api.model.metadata.processes;


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import org.eclipse.jetty.http.HttpStatus;


/*******************************************************************************
 **
 *******************************************************************************/
public interface ApiProcessOutputInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   Serializable getOutputForProcess(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput) throws QException;

   /*******************************************************************************
    **
    *******************************************************************************/
   default HttpStatus.Code getSuccessStatusCode(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput)
   {
      return (HttpStatus.Code.OK);
   }

}
