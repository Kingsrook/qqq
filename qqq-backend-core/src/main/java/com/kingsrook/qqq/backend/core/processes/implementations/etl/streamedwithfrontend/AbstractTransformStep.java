package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Base class for the Transform logic of Streamed ETL processes.
 **
 ** Records are to be read out of the inputRecordPage field, and after transformation,
 ** should be written to the outputRecordPage.  That is to say, DO NOT use the
 ** recordList in the step input/output objects.
 *******************************************************************************/
public abstract class AbstractTransformStep implements BackendStep
{
   private List<QRecord> inputRecordPage  = new ArrayList<>();
   private List<QRecord> outputRecordPage = new ArrayList<>();



   /*******************************************************************************
    ** Allow subclasses to do an action before the run is complete - before any
    ** pages of records are passed in.
    *******************************************************************************/
   public void preRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    ** Allow subclasses to do an action after the run is complete - after the last
    ** page of records is passed in.
    *******************************************************************************/
   public void postRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    ** Getter for recordPage
    **
    *******************************************************************************/
   public List<QRecord> getInputRecordPage()
   {
      return inputRecordPage;
   }



   /*******************************************************************************
    ** Setter for recordPage
    **
    *******************************************************************************/
   public void setInputRecordPage(List<QRecord> inputRecordPage)
   {
      this.inputRecordPage = inputRecordPage;
   }



   /*******************************************************************************
    ** Getter for outputRecordPage
    **
    *******************************************************************************/
   public List<QRecord> getOutputRecordPage()
   {
      return outputRecordPage;
   }



   /*******************************************************************************
    ** Setter for outputRecordPage
    **
    *******************************************************************************/
   public void setOutputRecordPage(List<QRecord> outputRecordPage)
   {
      this.outputRecordPage = outputRecordPage;
   }
}
