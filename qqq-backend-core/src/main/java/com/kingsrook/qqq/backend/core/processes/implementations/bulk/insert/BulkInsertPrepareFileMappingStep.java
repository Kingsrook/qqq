/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping.BulkLoadMappingSuggester;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping.BulkLoadTableStructureBuilder;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadTableStructure;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkInsertPrepareFileMappingStep implements BackendStep
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      buildFileDetailsForMappingStep(runBackendStepInput, runBackendStepOutput);

      String                 tableName      = runBackendStepInput.getValueString("tableName");
      BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(tableName);
      runBackendStepOutput.addValue("tableStructure", tableStructure);

      boolean needSuggestedMapping = true;
      if(runBackendStepOutput.getProcessState().getIsStepBack())
      {
         needSuggestedMapping = false;

         StreamedETLWithFrontendProcess.resetValidationFields(runBackendStepInput);
      }

      if(needSuggestedMapping)
      {
         @SuppressWarnings("unchecked")
         List<String> headerValues = (List<String>) runBackendStepOutput.getValue("headerValues");
         buildSuggestedMapping(headerValues, tableStructure, runBackendStepOutput);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void buildSuggestedMapping(List<String> headerValues, BulkLoadTableStructure tableStructure, RunBackendStepOutput runBackendStepOutput)
   {
      BulkLoadMappingSuggester bulkLoadMappingSuggester = new BulkLoadMappingSuggester();
      BulkLoadProfile          bulkLoadProfile          = bulkLoadMappingSuggester.suggestBulkLoadMappingProfile(tableStructure, headerValues);
      runBackendStepOutput.addValue("bulkLoadProfile", bulkLoadProfile);
      runBackendStepOutput.addValue("suggestedBulkLoadProfile", bulkLoadProfile);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void buildFileDetailsForMappingStep(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      StorageInput storageInput = BulkInsertStepUtils.getStorageInputForTheFile(runBackendStepInput);
      File         file         = new File(storageInput.getReference());
      runBackendStepOutput.addValue("fileBaseName", file.getName());

      try
         (
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            // open a stream to read from our file, and a FileToRows object, that knows how to read from that stream //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            InputStream inputStream = new StorageAction().getInputStream(storageInput);
            FileToRowsInterface fileToRowsInterface = FileToRowsInterface.forFile(storageInput.getReference(), inputStream);
         )
      {
         /////////////////////////////////////////////////
         // read the 1st row, and assume it is a header //
         /////////////////////////////////////////////////
         BulkLoadFileRow   headerRow     = fileToRowsInterface.next();
         ArrayList<String> headerValues  = new ArrayList<>();
         ArrayList<String> headerLetters = new ArrayList<>();
         for(int i = 0; i < headerRow.size(); i++)
         {
            headerValues.add(ValueUtils.getValueAsString(headerRow.getValue(i)));
            headerLetters.add(toHeaderLetter(i));
         }
         runBackendStepOutput.addValue("headerValues", headerValues);
         runBackendStepOutput.addValue("headerLetters", headerLetters);

         ///////////////////////////////////////////////////////////////////////////////////////////
         // while there are more rows in the file - and we're under preview-rows limit, read rows //
         ///////////////////////////////////////////////////////////////////////////////////////////
         int                          previewRows      = 0;
         int                          previewRowsLimit = 5;
         ArrayList<ArrayList<String>> bodyValues       = new ArrayList<>();
         for(int i = 0; i < headerRow.size(); i++)
         {
            bodyValues.add(new ArrayList<>());
         }

         while(fileToRowsInterface.hasNext() && previewRows < previewRowsLimit)
         {
            BulkLoadFileRow bodyRow = fileToRowsInterface.next();
            previewRows++;

            for(int i = 0; i < headerRow.size(); i++)
            {
               bodyValues.get(i).add(ValueUtils.getValueAsString(bodyRow.getValueElseNull(i)));
            }
         }
         runBackendStepOutput.addValue("bodyValuesPreview", bodyValues);

      }
      catch(Exception e)
      {
         throw (new QException("Error reading bulk load file", e));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   static String toHeaderLetter(int i)
   {
      StringBuilder rs = new StringBuilder();

      do
      {
         rs.insert(0, (char) ('A' + (i % 26)));
         i = (i / 26) - 1;
      }
      while(i >= 0);

      return (rs.toString());
   }

}
