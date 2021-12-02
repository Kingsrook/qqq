package com.kingsrook.qqq.backend.core.exceptions;


/*******************************************************************************
 * Exception thrown while doing module-dispatch
 *
 *******************************************************************************/
public class QModuleDispatchException extends QException
{

   /*******************************************************************************
    ** Constructor of message
    **
    *******************************************************************************/
   public QModuleDispatchException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    ** Constructor of message & cause
    **
    *******************************************************************************/
   public QModuleDispatchException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
