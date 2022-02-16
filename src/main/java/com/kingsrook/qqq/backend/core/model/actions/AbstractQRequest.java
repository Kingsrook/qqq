/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions;


import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Base request class for all Q actions.
 **
 *******************************************************************************/
public abstract class AbstractQRequest
{
   protected QInstance instance;
   protected QSession session;



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
      if(!instance.getHasBeenValidated())
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
    **
    *******************************************************************************/
   public QAuthenticationMetaData getAuthenticationMetaData()
   {
      return (instance.getAuthentication());
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



   /*******************************************************************************
    ** Getter for session
    **
    *******************************************************************************/
   public QSession getSession()
   {
      return session;
   }



   /*******************************************************************************
    ** Setter for session
    **
    *******************************************************************************/
   public void setSession(QSession session)
   {
      this.session = session;
   }
}
