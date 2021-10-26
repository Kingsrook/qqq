package com.kingsrook.qqq.backend.core.exceptions;


/*******************************************************************************
 * Base class for checked exceptions thrown in qqq.
 *
 *******************************************************************************/
public class QModuleDispatchException extends QException
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public QModuleDispatchException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QModuleDispatchException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
