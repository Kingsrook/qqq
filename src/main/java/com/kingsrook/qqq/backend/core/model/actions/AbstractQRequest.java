package com.kingsrook.qqq.backend.core.model.actions;


import com.kingsrook.qqq.backend.core.model.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractQRequest
{
   protected QInstance instance;
   // todo session



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract QBackendMetaData getBackend();



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractQRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractQRequest(QInstance instance)
   {
      this.instance = instance;
   }



   /*******************************************************************************
    ** Getter for instance
    **
    *******************************************************************************/
   public QInstance getInstance()
   {
      return instance;
   }



   /*******************************************************************************
    ** Setter for instance
    **
    *******************************************************************************/
   public void setInstance(QInstance instance)
   {
      this.instance = instance;
   }
}
