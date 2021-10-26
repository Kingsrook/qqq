package com.kingsrook.qqq.backend.core.model.data;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 * Data Record within qqq
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
