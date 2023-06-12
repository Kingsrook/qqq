package com.kingsrook.qqq.api.model.metadata.processes;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public interface PostRunApiProcessCustomizer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   void postApiRun(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput) throws QException;

}
