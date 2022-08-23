package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 ** Base class for the StreamedETL preview & execute steps
 *******************************************************************************/
public class BaseStreamedETLStep
{
   protected static final int PROCESS_OUTPUT_RECORD_LIST_LIMIT = 20;



   /*******************************************************************************
    **
    *******************************************************************************/
   protected AbstractExtractStep getExtractStep(RunBackendStepInput runBackendStepInput)
   {
      QCodeReference codeReference = (QCodeReference) runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_EXTRACT_CODE);
      return (QCodeLoader.getBackendStep(AbstractExtractStep.class, codeReference));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected AbstractTransformStep getTransformStep(RunBackendStepInput runBackendStepInput)
   {
      QCodeReference codeReference = (QCodeReference) runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_TRANSFORM_CODE);
      return (QCodeLoader.getBackendStep(AbstractTransformStep.class, codeReference));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected AbstractLoadStep getLoadStep(RunBackendStepInput runBackendStepInput)
   {
      QCodeReference codeReference = (QCodeReference) runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_LOAD_CODE);
      return (QCodeLoader.getBackendStep(AbstractLoadStep.class, codeReference));
   }

}
