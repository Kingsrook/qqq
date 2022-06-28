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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.filesystem.sync;


import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Definition for Filesystem sync process.
 **
 ** Job is to:
 ** - list all files in the source table.
 ** - list all files in the archive table.
 ** - if any files exist in the source, but not in the archive, then:
 **   - copy the file to both the archive and the processing table.
 **
 ** The idea being, that the source is read-only, and we want to move files out of
 ** processing after they've been processed - and the archive is what we can have
 ** in-between the two.
 *******************************************************************************/
public class FilesystemSyncProcess
{
   public static final String PROCESS_NAME = "filesystem.sync";

   public static final String FIELD_SOURCE_TABLE     = "sourceTable";
   public static final String FIELD_ARCHIVE_TABLE    = "archiveTable";
   public static final String FIELD_PROCESSING_TABLE = "processingTable";



   /*******************************************************************************
    **
    *******************************************************************************/
   public QProcessMetaData defineProcessMetaData()
   {
      QFunctionMetaData syncFunction = new QFunctionMetaData()
         .withName(FilesystemSyncFunction.FUNCTION_NAME)
         .withCode(new QCodeReference()
            .withName(FilesystemSyncFunction.class.getName())
            .withCodeType(QCodeType.JAVA)
            .withCodeUsage(QCodeUsage.FUNCTION))
         .withInputData(new QFunctionInputMetaData()
            .addField(new QFieldMetaData(FIELD_SOURCE_TABLE, QFieldType.STRING))
            .addField(new QFieldMetaData(FIELD_ARCHIVE_TABLE, QFieldType.STRING))
            .addField(new QFieldMetaData(FIELD_PROCESSING_TABLE, QFieldType.STRING)));

      return new QProcessMetaData()
         .withName(PROCESS_NAME)
         .addFunction(syncFunction);
   }
}
