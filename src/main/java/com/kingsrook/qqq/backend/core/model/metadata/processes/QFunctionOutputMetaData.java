/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata.processes;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;


/*******************************************************************************
 ** Meta-Data to define the Output Data for a QQQ Function
 **
 *******************************************************************************/
public class QFunctionOutputMetaData
{
   private QRecordListMetaData recordListMetaData;
   private List<QFieldMetaData> fieldList;



   /*******************************************************************************
    ** Getter for recordListMetaData
    **
    *******************************************************************************/
   public QRecordListMetaData getRecordListMetaData()
   {
      return recordListMetaData;
   }



   /*******************************************************************************
    ** Setter for recordListMetaData
    **
    *******************************************************************************/
   public void setRecordListMetaData(QRecordListMetaData recordListMetaData)
   {
      this.recordListMetaData = recordListMetaData;
   }



   /*******************************************************************************
    ** Setter for recordListMetaData
    **
    *******************************************************************************/
   public QFunctionOutputMetaData withRecordListMetaData(QRecordListMetaData recordListMetaData)
   {
      this.recordListMetaData = recordListMetaData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fieldList
    **
    *******************************************************************************/
   public List<QFieldMetaData> getFieldList()
   {
      return fieldList;
   }



   /*******************************************************************************
    ** Setter for fieldList
    **
    *******************************************************************************/
   public void setFieldList(List<QFieldMetaData> fieldList)
   {
      this.fieldList = fieldList;
   }



   /*******************************************************************************
    ** Setter for fieldList
    **
    *******************************************************************************/
   public QFunctionOutputMetaData withFieldList(List<QFieldMetaData> fieldList)
   {
      this.fieldList = fieldList;
      return (this);
   }



   /*******************************************************************************
    ** Setter for fieldList
    **
    *******************************************************************************/
   public QFunctionOutputMetaData addField(QFieldMetaData field)
   {
      if(this.fieldList == null)
      {
         this.fieldList = new ArrayList<>();
      }
      this.fieldList.add(field);
      return (this);
   }

}

