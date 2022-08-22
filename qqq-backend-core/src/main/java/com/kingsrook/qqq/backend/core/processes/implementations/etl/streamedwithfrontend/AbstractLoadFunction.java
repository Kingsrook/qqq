package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractLoadFunction implements BackendStep
{
   private List<QRecord> inputRecordPage = new ArrayList<>();
   private List<QRecord> outputRecordPage = new ArrayList<>();

   protected QBackendTransaction transaction;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendTransaction openTransaction(RunBackendStepInput runBackendStepInput) throws QException
   {
      this.transaction = doOpenTransaction(runBackendStepInput);
      return (transaction);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected abstract QBackendTransaction doOpenTransaction(RunBackendStepInput runBackendStepInput) throws QException;



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
