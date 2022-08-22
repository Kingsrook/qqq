package com.kingsrook.qqq.backend.core.actions.interfaces;


import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;


/*******************************************************************************
 **
 *******************************************************************************/
public interface QActionInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   default QBackendTransaction openTransaction(AbstractTableActionInput input) throws QException
   {
      return (new QBackendTransaction());
   }

}
