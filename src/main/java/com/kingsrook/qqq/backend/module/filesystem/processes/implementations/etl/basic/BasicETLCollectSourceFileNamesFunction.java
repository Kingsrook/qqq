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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.etl.basic;


import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.BackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;


/*******************************************************************************
 ** Function body for collecting the file names that were discovered in the
 ** Extract step.  These will be lost during the transform, so we capture them here,
 ** so that our Clean function can move or delete them.
 **
 ** TODO - need unit test!!
 *******************************************************************************/
public class BasicETLCollectSourceFileNamesFunction implements BackendStep
{
   public static final String FUNCTION_NAME           = "collectSourceFileNames";
   public static final String FIELD_SOURCE_FILE_PATHS = "sourceFilePaths";



   /*******************************************************************************
    ** Execute the function - using the request as input, and the result as output.
    *******************************************************************************/
   @Override
   public void run(RunBackendStepRequest runBackendStepRequest, RunBackendStepResult runBackendStepResult) throws QException
   {
      Set<String> sourceFiles = runBackendStepRequest.getRecords().stream()
         .map(record -> record.getBackendDetailString(FilesystemRecordBackendDetailFields.FULL_PATH))
         .collect(Collectors.toSet());
      runBackendStepResult.addValue(FIELD_SOURCE_FILE_PATHS, StringUtils.join(",", sourceFiles));
   }



   /*******************************************************************************
    ** define the metaData that describes this function
    *******************************************************************************/
   public QBackendStepMetaData defineStepMetaData()
   {
      return (new QBackendStepMetaData()
         .withName(FUNCTION_NAME)
         .withCode(new QCodeReference()
            .withName(this.getClass().getName())
            .withCodeType(QCodeType.JAVA)
            .withCodeUsage(QCodeUsage.FUNCTION))
         .withOutputMetaData(new QFunctionOutputMetaData()
            .addField(new QFieldMetaData(FIELD_SOURCE_FILE_PATHS, QFieldType.STRING))));
   }
}
