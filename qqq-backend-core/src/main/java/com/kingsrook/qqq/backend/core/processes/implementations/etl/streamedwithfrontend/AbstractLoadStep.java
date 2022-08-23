package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Base class for the Load (aka, store) logic of Streamed ETL processes.
 **
 ** Records are to be read out of the inputRecordPage field, and after storing,
 ** should be written to the outputRecordPage.  That is to say, DO NOT use the
 ** recordList in the step input/output objects.
 **
 ** Also - use the transaction member variable - though be aware, it
 *******************************************************************************/
public abstract class AbstractLoadStep implements BackendStep
{
   private List<QRecord> inputRecordPage  = new ArrayList<>();
   private List<QRecord> outputRecordPage = new ArrayList<>();

   private Optional<QBackendTransaction> transaction = Optional.empty();



   /*******************************************************************************
    **
    *******************************************************************************/
   public Optional<QBackendTransaction> openTransaction(RunBackendStepInput runBackendStepInput) throws QException
   {
      return (Optional.empty());
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



   /*******************************************************************************
    ** Setter for transaction
    **
    *******************************************************************************/
   public void setTransaction(Optional<QBackendTransaction> transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    ** Getter for transaction
    **
    *******************************************************************************/
   public Optional<QBackendTransaction> getTransaction()
   {
      return (transaction);
   }
}
