package com.kingsrook.qqq.api;


import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.general.StandardProcessSummaryLineProducer;


/*******************************************************************************
 **
 *******************************************************************************/
public class TransformPersonStep extends AbstractTransformStep
{
   private ProcessSummaryLine okLine    = StandardProcessSummaryLineProducer.getOkToUpdateLine();
   private ProcessSummaryLine errorLine = StandardProcessSummaryLineProducer.getErrorLine();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      okLine.addSelfToListIfAnyCount(rs);
      errorLine.addSelfToListIfAnyCount(rs);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      for(QRecord record : runBackendStepInput.getRecords())
      {
         Integer id = record.getValueInteger("id");
         if(id % 2 == 0)
         {
            okLine.incrementCountAndAddPrimaryKey(id);
         }
         else
         {
            errorLine.incrementCountAndAddPrimaryKey(id);
         }
      }
   }

}
