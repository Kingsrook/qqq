/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonFilter;


/*******************************************************************************
 ** Meta-data to provide details of an authentication provider (e.g., google, saml,
 ** etc) within a qqq instance
 **
 *******************************************************************************/
public class QAuthenticationMetaData
{
   private String name;
   private String type;

   @JsonFilter("secretsFilter")
   private Map<String, String> values;



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
   public QAuthenticationMetaData withValue(String key, String value)
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
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAuthenticationMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(String type)
   {
      this.type = type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAuthenticationMetaData withType(String type)
   {
      this.type = type;
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



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAuthenticationMetaData withVales(Map<String, String> values)
   {
      this.values = values;
      return (this);
   }

}
