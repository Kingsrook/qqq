package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractExtractFunction implements BackendStep
{
   private RecordPipe recordPipe;
   private Integer    limit;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setRecordPipe(RecordPipe recordPipe)
   {
      this.recordPipe = recordPipe;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public RecordPipe getRecordPipe()
   {
      return recordPipe;
   }



   /*******************************************************************************
    ** Getter for limit
    **
    *******************************************************************************/
   public Integer getLimit()
   {
      return limit;
   }



   /*******************************************************************************
    ** Setter for limit
    **
    *******************************************************************************/
   public void setLimit(Integer limit)
   {
      this.limit = limit;
   }

}
