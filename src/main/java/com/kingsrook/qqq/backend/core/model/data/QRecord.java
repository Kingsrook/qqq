/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.data;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 * Data Record within qqq.  e.g., a single row from a database.
 *
 * Actual values (e.g., as stored in the backend system) are in the `values`
 * map.  Keys in this map are fieldNames from the QTableMetaData.
 *
 * "Display values" (e.g., labels for possible values, or formatted numbers
 * (e.g., quantities with commas)) are in the displayValues map.
 *******************************************************************************/
public class QRecord implements Serializable
{
   private String tableName;
   //x private Serializable primaryKey;
   private Map<String, Serializable> values = new LinkedHashMap<>();
   private Map<String, String> displayValues = new LinkedHashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setValue(String fieldName, Serializable value)
   {
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
    **
    *******************************************************************************/
   public void setDisplayValue(String fieldName, String displayValue)
   {
      displayValues.put(fieldName, displayValue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord withDisplayValue(String fieldName, String displayValue)
   {
      setDisplayValue(fieldName, displayValue);
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

   //x /*******************************************************************************
   //x  ** Getter for primaryKey
   //x  **
   //x  *******************************************************************************/
   //x public Serializable getPrimaryKey()
   //x {
   //x    return primaryKey;
   //x }

   //x /*******************************************************************************
   //x  ** Setter for primaryKey
   //x  **
   //x  *******************************************************************************/
   //x public void setPrimaryKey(Serializable primaryKey)
   //x {
   //x    this.primaryKey = primaryKey;
   //x }

   //x /*******************************************************************************
   //x  ** Setter for primaryKey
   //x  **
   //x  *******************************************************************************/
   //x public QRecord withPrimaryKey(Serializable primaryKey)
   //x {
   //x    this.primaryKey = primaryKey;
   //x    return (this);
   //x }



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
    ** Getter for displayValues
    **
    *******************************************************************************/
   public Map<String, String> getDisplayValues()
   {
      return displayValues;
   }



   /*******************************************************************************
    ** Setter for displayValues
    **
    *******************************************************************************/
   public void setDisplayValues(Map<String, String> displayValues)
   {
      this.displayValues = displayValues;
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public String getDisplayValue(String fieldName)
   {
      return (displayValues.get(fieldName));
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



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public LocalDate getValueDate(String fieldName)
   {
      return ((LocalDate) values.get(fieldName));
   }

}
