/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata.processes;


/*******************************************************************************
 ** Meta-Data to define the Output View for a QQQ Function
 **
 *******************************************************************************/
public class QOutputView
{
   private String messageField;
   private QRecordListView recordListView;



   /*******************************************************************************
    ** Getter for message
    **
    *******************************************************************************/
   public String getMessageField()
   {
      return messageField;
   }



   /*******************************************************************************
    ** Setter for message
    **
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.messageField = message;
   }



   /*******************************************************************************
    ** Setter for message
    **
    *******************************************************************************/
   public QOutputView withMessageField(String messageField)
   {
      this.messageField = messageField;
      return(this);
   }



   /*******************************************************************************
    ** Getter for recordListView
    **
    *******************************************************************************/
   public QRecordListView getRecordListView()
   {
      return recordListView;
   }



   /*******************************************************************************
    ** Setter for recordListView
    **
    *******************************************************************************/
   public void setRecordListView(QRecordListView recordListView)
   {
      this.recordListView = recordListView;
   }



   /*******************************************************************************
    ** Setter for recordListView
    **
    *******************************************************************************/
   public QOutputView withRecordListView(QRecordListView recordListView)
   {
      this.recordListView = recordListView;
      return(this);
   }


}
