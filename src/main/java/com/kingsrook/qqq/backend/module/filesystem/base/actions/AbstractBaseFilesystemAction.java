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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.adapters.JsonToQRecordAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Base class for all Filesystem actions across all modules.
 **
 ** @param FILE The class that represents a file in the sub-module.  e.g.,
 *               a java.io.File, or an S3Object.
 *******************************************************************************/
public abstract class AbstractBaseFilesystemAction<FILE>
{
   private static final Logger LOG = LogManager.getLogger(AbstractBaseFilesystemAction.class);



   /*******************************************************************************
    ** List the files for a table - to be implemented in module-specific subclasses.
    *******************************************************************************/
   public abstract List<FILE> listFiles(QTableMetaData table, QBackendMetaData backendBase);

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
   public QueryOutput executeQuery(QueryInput queryInput) throws QException
   {
      preAction(queryInput.getBackend());

      try
      {
         QueryOutput queryOutput = new QueryOutput(queryInput);

         QTableMetaData                        table        = queryInput.getTable();
         AbstractFilesystemTableBackendDetails tableDetails = getTableBackendDetails(AbstractFilesystemTableBackendDetails.class, table);
         List<FILE>                            files        = listFiles(table, queryInput.getBackend());

         for(FILE file : files)
         {
            LOG.info("Processing file: " + getFullPathForFile(file));
            switch(tableDetails.getRecordFormat())
            {
               case CSV:
               {
                  String fileContents = IOUtils.toString(readFile(file));
                  fileContents = customizeFileContentsAfterReading(table, fileContents);

                  List<QRecord> recordsInFile = new CsvToQRecordAdapter().buildRecordsFromCsv(fileContents, table, null);
                  addBackendDetailsToRecords(recordsInFile, file);

                  queryOutput.addRecords(recordsInFile);
                  break;
               }
               case JSON:
               {
                  String fileContents = IOUtils.toString(readFile(file));
                  fileContents = customizeFileContentsAfterReading(table, fileContents);

                  List<QRecord> recordsInFile = new JsonToQRecordAdapter().buildRecordsFromJson(fileContents, table, null);
                  addBackendDetailsToRecords(recordsInFile, file);

                  queryOutput.addRecords(recordsInFile);
                  break;
               }
               default:
               {
                  throw new NotImplementedException("Filesystem record format " + tableDetails.getRecordFormat() + " is not yet implemented");
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
    ** Add backend details to records about the file that they are in.
    *******************************************************************************/
   protected void addBackendDetailsToRecords(List<QRecord> recordsInFile, FILE file)
   {
      recordsInFile.forEach(record ->
      {
         record.withBackendDetail(FilesystemRecordBackendDetailFields.FULL_PATH, getFullPathForFile(file));
      });
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
      Optional<QCodeReference> optionalCustomizer = table.getCustomizer(FilesystemBackendModuleInterface.CUSTOMIZER_FILE_POST_FILE_READ);
      if(optionalCustomizer.isEmpty())
      {
         return (fileContents);
      }
      QCodeReference customizer = optionalCustomizer.get();

      try
      {
         Class<?> customizerClass = Class.forName(customizer.getName());

         @SuppressWarnings("unchecked")
         Function<String, String> function = (Function<String, String>) customizerClass.getConstructor().newInstance();

         return function.apply(fileContents);
      }
      catch(Exception e)
      {
         throw (new QException("Error customizing file contents", e));
      }
   }

}
