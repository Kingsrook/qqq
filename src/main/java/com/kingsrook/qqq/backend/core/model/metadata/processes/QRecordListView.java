/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata.processes;


import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 ** Meta-Data to define how to view a qqq record list (e.g., what fields to show).
 **
 *******************************************************************************/
public class QRecordListView
{
   private List<String> fieldNames;



   /*******************************************************************************
    ** Getter for fieldNames
    **
    *******************************************************************************/
   public List<String> getFieldNames()
   {
      return fieldNames;
   }



   /*******************************************************************************
    ** Setter for fieldNames
    **
    *******************************************************************************/
   public void setFieldNames(List<String> fieldNames)
   {
      this.fieldNames = fieldNames;
   }



   /*******************************************************************************
    ** Setter for fieldNames
    **
    *******************************************************************************/
   public QRecordListView withFieldNames(List<String> fieldNames)
   {
      this.fieldNames = fieldNames;
      return (this);
   }



   /*******************************************************************************
    ** Setter for fieldNames
    **
    *******************************************************************************/
   public QRecordListView addFieldName(String fieldName)
   {
      if(this.fieldNames == null)
      {
         this.fieldNames = new ArrayList<>();
      }
      this.fieldNames.add(fieldName);
      return (this);
   }

}
