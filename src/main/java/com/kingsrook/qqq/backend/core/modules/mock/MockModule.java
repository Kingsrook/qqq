package com.kingsrook.qqq.backend.core.modules.mock;


import com.kingsrook.qqq.backend.core.modules.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.QModuleInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.QueryInterface;


/*******************************************************************************
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
