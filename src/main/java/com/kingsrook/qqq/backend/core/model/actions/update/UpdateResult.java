/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.update;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQResult;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;


/*******************************************************************************
 * Result for an update action
 *
 *******************************************************************************/
public class UpdateResult extends AbstractQResult
{
   List<QRecordWithStatus> records;



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecordWithStatus> getRecords()
   {
      return records;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setRecords(List<QRecordWithStatus> records)
   {
      this.records = records;
   }
}
