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


import java.io.File;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.BackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLProcess;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Function body for performing the Cleanup step of a basic ETL process - e.g.,
 ** after the loading, delete or move the processed file(s).
 *******************************************************************************/
public class BasicETLCleanupSourceFilesFunction implements BackendStep
{
   private static final Logger LOG = LogManager.getLogger(BasicETLCleanupSourceFilesFunction.class);

   public static final String FIELD_MOVE_OR_DELETE        = "moveOrDelete";
   public static final String FIELD_DESTINATION_FOR_MOVES = "destinationForMoves";

   public static final String VALUE_MOVE    = "move";
   public static final String VALUE_DELETE  = "delete";
   public static final String FUNCTION_NAME = "cleanupSourceFiles";



   /*******************************************************************************
    ** Execute the function - using the request as input, and the result as output.
    *******************************************************************************/
   @Override
   public void run(RunBackendStepRequest runBackendStepRequest, RunBackendStepResult runBackendStepResult) throws QException
   {
      String                  sourceTableName = runBackendStepRequest.getValueString(BasicETLProcess.FIELD_SOURCE_TABLE);
      QTableMetaData          table           = runBackendStepRequest.getInstance().getTable(sourceTableName);
      QBackendMetaData        backend         = runBackendStepRequest.getInstance().getBackendForTable(sourceTableName);
      QBackendModuleInterface module          = new QBackendModuleDispatcher().getQBackendModule(backend);

      if(!(module instanceof FilesystemBackendModuleInterface filesystemModule))
      {
         throw (new QException("Backend " + table.getBackendName() + " for table " + sourceTableName + " does not support this function."));
      }
      AbstractBaseFilesystemAction actionBase = filesystemModule.getActionBase();
      actionBase.preAction(backend);

      String sourceFilePaths = runBackendStepRequest.getValueString(BasicETLCollectSourceFileNamesFunction.FIELD_SOURCE_FILE_PATHS);
      if(!StringUtils.hasContent(sourceFilePaths))
      {
         LOG.info("No source file paths were specified in field [" + BasicETLCollectSourceFileNamesFunction.FIELD_SOURCE_FILE_PATHS + "]");
         return;
      }

      String[] sourceFiles = sourceFilePaths.split(",");
      for(String sourceFile : sourceFiles)
      {
         String moveOrDelete = runBackendStepRequest.getValueString(FIELD_MOVE_OR_DELETE);
         if(VALUE_DELETE.equals(moveOrDelete))
         {
            actionBase.deleteFile(runBackendStepRequest.getInstance(), table, sourceFile);
         }
         else if(VALUE_MOVE.equals(moveOrDelete))
         {
            String destinationForMoves = runBackendStepRequest.getValueString(FIELD_DESTINATION_FOR_MOVES);
            if(!StringUtils.hasContent(destinationForMoves))
            {
               throw (new QException("Field [" + FIELD_DESTINATION_FOR_MOVES + "] is missing a value."));
            }
            String filePathWithoutBase = actionBase.stripBackendAndTableBasePathsFromFileName(sourceFile, backend, table);
            String destinationPath     = destinationForMoves + File.separator + filePathWithoutBase;
            actionBase.moveFile(runBackendStepRequest.getInstance(), table, sourceFile, destinationPath);
         }
         else
         {
            throw (new QException("Unexpected value [" + moveOrDelete + "] for field [" + FIELD_MOVE_OR_DELETE + "].  "
               + "Must be either [" + VALUE_MOVE + "] or [" + VALUE_DELETE + "]."));
         }
      }
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
         .withInputData(new QFunctionInputMetaData()
            .addField(new QFieldMetaData("moveOrDelete", QFieldType.STRING))
            .addField(new QFieldMetaData("destinationForMoves", QFieldType.STRING))));
   }
}
