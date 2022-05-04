/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata.processes;


import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class QRecordListMetaData
{
   private String tableName;
   private Map<String, QFieldMetaData> fields;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData getField(String fieldName)
   {
      if(fields == null)
      {
         return (null);
      }

      QFieldMetaData field = getFields().get(fieldName);
      if(field == null)
      {
         throw (new IllegalArgumentException("Field [" + fieldName + "] was not found."));
      }

      return (field);
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
   public QRecordListMetaData withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, QFieldMetaData> getFields()
   {
      return fields;
   }



   /*******************************************************************************
    ** Setter for fields
    **
    *******************************************************************************/
   public void setFields(Map<String, QFieldMetaData> fields)
   {
      this.fields = fields;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecordListMetaData addField(QFieldMetaData field)
   {
      if(this.fields == null)
      {
         this.fields = new LinkedHashMap<>();
      }
      this.fields.put(field.getName(), field);
      return (this);
   }

}
