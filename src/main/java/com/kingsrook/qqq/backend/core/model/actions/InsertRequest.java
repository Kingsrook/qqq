package com.kingsrook.qqq.backend.core.model.actions;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 **
 *******************************************************************************/
public class InsertRequest extends AbstractQTableRequest
{
   private List<QRecord> records;



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertRequest(QInstance instance)
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
