package com.kingsrook.qqq.backend.core.model.actions;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;


/*******************************************************************************
 * Result for a delete action
 *
 *******************************************************************************/
public class DeleteResult extends AbstractQResult
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
