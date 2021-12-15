/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.exceptions;


/*******************************************************************************
 * Base class for checked exceptions thrown in qqq.
 *
 *******************************************************************************/
public class QException extends Exception
{

   /*******************************************************************************
    ** Constructor of message
    **
    *******************************************************************************/
   public QException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    ** Constructor of message & cause
    **
    *******************************************************************************/
   public QException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
