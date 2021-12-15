/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.data;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 * Data Record within qqq.  e.g., a single row from a database.
 *
 *******************************************************************************/
public class QRecord
{
   private String tableName;
   private Serializable primaryKey;
   private Map<String, Serializable> values;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setValue(String fieldName, Serializable value)
   {
      if(values == null)
      {
         values = new LinkedHashMap<>();
      }

      values.put(fieldName, value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord withValue(String fieldName, Serializable value)
   {
      setValue(fieldName, value);
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public QRecord withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for primaryKey
    **
    *******************************************************************************/
   public Serializable getPrimaryKey()
   {
      return primaryKey;
   }



   /*******************************************************************************
    ** Setter for primaryKey
    **
    *******************************************************************************/
   public void setPrimaryKey(Serializable primaryKey)
   {
      this.primaryKey = primaryKey;
   }



   /*******************************************************************************
    ** Setter for primaryKey
    **
    *******************************************************************************/
   public QRecord withPrimaryKey(Serializable primaryKey)
   {
      this.primaryKey = primaryKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Serializable getValue(String fieldName)
   {
      return (values.get(fieldName));
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public String getValueString(String fieldName)
   {
      return ((String) values.get(fieldName));
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Integer getValueInteger(String fieldName)
   {
      return ((Integer) values.get(fieldName));
   }

}
