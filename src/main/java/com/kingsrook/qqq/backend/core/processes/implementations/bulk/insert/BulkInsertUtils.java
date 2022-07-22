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
import java.util.Optional;
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.QUploadedFile;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.state.AbstractStateKey;
import com.kingsrook.qqq.backend.core.state.TempFileStateProvider;


/*******************************************************************************
 ** Utility methods used by bulk insert steps
 *******************************************************************************/
public class BulkInsertUtils
{
   /*******************************************************************************
    **
    *******************************************************************************/
   static List<QRecord> getQRecordsFromFile(RunBackendStepInput runBackendStepInput) throws QException
   {
      AbstractStateKey        stateKey             = (AbstractStateKey) runBackendStepInput.getValue("uploadedFileKey");
      Optional<QUploadedFile> optionalUploadedFile = TempFileStateProvider.getInstance().get(QUploadedFile.class, stateKey);
      if(optionalUploadedFile.isEmpty())
      {
         throw (new QException("Could not find uploaded file"));
      }

      byte[] bytes = optionalUploadedFile.get().getBytes();

      // todo - sniff out file type...
      String        tableName = runBackendStepInput.getTableName();
      List<QRecord> qRecords  = new CsvToQRecordAdapter().buildRecordsFromCsv(new String(bytes), runBackendStepInput.getInstance().getTable(tableName), null);

      ////////////////////////////////////////////////
      // remove values from any non-editable fields //
      ////////////////////////////////////////////////
      List<QFieldMetaData> nonEditableFields = runBackendStepInput.getTable().getFields().values().stream()
         .filter(f -> !f.getIsEditable())
         .toList();
      if(!nonEditableFields.isEmpty())
      {
         for(QRecord qRecord : qRecords)
         {
            for(QFieldMetaData nonEditableField : nonEditableFields)
            {
               qRecord.setValue(nonEditableField.getName(), null);
            }
         }
      }

      return (qRecords);
   }
}
