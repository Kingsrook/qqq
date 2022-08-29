package com.kingsrook.sampleapp.processes.clonepeople;


import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.sampleapp.SampleMetaDataProvider;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for ClonePeopleTransformStep
 *******************************************************************************/
class ClonePeopleTransformStepTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testProcessStep() throws QException
   {
      QInstance qInstance = SampleMetaDataProvider.defineInstance();

      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setTableName(SampleMetaDataProvider.TABLE_NAME_PERSON);
      queryInput.setSession(new QSession());
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      RunBackendStepInput      input                    = new RunBackendStepInput();
      RunBackendStepOutput     output                   = new RunBackendStepOutput();
      ClonePeopleTransformStep clonePeopleTransformStep = new ClonePeopleTransformStep();

      clonePeopleTransformStep.setInputRecordPage(queryOutput.getRecords());
      clonePeopleTransformStep.run(input, output);

      ArrayList<ProcessSummaryLine> processSummary = clonePeopleTransformStep.getProcessSummary(true);

      assertThat(processSummary)
         .usingRecursiveFieldByFieldElementComparatorOnFields("status", "count")
         .contains(new ProcessSummaryLine(Status.OK, 4, null))
         .contains(new ProcessSummaryLine(Status.ERROR, 1, null));
   }

}