package com.kingsrook.qqq.api.model.metadata.processes;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;


/*******************************************************************************
 **
 *******************************************************************************/
public interface PreRunApiProcessCustomizer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   void preApiRun(RunProcessInput runProcessInput) throws QException;

}
