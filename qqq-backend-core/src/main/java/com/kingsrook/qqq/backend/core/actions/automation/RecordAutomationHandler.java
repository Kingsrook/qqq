package com.kingsrook.qqq.backend.core.actions.automation;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.automation.RecordAutomationInput;


/*******************************************************************************
 ** Base class for custom-codes to run as an automation action
 *******************************************************************************/
public abstract class RecordAutomationHandler
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract void execute(RecordAutomationInput recordAutomationInput) throws QException;

}
