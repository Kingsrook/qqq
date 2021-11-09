package com.kingsrook.qqq.backend.core.utils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ExceptionUtils
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static <T extends Throwable> T findClassInRootChain(Throwable e, Class<T> targetClass)
   {
      if (e == null)
      {
         return (null);
      }

      if(e.getClass().equals(targetClass))
      {
         //noinspection unchecked
         return ((T) e);
      }

      if(e.getCause() == null)
      {
         return (null);
      }

      return findClassInRootChain(e.getCause(), targetClass);
   }

}
