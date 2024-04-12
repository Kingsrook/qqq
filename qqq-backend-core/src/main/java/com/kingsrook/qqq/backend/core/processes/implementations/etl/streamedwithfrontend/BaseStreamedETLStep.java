/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Base class for the StreamedETL preview & execute steps
 *******************************************************************************/
public class BaseStreamedETLStep
{
   private static final QLogger LOG = QLogger.getLogger(BaseStreamedETLStep.class);

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
      return (QCodeLoader.getAdHoc(AbstractTransformStep.class, codeReference));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected AbstractLoadStep getLoadStep(RunBackendStepInput runBackendStepInput)
   {
      QCodeReference codeReference = (QCodeReference) runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_LOAD_CODE);
      return (QCodeLoader.getAdHoc(AbstractLoadStep.class, codeReference));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void updateRecordsWithDisplayValuesAndPossibleValues(RunBackendStepInput input, List<QRecord> list)
   {
      String         destinationTable = input.getValueString(StreamedETLWithFrontendProcess.FIELD_DESTINATION_TABLE);
      QTableMetaData table            = input.getInstance().getTable(destinationTable);

      if(table != null && list != null)
      {
         QValueFormatter qValueFormatter = new QValueFormatter();
         qValueFormatter.setDisplayValuesInRecords(table, list);

         QPossibleValueTranslator qPossibleValueTranslator = new QPossibleValueTranslator(input.getInstance(), input.getSession());
         qPossibleValueTranslator.translatePossibleValuesInRecords(table, list);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void moveReviewStepAfterValidateStep(RunBackendStepOutput runBackendStepOutput)
   {
      LOG.debug("Skipping to validation step");
      ArrayList<String> stepList = new ArrayList<>(runBackendStepOutput.getProcessState().getStepList());
      LOG.trace("Step list pre: " + stepList);
      stepList.removeIf(s -> s.equals(StreamedETLWithFrontendProcess.STEP_NAME_REVIEW));
      stepList.add(stepList.indexOf(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE) + 1, StreamedETLWithFrontendProcess.STEP_NAME_REVIEW);
      runBackendStepOutput.getProcessState().setStepList(stepList);
      LOG.trace("Step list post: " + stepList);
   }
}
