package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;


/*******************************************************************************
 ** Base class for the Extract logic of Streamed ETL processes.
 **
 ** These steps are invoked by both the "preview" and the "execute" steps of a
 ** StreamedETLWithFrontend process.
 **
 ** Key here, is that subclasses here should put records that they're "Extracting"
 ** into the recordPipe member.  That is to say, DO NOT use the recordList in
 ** the Step input/output objects.
 **
 ** Ideally, they'll also stop once they've hit the "limit" number of records
 ** (though if you keep going, the pipe will get terminated and the job will be
 ** cancelled, etc...).
 *******************************************************************************/
public abstract class AbstractExtractStep implements BackendStep
{
   private RecordPipe recordPipe;
   private Integer    limit;



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer doCount(RunBackendStepInput runBackendStepInput) throws QException
   {
      return (null);
   }



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
