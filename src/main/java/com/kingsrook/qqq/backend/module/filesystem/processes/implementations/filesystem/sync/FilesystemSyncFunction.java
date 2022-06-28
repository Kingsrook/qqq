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
import com.kingsrook.qqq.backend.core.interfaces.FunctionBody;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Function body for collecting the file names that were discovered in the
 ** Extract step.  These will be lost during the transform, so we capture them here,
 ** so that our Clean function can move or delete them.
 **
 *******************************************************************************/
public class FilesystemSyncFunction implements FunctionBody
{
   private static final Logger LOG = LogManager.getLogger(FilesystemSyncFunction.class);

   public static final String FUNCTION_NAME = "sync";



   /*******************************************************************************
    ** Execute the function - using the request as input, and the result as output.
    *******************************************************************************/
   @Override
   public void run(RunFunctionRequest runFunctionRequest, RunFunctionResult runFunctionResult) throws QException
   {
      QTableMetaData sourceTable     = runFunctionRequest.getInstance().getTable(runFunctionRequest.getValueString(FilesystemSyncProcess.FIELD_SOURCE_TABLE));
      QTableMetaData archiveTable    = runFunctionRequest.getInstance().getTable(runFunctionRequest.getValueString(FilesystemSyncProcess.FIELD_ARCHIVE_TABLE));
      QTableMetaData processingTable = runFunctionRequest.getInstance().getTable(runFunctionRequest.getValueString(FilesystemSyncProcess.FIELD_PROCESSING_TABLE));

      QBackendMetaData                 sourceBackend = runFunctionRequest.getInstance().getBackendForTable(sourceTable.getName());
      FilesystemBackendModuleInterface sourceModule  = (FilesystemBackendModuleInterface) new QBackendModuleDispatcher().getQBackendModule(sourceBackend);
      Map<String, Object>              sourceFiles   = getFileNames(sourceModule.getActionBase(), sourceTable, sourceBackend);

      QBackendMetaData                 archiveBackend = runFunctionRequest.getInstance().getBackendForTable(archiveTable.getName());
      FilesystemBackendModuleInterface archiveModule  = (FilesystemBackendModuleInterface) new QBackendModuleDispatcher().getQBackendModule(archiveBackend);
      Set<String>                      archiveFiles   = getFileNames(archiveModule.getActionBase(), archiveTable, archiveBackend).keySet();

      QBackendMetaData                 processingBackend = runFunctionRequest.getInstance().getBackendForTable(processingTable.getName());
      FilesystemBackendModuleInterface processingModule  = (FilesystemBackendModuleInterface) new QBackendModuleDispatcher().getQBackendModule(processingBackend);

      for(Map.Entry<String, Object> sourceEntry : sourceFiles.entrySet())
      {
         try
         {
            String sourceFileName = sourceEntry.getKey();
            if(!archiveFiles.contains(sourceFileName))
            {
               LOG.info("Syncing file [" + sourceFileName + "] to [" + archiveTable + "] and [" + processingTable + "]");
               InputStream inputStream = sourceModule.getActionBase().readFile(sourceEntry.getValue());
               byte[]      bytes       = inputStream.readAllBytes();

               String archivePath = archiveModule.getActionBase().getFullBasePath(archiveTable, archiveBackend);
               archiveModule.getActionBase().writeFile(archiveBackend, archivePath + File.separator + sourceFileName, bytes);

               String processingPath = processingModule.getActionBase().getFullBasePath(processingTable, processingBackend);
               processingModule.getActionBase().writeFile(processingBackend, processingPath + File.separator + sourceFileName, bytes);
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
         String fileName = actionBase.stripBackendAndTableBasePathsFromFileName(file, backend, table);
         rs.put(fileName, file);
      }

      return (rs);
   }

}
