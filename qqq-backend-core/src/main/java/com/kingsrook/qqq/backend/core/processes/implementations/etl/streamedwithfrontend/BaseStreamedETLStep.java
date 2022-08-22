package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 ** Base class for the StreamedETL preview & execute steps
 *******************************************************************************/
public class BaseStreamedETLStep
{
   protected static final int IN_MEMORY_RECORD_LIMIT = 20;



   /*******************************************************************************
    **
    *******************************************************************************/
   protected AbstractExtractFunction getExtractFunction(RunBackendStepInput runBackendStepInput)
   {
      QCodeReference codeReference = (QCodeReference) runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_EXTRACT_CODE);
      return (QCodeLoader.getBackendStep(AbstractExtractFunction.class, codeReference));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected AbstractTransformFunction getTransformFunction(RunBackendStepInput runBackendStepInput)
   {
      QCodeReference codeReference = (QCodeReference) runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_TRANSFORM_CODE);
      return (QCodeLoader.getBackendStep(AbstractTransformFunction.class, codeReference));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected AbstractLoadFunction getLoadFunction(RunBackendStepInput runBackendStepInput)
   {
      QCodeReference codeReference = (QCodeReference) runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_LOAD_CODE);
      return (QCodeLoader.getBackendStep(AbstractLoadFunction.class, codeReference));
   }

}
