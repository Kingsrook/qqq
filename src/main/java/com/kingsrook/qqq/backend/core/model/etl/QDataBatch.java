/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.etl;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class QDataBatch
{
   private String identity; // e.g., a full path to a file
   private List<QRecord> records;



   /*******************************************************************************
    ** Getter for identity
    **
    *******************************************************************************/
   public String getIdentity()
   {
      return identity;
   }



   /*******************************************************************************
    ** Setter for identity
    **
    *******************************************************************************/
   public void setIdentity(String identity)
   {
      this.identity = identity;
   }



   /*******************************************************************************
    ** Fluent setter for identity
    **
    *******************************************************************************/
   public QDataBatch withIdentity(String identity)
   {
      this.identity = identity;
      return (this);
   }



   /*******************************************************************************
    ** Getter for records
    **
    *******************************************************************************/
   public List<QRecord> getRecords()
   {
      return records;
   }



   /*******************************************************************************
    ** Setter for records
    **
    *******************************************************************************/
   public void setRecords(List<QRecord> records)
   {
      this.records = records;
   }



   /*******************************************************************************
    ** Setter for records
    **
    *******************************************************************************/
   public QDataBatch withRecords(List<QRecord> records)
   {
      this.records = records;
      return (this);
   }

}
