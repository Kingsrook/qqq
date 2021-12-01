package com.kingsrook.qqq.backend.core.modules.interfaces;


/*******************************************************************************
 **
 *******************************************************************************/
public interface QModuleInterface
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
