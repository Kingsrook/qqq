package com.kingsrook.qqq.backend.core.model.actions;


import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


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
   public AbstractQRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractQRequest(QInstance instance)
   {
      this.instance = instance;

      ////////////////////////////////////////////////////////////
      // if this instance hasn't been validated yet, do so now  //
      // noting that this will also enrich any missing metaData //
      ////////////////////////////////////////////////////////////
      if(! instance.getHasBeenValidated())
      {
         try
         {
            new QInstanceValidator().validate(instance);
         }
         catch(QInstanceValidationException e)
         {
            System.err.println(e.getMessage());
            throw (new IllegalArgumentException("QInstance failed validation" + e.getMessage()));
         }
      }
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
