package com.kingsrook.qqq.backend.core.exceptions;


/*******************************************************************************
 * Base class for checked exceptions thrown in qqq.
 *
 *******************************************************************************/
public class QException extends Exception
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public QException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
