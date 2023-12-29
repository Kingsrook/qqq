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

package com.kingsrook.qqq.backend.module.filesystem.base.actions;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.adapters.JsonToQRecordAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.Cardinality;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Base class for all Filesystem actions across all modules.
 **
 ** @param FILE The class that represents a file in the sub-module.  e.g.,
 *               a java.io.File, or an S3Object.
 *******************************************************************************/
public abstract class AbstractBaseFilesystemAction<FILE>
{
   private static final QLogger LOG = QLogger.getLogger(AbstractBaseFilesystemAction.class);



   /*******************************************************************************
    ** List the files for a table - to be implemented in module-specific subclasses.
    *******************************************************************************/
   public List<FILE> listFiles(QTableMetaData table, QBackendMetaData backendBase) throws QException
   {
      return (listFiles(table, backendBase, null));
   }



   /*******************************************************************************
    ** List the files for a table - WITH an input filter - to be implemented in module-specific subclasses.
    *******************************************************************************/
   public abstract List<FILE> listFiles(QTableMetaData table, QBackendMetaData backendBase, QQueryFilter filter) throws QException;

   /*******************************************************************************
    ** Read the contents of a file - to be implemented in module-specific subclasses.
    *******************************************************************************/
   public abstract InputStream readFile(FILE file) throws IOException;

   /*******************************************************************************
    ** Write a file - to be implemented in module-specific subclasses.
    *******************************************************************************/
   public abstract void writeFile(QBackendMetaData backend, String path, byte[] contents) throws IOException;

   /*******************************************************************************
    ** Get a string that represents the full path to a file.
    *******************************************************************************/
   public abstract String getFullPathForFile(FILE file);

   /*******************************************************************************
    ** In contrast with the DeleteAction, which deletes RECORDS - this is a
    ** filesystem-(or s3, sftp, etc)-specific extension to delete an entire FILE
    ** e.g., for post-ETL.
    **
    ** @throws FilesystemException if the delete is known to have failed, and the file is thought to still exit
    *******************************************************************************/
   public abstract void deleteFile(QInstance instance, QTableMetaData table, String fileReference) throws FilesystemException;

   /*******************************************************************************
    ** Move a file from a source path, to a destination path.
    **
    ** @throws FilesystemException if the move is known to have failed
    *******************************************************************************/
   public abstract void moveFile(QInstance instance, QTableMetaData table, String source, String destination) throws FilesystemException;

   /*******************************************************************************
    ** e.g., with a base path of /foo/
    ** and a table path of /bar/
    ** and a file at /foo/bar/baz.txt
    ** give us just the baz.txt part.
    *******************************************************************************/
   public abstract String stripBackendAndTableBasePathsFromFileName(String filePath, QBackendMetaData sourceBackend, QTableMetaData sourceTable);



