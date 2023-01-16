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
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLProcess;
import com.kingsrook.qqq.backend.core.utils.QLogger;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;


/*******************************************************************************
 ** BackendStep for performing the Cleanup step of a basic ETL process - e.g.,
 ** after the loading, delete or move the processed file(s).
 *******************************************************************************/
public class BasicETLCleanupSourceFilesStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(BasicETLCleanupSourceFilesStep.class);

   public static final String FIELD_MOVE_OR_DELETE        = "moveOrDelete";
   public static final String FIELD_DESTINATION_FOR_MOVES = "destinationForMoves";

   public static final String VALUE_MOVE   = "move";
   public static final String VALUE_DELETE = "delete";
   public static final String STEP_NAME    = "cleanupSourceFiles";



   /*******************************************************************************
    ** Execute the step - using the request as input, and the result as output.
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String                  sourceTableName = runBackendStepInput.getValueString(BasicETLProcess.FIELD_SOURCE_TABLE);
      QTableMetaData          table           = runBackendStepInput.getInstance().getTable(sourceTableName);
      QBackendMetaData        backend         = runBackendStepInput.getInstance().getBackendForTable(sourceTableName);
      QBackendModuleInterface module          = new QBackendModuleDispatcher().getQBackendModule(backend);

      if(!(module instanceof FilesystemBackendModuleInterface filesystemModule))
      {
         throw (new QException("Backend " + table.getBackendName() + " for table " + sourceTableName + " does not support this action."));
      }
      AbstractBaseFilesystemAction actionBase = filesystemModule.getActionBase();
      actionBase.preAction(backend);

      String sourceFilePaths = runBackendStepInput.getValueString(BasicETLCollectSourceFileNamesStep.FIELD_SOURCE_FILE_PATHS);
      if(!StringUtils.hasContent(sourceFilePaths))
      {
         LOG.debug("No source file paths were specified in field [" + BasicETLCollectSourceFileNamesStep.FIELD_SOURCE_FILE_PATHS + "]");
         return;
      }

      String[] sourceFiles = sourceFilePaths.split(",");
      for(String sourceFile : sourceFiles)
      {
         String moveOrDelete = runBackendStepInput.getValueString(FIELD_MOVE_OR_DELETE);
         if(VALUE_DELETE.equals(moveOrDelete))
         {
            LOG.info("Deleting ETL source file: " + sourceFile);
            actionBase.deleteFile(runBackendStepInput.getInstance(), table, sourceFile);
         }
         else if(VALUE_MOVE.equals(moveOrDelete))
         {
            String destinationForMoves = runBackendStepInput.getValueString(FIELD_DESTINATION_FOR_MOVES);
            LOG.info("Moving ETL source file: " + sourceFile + " to " + destinationForMoves);
            if(!StringUtils.hasContent(destinationForMoves))
            {
               throw (new QException("Field [" + FIELD_DESTINATION_FOR_MOVES + "] is missing a value."));
            }
            String filePathWithoutBase = actionBase.stripBackendAndTableBasePathsFromFileName(sourceFile, backend, table);
            String destinationPath     = destinationForMoves + File.separator + filePathWithoutBase;
            actionBase.moveFile(runBackendStepInput.getInstance(), table, sourceFile, destinationPath);
         }
         else
         {
            throw (new QException("Unexpected value [" + moveOrDelete + "] for field [" + FIELD_MOVE_OR_DELETE + "].  "
               + "Must be either [" + VALUE_MOVE + "] or [" + VALUE_DELETE + "]."));
         }
      }
   }



   /*******************************************************************************
    ** define the metaData that describes this step
    *******************************************************************************/
   public QBackendStepMetaData defineStepMetaData()
   {
      return (new QBackendStepMetaData()
         .withName(STEP_NAME)
         .withCode(new QCodeReference()
            .withName(this.getClass().getName())
            .withCodeType(QCodeType.JAVA)
            .withCodeUsage(QCodeUsage.BACKEND_STEP))
         .withInputData(new QFunctionInputMetaData()
            .addField(new QFieldMetaData("moveOrDelete", QFieldType.STRING))
            .addField(new QFieldMetaData("destinationForMoves", QFieldType.STRING))));
   }
}
