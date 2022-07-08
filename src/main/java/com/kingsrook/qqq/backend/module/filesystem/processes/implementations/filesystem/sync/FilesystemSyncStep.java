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


import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.BackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** BackendStep to sync two filesystem tables (copying the new files to a 3rd
 ** location as well...)
 **
 *******************************************************************************/
@SuppressWarnings("unchecked")
public class FilesystemSyncStep implements BackendStep
{
   private static final Logger LOG = LogManager.getLogger(FilesystemSyncStep.class);

   public static final String STEP_NAME = "sync";



   /*******************************************************************************
    ** Execute the step - using the request as input, and the result as output.
    *******************************************************************************/
   @Override
   public void run(RunBackendStepRequest runBackendStepRequest, RunBackendStepResult runBackendStepResult) throws QException
   {
      QTableMetaData sourceTable     = runBackendStepRequest.getInstance().getTable(runBackendStepRequest.getValueString(FilesystemSyncProcess.FIELD_SOURCE_TABLE));
      QTableMetaData archiveTable    = runBackendStepRequest.getInstance().getTable(runBackendStepRequest.getValueString(FilesystemSyncProcess.FIELD_ARCHIVE_TABLE));
      QTableMetaData processingTable = runBackendStepRequest.getInstance().getTable(runBackendStepRequest.getValueString(FilesystemSyncProcess.FIELD_PROCESSING_TABLE));

      QBackendMetaData                 sourceBackend    = runBackendStepRequest.getInstance().getBackendForTable(sourceTable.getName());
      FilesystemBackendModuleInterface sourceModule     = (FilesystemBackendModuleInterface) new QBackendModuleDispatcher().getQBackendModule(sourceBackend);
      AbstractBaseFilesystemAction     sourceActionBase = sourceModule.getActionBase();
      sourceActionBase.preAction(sourceBackend);
      Map<String, Object> sourceFiles = getFileNames(sourceActionBase, sourceTable, sourceBackend);

      QBackendMetaData                 archiveBackend    = runBackendStepRequest.getInstance().getBackendForTable(archiveTable.getName());
      FilesystemBackendModuleInterface archiveModule     = (FilesystemBackendModuleInterface) new QBackendModuleDispatcher().getQBackendModule(archiveBackend);
      AbstractBaseFilesystemAction     archiveActionBase = archiveModule.getActionBase();
      archiveActionBase.preAction(archiveBackend);
      Set<String> archiveFiles = getFileNames(archiveActionBase, archiveTable, archiveBackend).keySet();

      QBackendMetaData                 processingBackend    = runBackendStepRequest.getInstance().getBackendForTable(processingTable.getName());
      FilesystemBackendModuleInterface processingModule     = (FilesystemBackendModuleInterface) new QBackendModuleDispatcher().getQBackendModule(processingBackend);
      AbstractBaseFilesystemAction     processingActionBase = processingModule.getActionBase();
      processingActionBase.preAction(processingBackend);

      Integer maxFilesToSync = runBackendStepRequest.getValueInteger(FilesystemSyncProcess.FIELD_MAX_FILES_TO_ARCHIVE);
      int syncedFileCount = 0;
      for(Map.Entry<String, Object> sourceEntry : sourceFiles.entrySet())
      {
         try
         {
            String sourceFileName = sourceEntry.getKey();
            if(!archiveFiles.contains(sourceFileName))
            {
               LOG.info("Syncing file [" + sourceFileName + "] to [" + archiveTable + "] and [" + processingTable + "]");
               InputStream inputStream = sourceActionBase.readFile(sourceEntry.getValue());
               byte[]      bytes       = inputStream.readAllBytes();

               String archivePath = archiveActionBase.getFullBasePath(archiveTable, archiveBackend);
               archiveActionBase.writeFile(archiveBackend, archivePath + File.separator + sourceFileName, bytes);

               String processingPath = processingActionBase.getFullBasePath(processingTable, processingBackend);
               processingActionBase.writeFile(processingBackend, processingPath + File.separator + sourceFileName, bytes);
               syncedFileCount++;

               if(maxFilesToSync != null && syncedFileCount >= maxFilesToSync)
               {
                  LOG.info("Breaking after syncing " + syncedFileCount + " files");
                  break;
               }
            }
         }
         catch(Exception e)
         {
            LOG.error("Error processing file: " + sourceEntry, e);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<String, Object> getFileNames(AbstractBaseFilesystemAction actionBase, QTableMetaData table, QBackendMetaData backend)
   {
      List<Object>        files = actionBase.listFiles(table, backend);
      Map<String, Object> rs    = new LinkedHashMap<>();

      for(Object file : files)
      {
         String fileName = actionBase.stripBackendAndTableBasePathsFromFileName(actionBase.getFullPathForFile(file), backend, table);
         rs.put(fileName, file);
      }

      return (rs);
   }

}
