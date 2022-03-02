/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.utils;


/*******************************************************************************
 ** Utility class for working with exceptions.
 **
 *******************************************************************************/
public class ExceptionUtils
{

   /*******************************************************************************
    ** Find a specific exception class in an exception's caused-by chain.  Returns
    ** null if not found.  Be aware, checks for class.equals -- not instanceof.
    **
    *******************************************************************************/
   public static <T extends Throwable> T findClassInRootChain(Throwable e, Class<T> targetClass)
   {
      if (e == null)
      {
         return (null);
      }

      if(targetClass.isInstance(e))
      {
         return targetClass.cast(e);
      }

      if(e.getCause() == null)
      {
         return (null);
      }

      return findClassInRootChain(e.getCause(), targetClass);
   }

}
