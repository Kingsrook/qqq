package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;


/*******************************************************************************
 ** Generic implementation of a LoadStep - that runs an Insert action for a
 ** specified table.
 *******************************************************************************/
public class LoadViaInsertStep extends AbstractLoadStep
{
   public static final String FIELD_DESTINATION_TABLE = "destinationTable";



   /*******************************************************************************
    ** Execute the backend step - using the request as input, and the result as output.
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      InsertInput insertInput = new InsertInput(runBackendStepInput.getInstance());
      insertInput.setSession(runBackendStepInput.getSession());
      insertInput.setTableName(runBackendStepInput.getValueString(FIELD_DESTINATION_TABLE));
      insertInput.setRecords(getInputRecordPage());
      getTransaction().ifPresent(insertInput::setTransaction);
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      getOutputRecordPage().addAll(insertOutput.getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Optional<QBackendTransaction> openTransaction(RunBackendStepInput runBackendStepInput) throws QException
   {
      InsertInput insertInput = new InsertInput(runBackendStepInput.getInstance());
      insertInput.setSession(runBackendStepInput.getSession());
      insertInput.setTableName(runBackendStepInput.getValueString(FIELD_DESTINATION_TABLE));

      return (Optional.of(new InsertAction().openTransaction(insertInput)));
   }
}
