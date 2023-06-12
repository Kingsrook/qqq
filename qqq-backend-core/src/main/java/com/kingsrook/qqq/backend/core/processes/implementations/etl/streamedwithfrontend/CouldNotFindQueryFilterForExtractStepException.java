package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import com.kingsrook.qqq.backend.core.exceptions.QException;


/*******************************************************************************
 **
 *******************************************************************************/
public class CouldNotFindQueryFilterForExtractStepException extends QException
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public CouldNotFindQueryFilterForExtractStepException(String message)
   {
      super(message);
   }
}
