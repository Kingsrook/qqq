package com.kingsrook.qqq.backend.core.model.data;


import java.util.List;


/*******************************************************************************
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