   /*******************************************************************************
    ** Append together the backend's base path (if present), with a table's base
    ** path (again, if present).
    *******************************************************************************/
   public String getFullBasePath(QTableMetaData table, QBackendMetaData backendBase)
   {
      AbstractFilesystemBackendMetaData metaData = getBackendMetaData(AbstractFilesystemBackendMetaData.class, backendBase);
      String                            fullPath = StringUtils.hasContent(metaData.getBasePath()) ? metaData.getBasePath() : "";

      AbstractFilesystemTableBackendDetails tableDetails = getTableBackendDetails(AbstractFilesystemTableBackendDetails.class, table);
      if(StringUtils.hasContent(tableDetails.getBasePath()))
      {
         fullPath += File.separatorChar + tableDetails.getBasePath();
      }

      fullPath += File.separatorChar;
      fullPath = stripDuplicatedSlashes(fullPath);

      return fullPath;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String stripDuplicatedSlashes(String path)
   {
      if(path == null)
      {
         return (null);
      }

      return (path.replaceAll("//+", "/"));
   }



   /*******************************************************************************
    ** Get the backend metaData, type-checked as the requested type.
    *******************************************************************************/
   protected <T extends AbstractFilesystemBackendMetaData> T getBackendMetaData(Class<T> outputClass, QBackendMetaData metaData)
   {
      if(!(outputClass.isInstance(metaData)))
      {
         throw new IllegalArgumentException("MetaData was not of expected type (was " + metaData.getClass().getSimpleName() + ")");
      }
      return outputClass.cast(metaData);
   }



   /*******************************************************************************
    ** Get the backendDetails out of a table, type-checked as the requested type
    *******************************************************************************/
   protected <T extends AbstractFilesystemTableBackendDetails> T getTableBackendDetails(Class<T> outputClass, QTableMetaData tableMetaData)
   {
      QTableBackendDetails tableBackendDetails = tableMetaData.getBackendDetails();
      if(!(outputClass.isInstance(tableBackendDetails)))
      {
         throw new IllegalArgumentException("Table backend details was not of expected type (was " + tableBackendDetails.getClass().getSimpleName() + ")");
      }
      return outputClass.cast(tableBackendDetails);
   }



   /*******************************************************************************
    ** Generic implementation of the execute method from the QueryInterface
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   public QueryOutput executeQuery(QueryInput queryInput) throws QException
   {
      preAction(queryInput.getBackend());

      try
      {
         QueryOutput queryOutput = new QueryOutput(queryInput);

         QTableMetaData                        table        = queryInput.getTable();
         AbstractFilesystemTableBackendDetails tableDetails = getTableBackendDetails(AbstractFilesystemTableBackendDetails.class, table);
         List<FILE>                            files        = listFiles(table, queryInput.getBackend(), queryInput.getFilter());

         int recordCount = 0;

         FILE_LOOP:
         for(FILE file : files)
         {
            InputStream inputStream = readFile(file);
            switch(tableDetails.getCardinality())
            {
               case MANY:
               {
                  LOG.info("Extracting records from file", logPair("table", table.getName()), logPair("path", getFullPathForFile(file)));
                  switch(tableDetails.getRecordFormat())
                  {
                     case CSV:
                     {
                        String fileContents = IOUtils.toString(inputStream);
                        fileContents = customizeFileContentsAfterReading(table, fileContents);

                        if(queryInput.getRecordPipe() != null)
                        {
                           new CsvToQRecordAdapter().buildRecordsFromCsv(queryInput.getRecordPipe(), fileContents, table, null, (record ->
                           {
                              ////////////////////////////////////////////////////////////////////////////////////////////
                              // Before the records go into the pipe, make sure their backend details are added to them //
                              ////////////////////////////////////////////////////////////////////////////////////////////
                              addBackendDetailsToRecord(record, file);
                           }));
                        }
                        else
                        {
                           List<QRecord> recordsInFile = new CsvToQRecordAdapter().buildRecordsFromCsv(fileContents, table, null);
                           addBackendDetailsToRecords(recordsInFile, file);
                           queryOutput.addRecords(recordsInFile);
                        }
                        break;
                     }
                     case JSON:
                     {
                        String fileContents = IOUtils.toString(inputStream);
                        fileContents = customizeFileContentsAfterReading(table, fileContents);

                        // todo - pipe support!!
                        List<QRecord> recordsInFile = new JsonToQRecordAdapter().buildRecordsFromJson(fileContents, table, null);
                        addBackendDetailsToRecords(recordsInFile, file);

                        queryOutput.addRecords(recordsInFile);
                        break;
                     }
                     default:
                     {
                        throw new IllegalStateException("Unexpected table record format: " + tableDetails.getRecordFormat());
                     }
                  }
                  break;
               }
               case ONE:
               {
                  ////////////////////////////////////////////////////////////////////////////////
                  // for one-record tables, put the entire file's contents into a single record //
                  ////////////////////////////////////////////////////////////////////////////////
                  String filePathWithoutBase = stripBackendAndTableBasePathsFromFileName(getFullPathForFile(file), queryInput.getBackend(), table);
                  byte[] bytes               = inputStream.readAllBytes();

                  QRecord record = new QRecord()
                     .withValue(tableDetails.getFileNameFieldName(), filePathWithoutBase)
                     .withValue(tableDetails.getContentsFieldName(), bytes);
                  queryOutput.addRecord(record);

                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // keep our own count - in case the query output is using a pipe (e.g., so we can't just call a .size()) //
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                  recordCount++;

                  ////////////////////////////////////////////////////////////////////////////
                  // break out of the file loop if we have hit the limit (if one was given) //
                  ////////////////////////////////////////////////////////////////////////////
                  if(queryInput.getFilter() != null && queryInput.getFilter().getLimit() != null)
                  {
                     if(recordCount >= queryInput.getFilter().getLimit())
                     {
                        break FILE_LOOP;
                     }
                  }

                  break;
               }
               default:
               {
                  throw new IllegalStateException("Unexpected table cardinality: " + tableDetails.getCardinality());
               }
            }
         }

         return queryOutput;
      }
      catch(Exception e)
      {
         LOG.warn("Error executing query", e);
         throw new QException("Error executing query", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public CountOutput executeCount(CountInput countInput) throws QException
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(countInput.getTableName());
      queryInput.setFilter(countInput.getFilter());
      QueryOutput queryOutput = executeQuery(queryInput);

      CountOutput countOutput = new CountOutput();
      countOutput.setCount(queryOutput.getRecords().size());
      return (countOutput);
   }



   /*******************************************************************************
    ** Add backend details to records about the file that they are in.
    *******************************************************************************/
   protected void addBackendDetailsToRecords(List<QRecord> recordsInFile, FILE file)
   {
      recordsInFile.forEach(r -> addBackendDetailsToRecord(r, file));
   }



   /*******************************************************************************
    ** Add backend details to a record about the file that it is in.
    *******************************************************************************/
   protected void addBackendDetailsToRecord(QRecord record, FILE file)
   {
      record.addBackendDetail(FilesystemRecordBackendDetailFields.FULL_PATH, getFullPathForFile(file));
   }



   /*******************************************************************************
    ** Method that subclasses can override to add pre-action things (e.g., setting up
    ** s3 client).
    *******************************************************************************/
   public void preAction(QBackendMetaData backendMetaData)
   {
      /////////////////////////////////////////////////////////////////////
      // noop in base class - subclasses can add functionality if needed //
      /////////////////////////////////////////////////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String customizeFileContentsAfterReading(QTableMetaData table, String fileContents) throws QException
   {
      try
      {
         Optional<AbstractPostReadFileCustomizer> tableCustomizer = QCodeLoader.getTableCustomizer(AbstractPostReadFileCustomizer.class, table, FilesystemTableCustomizers.POST_READ_FILE.getRole());
         if(tableCustomizer.isEmpty())
         {
            return (fileContents);
         }

         return tableCustomizer.get().customizeFileContents(fileContents);
      }
      catch(Exception e)
      {
         throw (new QException("Error customizing file contents", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected InsertOutput executeInsert(InsertInput insertInput) throws QException
   {
      try
      {
         preAction(insertInput.getBackend());

         InsertOutput     output  = new InsertOutput();
         QTableMetaData   table   = insertInput.getTable();
         QBackendMetaData backend = insertInput.getBackend();

         output.setRecords(new ArrayList<>());

         AbstractFilesystemTableBackendDetails tableDetails = getTableBackendDetails(AbstractFilesystemTableBackendDetails.class, table);
         if(tableDetails.getCardinality().equals(Cardinality.ONE))
         {
            for(QRecord record : insertInput.getRecords())
            {
               String fullPath = stripDuplicatedSlashes(getFullBasePath(table, backend) + File.separator + record.getValueString(tableDetails.getFileNameFieldName()));
               writeFile(backend, fullPath, record.getValueByteArray(tableDetails.getContentsFieldName()));
               record.addBackendDetail(FilesystemRecordBackendDetailFields.FULL_PATH, fullPath);
               output.addRecord(record);
            }
         }
         else
         {
            throw (new NotImplementedException("Insert is not implemented for filesystem tables with cardinality: " + tableDetails.getCardinality()));
         }

         return (output);
      }
      catch(Exception e)
      {
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }
   }
}
