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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.filesystem.importer;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.adapters.JsonToQRecordAdapter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** BackendStep for FilesystemImporter process
 **
 ** Job is to:
 ** - foreach file in the `source` table (e.g., a ONE-type filesystem table):
 **   - optionally create an archive/backup copy of the file
 **   - create a record in the `importFile` table
 **   - parse the file, creating many records in the `importRecord` table
 **   - remove the file from the `source` (if so configured (e.g., may turn off for Read-only FS))
 *******************************************************************************/
@SuppressWarnings("unchecked")
public class FilesystemImporterStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(FilesystemImporterStep.class);

   public static final String FIELD_SOURCE_TABLE        = "sourceTable";
   public static final String FIELD_FILE_FORMAT         = "fileFormat";
   public static final String FIELD_IMPORT_FILE_TABLE   = "importFileTable";
   public static final String FIELD_IMPORT_RECORD_TABLE = "importRecordTable";

   public static final String FIELD_ARCHIVE_FILE_ENABLED     = "archiveFileEnabled";
   public static final String FIELD_ARCHIVE_TABLE_NAME       = "archiveTableName";
   public static final String FIELD_ARCHIVE_PATH             = "archivePath";
   public static final String FIELD_REMOVE_FILE_AFTER_IMPORT = "removeFileAfterImport";

   public static final String FIELD_UPDATE_FILE_IF_NAME_EXISTS = "updateFileIfNameExists";



   /*******************************************************************************
    ** Execute the step - using the request as input, and the result as output.
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////////////////
      // defer to a private method here, so we can add a type-parameter for that method to use              //
      // would think we could do that here, but get compiler error, since this method comes from base class //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////
      doRun(runBackendStepInput, runBackendStepOutput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private <F> void doRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String  fileFormat             = runBackendStepInput.getValueString(FIELD_FILE_FORMAT);
      Boolean removeFileAfterImport  = runBackendStepInput.getValueBoolean(FIELD_REMOVE_FILE_AFTER_IMPORT);
      Boolean updateFileIfNameExists = runBackendStepInput.getValueBoolean(FIELD_UPDATE_FILE_IF_NAME_EXISTS);
      Boolean archiveFileEnabled     = runBackendStepInput.getValueBoolean(FIELD_ARCHIVE_FILE_ENABLED);

      QTableMetaData sourceTable     = runBackendStepInput.getInstance().getTable(runBackendStepInput.getValueString(FIELD_SOURCE_TABLE));
      QTableMetaData importFileTable = runBackendStepInput.getInstance().getTable(runBackendStepInput.getValueString(FIELD_IMPORT_FILE_TABLE));

      String missingFieldErrorPrefix = "Process " + runBackendStepInput.getProcessName() + " was misconfigured - missing value in field: ";
      Objects.requireNonNull(fileFormat, missingFieldErrorPrefix + FIELD_FILE_FORMAT);

      ///////////////////////////////////////////////////////////////////////////////////
      // list files in the backend system                                              //
      // todo - can we do this using query action, with this being a "ONE" type table? //
      ///////////////////////////////////////////////////////////////////////////////////
      QBackendMetaData                    sourceBackend    = runBackendStepInput.getInstance().getBackendForTable(sourceTable.getName());
      FilesystemBackendModuleInterface<F> sourceModule     = (FilesystemBackendModuleInterface<F>) new QBackendModuleDispatcher().getQBackendModule(sourceBackend);
      AbstractBaseFilesystemAction<F>     sourceActionBase = sourceModule.getActionBase();
      sourceActionBase.preAction(sourceBackend);
      Map<String, F> sourceFiles = getFileNames(sourceActionBase, sourceTable, sourceBackend);

      if(CollectionUtils.nullSafeIsEmpty(sourceFiles))
      {
         LOG.debug("No files found in import filesystem", logPair("sourceTable", sourceTable));
         return;
      }

      ////////////////////////////////////////////////////////
      // look up any existing file records with those names //
      ////////////////////////////////////////////////////////
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(importFileTable.getName());
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria("sourceFileName", QCriteriaOperator.IN, sourceFiles.keySet())));

      QueryOutput               queryOutput           = new QueryAction().execute(queryInput);
      Map<String, Serializable> existingImportedFiles = CollectionUtils.listToMap(queryOutput.getRecords(), r -> r.getValueString("sourceFileName"), r -> r.getValue("id"));

      for(Map.Entry<String, F> sourceEntry : sourceFiles.entrySet())
      {
         QBackendTransaction transaction = null;
         try
         {
            String sourceFileName = sourceEntry.getKey();

            /////////////////////////////////////////////////////////
            // if filename was already imported, decide what to do //
            /////////////////////////////////////////////////////////
            boolean      alreadyImported = existingImportedFiles.containsKey(sourceFileName);
            Serializable idToUpdate      = null;
            if(alreadyImported)
            {
               //////////////////////////////////////////////////////////////////////////////////
               // todo - would we want to support importing multiple-times the same file name? //
               // possibly - if so, add it here, presumably w/ another boolean field           //
               //////////////////////////////////////////////////////////////////////////////////
               if(updateFileIfNameExists)
               {
                  LOG.info("Updating already-imported file", logPair("fileName", sourceFileName), logPair("id", idToUpdate));
                  idToUpdate = existingImportedFiles.get(sourceFileName);
               }
               else
               {
                  LOG.debug("Skipping already-imported file", logPair("fileName", sourceFileName));
                  continue;
               }
            }

            ///////////////////////////////////
            // read the file as input stream //
            ///////////////////////////////////
            try(InputStream inputStream = sourceActionBase.readFile(sourceEntry.getValue()))
            {
               byte[] bytes = inputStream.readAllBytes();

               //////////////////////////////////////
               // archive the file, if so directed //
               //////////////////////////////////////
               String archivedPath = null;
               if(archiveFileEnabled)
               {
                  archivedPath = archiveFile(runBackendStepInput, sourceFileName, bytes);
               }

               /////////////////////////////////
               // build record for importFile //
               /////////////////////////////////
               LOG.info("Syncing file [" + sourceFileName + "]");
               QRecord importFileRecord = new QRecord()
                  // todo - how to get clientId in here?
                  .withValue("id", idToUpdate)
                  .withValue("sourceFileName", sourceFileName)
                  .withValue("archivedPath", archivedPath);

               //////////////////////////////////////
               // build child importRecord records //
               //////////////////////////////////////
               String content = new String(bytes);
               importFileRecord.withAssociatedRecords("importRecords", parseFileIntoRecords(runBackendStepInput, content));

               ///////////////////////////////////////////////////////////////////
               // insert the file & records (records as association under file) //
               ///////////////////////////////////////////////////////////////////
               InsertAction insertAction = new InsertAction();
               InsertInput  insertInput  = new InsertInput();
               insertInput.setTableName(importFileTable.getName());
               insertInput.setRecords(List.of(importFileRecord));

               transaction = QBackendTransaction.openFor(insertInput);
               insertInput.setTransaction(transaction);

               InsertOutput insertOutput = insertAction.execute(insertInput);

               LOG.info("Inserted insertFile & records", logPair("id", insertOutput.getRecords().get(0).getValue("id")));

               transaction.commit();
            }

            ///////////////////////////////////////////////////////////////////////////////////////////////
            // after the records are built, we can delete the file                                       //
            // if we are interrupted between the commit & the delete, then the file will be found again, //
            // and we'll either skip it or do an update, based on FIELD_UPDATE_FILE_IF_NAME_EXISTS flag  //
            ///////////////////////////////////////////////////////////////////////////////////////////////
            if(removeFileAfterImport)
            {
               String fullBasePath = sourceActionBase.getFullBasePath(sourceTable, sourceBackend);
               sourceActionBase.deleteFile(QContext.getQInstance(), sourceTable, fullBasePath + "/" + sourceFileName);
            }
         }
         catch(Exception e)
         {
            LOG.error("Error processing file: " + sourceEntry, e);
            if(transaction != null)
            {
               transaction.rollback();
            }
         }
         finally
         {
            if(transaction != null)
            {
               transaction.close();
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String archiveFile(RunBackendStepInput runBackendStepInput, String sourceFileName, byte[] bytes) throws QException, IOException
   {
      String         archiveTableName = runBackendStepInput.getValueString(FIELD_ARCHIVE_TABLE_NAME);
      QTableMetaData archiveTable;
      try
      {
         archiveTable = runBackendStepInput.getInstance().getTable(archiveTableName);
      }
      catch(Exception e)
      {
         throw (new QException("Error getting archive table [" + archiveTableName + "]", e));
      }

      String archivePath = Objects.requireNonNullElse(runBackendStepInput.getValueString(FIELD_ARCHIVE_PATH), "");

      QBackendMetaData                    archiveBackend    = runBackendStepInput.getInstance().getBackendForTable(archiveTable.getName());
      FilesystemBackendModuleInterface<?> archiveModule     = (FilesystemBackendModuleInterface<?>) new QBackendModuleDispatcher().getQBackendModule(archiveBackend);
      AbstractBaseFilesystemAction<?>     archiveActionBase = archiveModule.getActionBase();
      archiveActionBase.preAction(archiveBackend);

      LocalDateTime now = LocalDateTime.now();
      String path = archiveActionBase.getFullBasePath(archiveTable, archiveBackend)
         + File.separator + archivePath
         + File.separator + now.getYear()
         + File.separator + now.getMonth()
         + File.separator + UUID.randomUUID()
         + "-" + sourceFileName.replaceAll(".*" + File.separator, "");
      LOG.info("Archiving file", logPair("path", path));
      archiveActionBase.writeFile(archiveBackend, path, bytes);

      return (path);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   List<QRecord> parseFileIntoRecords(RunBackendStepInput runBackendStepInput, String content) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      // first, parse the content into records, w/ unknown field names - just whatever is in the CSV or JSON //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      String fileFormat = runBackendStepInput.getValueString(FIELD_FILE_FORMAT);

      List<QRecord> contentRecords = switch(fileFormat.toLowerCase())
      {
         case "csv" ->
         {
            CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
            csvToQRecordAdapter.buildRecordsFromCsv(new CsvToQRecordAdapter.InputWrapper()
               .withCsv(content)
               .withCaseSensitiveHeaders(true)
               .withCsvHeadersAsFieldNames(true)
            );
            yield (csvToQRecordAdapter.getRecordList());
         }

         case "json" -> new JsonToQRecordAdapter().buildRecordsFromJson(content, null, null);

         default -> throw (new QException("Unexpected file format: " + fileFormat));
      };

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now, wrap those records with the fields of the importRecord table, putting the unknown fields in a blob together //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      List<QRecord> importRecordList = new ArrayList<>();
      int           recordNo         = 1;
      for(QRecord record : contentRecords)
      {
         record.setValue("recordNo", recordNo++);
         // todo - client_id??

         importRecordList.add(record);
      }

      return (importRecordList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private <F> Map<String, F> getFileNames(AbstractBaseFilesystemAction<F> actionBase, QTableMetaData table, QBackendMetaData backend) throws QException
   {
      List<F>        files = actionBase.listFiles(table, backend);
      Map<String, F> rs    = new LinkedHashMap<>();

      for(F file : files)
      {
         String fileName = actionBase.stripBackendAndTableBasePathsFromFileName(actionBase.getFullPathForFile(file), backend, table);
         rs.put(fileName, file);
      }

      return (rs);
   }

}