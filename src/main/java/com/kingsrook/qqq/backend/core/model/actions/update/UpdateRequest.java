/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.update;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQTableRequest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 ** Request data handler for the update action
 **
 *******************************************************************************/
public class UpdateRequest extends AbstractQTableRequest
{
   private List<QRecord> records;



   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateRequest(QInstance instance)
   {
      super(instance);
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
}
