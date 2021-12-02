package com.kingsrook.qqq.backend.core.modules.mock;


import com.kingsrook.qqq.backend.core.modules.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.QModuleInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.QueryInterface;


/*******************************************************************************
 ** A Mock implementation of the QModuleInterface.
 **
 ** Mostly just exists to allow the backend-core repository to be able to run
 ** tests over all the actions available through the QModuleInterface.
 **
 *******************************************************************************/
public class MockModule implements QModuleInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QueryInterface getQueryInterface()
   {
      return new MockQueryAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertInterface getInsertInterface()
   {
      return (new MockInsertAction());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DeleteInterface getDeleteInterface()
   {
      return (new MockDeleteAction());
   }

}
