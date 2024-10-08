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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.QUploadedFile;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QKeyBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractExtractStep;
import com.kingsrook.qqq.backend.core.state.AbstractStateKey;
import com.kingsrook.qqq.backend.core.state.TempFileStateProvider;


/*******************************************************************************
 ** Extract step for generic table bulk-insert ETL process
 *******************************************************************************/
public class BulkInsertExtractStep extends AbstractExtractStep
{
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      AbstractStateKey        stateKey             = (AbstractStateKey) runBackendStepInput.getValue(QUploadedFile.DEFAULT_UPLOADED_FILE_FIELD_NAME);
      Optional<QUploadedFile> optionalUploadedFile = TempFileStateProvider.getInstance().get(QUploadedFile.class, stateKey);
      if(optionalUploadedFile.isEmpty())
      {
         throw (new QException("Could not find uploaded file"));
      }

      byte[] bytes    = optionalUploadedFile.get().getBytes();
      String fileName = optionalUploadedFile.get().getFilename();

      /////////////////////////////////////////////////////
      // let the user specify field labels instead names //
      /////////////////////////////////////////////////////
      QTableMetaData        table     = runBackendStepInput.getTable();
      String                tableName = runBackendStepInput.getTableName();
      QKeyBasedFieldMapping mapping   = new QKeyBasedFieldMapping();
      for(Map.Entry<String, QFieldMetaData> entry : table.getFields().entrySet())
      {
         mapping.addMapping(entry.getKey(), entry.getValue().getLabel());
      }

      //////////////////////////////////////////////////////////////////////////
      // get the non-editable fields - they'll be blanked out in a customizer //
      //////////////////////////////////////////////////////////////////////////
      List<QFieldMetaData> nonEditableFields = table.getFields().values().stream()
         .filter(f -> !f.getIsEditable())
         .toList();

      if(fileName.toLowerCase(Locale.ROOT).endsWith(".csv"))
      {
         new CsvToQRecordAdapter().buildRecordsFromCsv(new CsvToQRecordAdapter.InputWrapper()
            .withRecordPipe(getRecordPipe())
            .withLimit(getLimit())
            .withCsv(new String(bytes))
            .withDoCorrectValueTypes(true)
            .withTable(QContext.getQInstance().getTable(tableName))
            .withMapping(mapping)
            .withRecordCustomizer((record) ->
            {
               ////////////////////////////////////////////
               // remove values from non-editable fields //
               ////////////////////////////////////////////
               for(QFieldMetaData nonEditableField : nonEditableFields)
               {
                  record.setValue(nonEditableField.getName(), null);
               }
            }));
      }
      else
      {
         throw (new QUserFacingException("Unsupported file type."));
      }
   }
}
