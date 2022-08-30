package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


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



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void updateRecordsWithDisplayValuesAndPossibleValues(RunBackendStepInput input, List<QRecord> list)
   {
      String destinationTable = input.getValueString(StreamedETLWithFrontendProcess.FIELD_DESTINATION_TABLE);
      QTableMetaData table    = input.getInstance().getTable(destinationTable);

      if(table != null && list != null)
      {
         QValueFormatter qValueFormatter = new QValueFormatter();
         qValueFormatter.setDisplayValuesInRecords(table, list);

         QPossibleValueTranslator qPossibleValueTranslator = new QPossibleValueTranslator(input.getInstance(), input.getSession());
         qPossibleValueTranslator.translatePossibleValuesInRecords(input.getTable(), list);
      }
   }
}
