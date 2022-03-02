/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.interfaces;


/*******************************************************************************
 ** Interface that a QBackendModule must implement.
 **
 ** Note, methods all have a default version, which throws a 'not implemented'
 ** exception.
 **
 *******************************************************************************/
public interface QBackendModuleInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   default QueryInterface getQueryInterface()
   {
      throwNotImplemented("Query");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default InsertInterface getInsertInterface()
   {
      throwNotImplemented("Insert");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default UpdateInterface getUpdateInterface()
   {
      throwNotImplemented("Update");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default DeleteInterface getDeleteInterface()
   {
      throwNotImplemented("Delete");
      return null;
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   private void throwNotImplemented(String actionName)
   {
      throw new IllegalStateException(actionName + " is not implemented in this module: " + this.getClass().getSimpleName());
   }
}
