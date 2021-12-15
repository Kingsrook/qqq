/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.data;


import java.util.List;


/*******************************************************************************
 ** Wrapper on a QRecord, to add status information after an action took place.
 ** e.g., any errors that occurred.
 **
 ** TODO - expand?
 **
 *******************************************************************************/
public class QRecordWithStatus extends QRecord
{
   private List<Exception> errors;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecordWithStatus()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecordWithStatus(QRecord record)
   {
      super.setTableName(record.getTableName());
      super.setPrimaryKey(record.getPrimaryKey());
      super.setValues(record.getValues());
   }



   /*******************************************************************************
    ** Getter for errors
    **
    *******************************************************************************/
   public List<Exception> getErrors()
   {
      return errors;
   }



   /*******************************************************************************
    ** Setter for errors
    **
    *******************************************************************************/
   public void setErrors(List<Exception> errors)
   {
      this.errors = errors;
   }
}
