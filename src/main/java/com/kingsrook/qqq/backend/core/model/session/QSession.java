/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.session;


import java.util.HashMap;
import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class QSession
{
   private String idReference;
   private QUser user;

   // implementation-specific custom values
   private Map<String, String> values;



   /*******************************************************************************
    ** Getter for idReference
    **
    *******************************************************************************/
   public String getIdReference()
   {
      return idReference;
   }



   /*******************************************************************************
    ** Setter for idReference
    **
    *******************************************************************************/
   public void setIdReference(String idReference)
   {
      this.idReference = idReference;
   }



   /*******************************************************************************
    ** Getter for user
    **
    *******************************************************************************/
   public QUser getUser()
   {
      return user;
   }



   /*******************************************************************************
    ** Setter for user
    **
    *******************************************************************************/
   public void setUser(QUser user)
   {
      this.user = user;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getValue(String key)
   {
      if(values == null)
      {
         return null;
      }
      return values.get(key);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setValue(String key, String value)
   {
      if(values == null)
      {
         values = new HashMap<>();
      }
      values.put(key, value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QSession withValue(String key, String value)
   {
      if(values == null)
      {
         values = new HashMap<>();
      }
      values.put(key, value);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, String> getValues()
   {
      return values;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setValues(Map<String, String> values)
   {
      this.values = values;
   }

}
