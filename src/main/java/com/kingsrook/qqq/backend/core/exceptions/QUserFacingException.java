package com.kingsrook.qqq.backend.core.exceptions;


/*******************************************************************************
 ** Exception with a good-quality message meant to be shown to an end-user.
 **
 *******************************************************************************/
public class QUserFacingException extends QException
{


   /*******************************************************************************
    ** Constructor of message
    **
    *******************************************************************************/
   public QUserFacingException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    ** Constructor of message & cause
    **
    *******************************************************************************/
   public QUserFacingException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
