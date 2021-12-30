/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.query;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Result for a query action
 **
 *******************************************************************************/
public class QueryResult extends AbstractQResult
{
   List<QRecord> records;



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> getRecords()
   {
      return records;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setRecords(List<QRecord> records)
   {
      this.records = records;
   }
}
