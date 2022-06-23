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
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.adapters.JsonToQRecordAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemTableBackendDetails;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 ** Base class for all Filesystem actions across all modules.
 *******************************************************************************/
public abstract class AbstractBaseFilesystemAction<FILE>
{

   /*******************************************************************************
    ** List the files for a table - to be implemented in module-specific subclasses.
    *******************************************************************************/
   public abstract List<FILE> listFiles(QTableMetaData table, QBackendMetaData backendBase);

   /*******************************************************************************
    ** Read the contents of a file - to be implemented in module-specific subclasses.
    *******************************************************************************/
   public abstract InputStream readFile(FILE file) throws IOException;

   /*******************************************************************************
    ** Add backend details to records about the file that they are in.
    *******************************************************************************/
   protected abstract void addBackendDetailsToRecords(List<QRecord> recordsInFile, FILE file);


   /*******************************************************************************
    ** Append together the backend's base path (if present), with a table's path (again, if present).
    *******************************************************************************/
   protected String getFullPath(QTableMetaData table, QBackendMetaData backendBase)
   {
      AbstractFilesystemBackendMetaData metaData = getBackendMetaData(AbstractFilesystemBackendMetaData.class, backendBase);
      String                            fullPath = StringUtils.hasContent(metaData.getBasePath()) ? metaData.getBasePath() : "";

      AbstractFilesystemTableBackendDetails tableDetails = getTableBackendDetails(AbstractFilesystemTableBackendDetails.class, table);
      if(StringUtils.hasContent(tableDetails.getPath()))
      {
         fullPath += File.separatorChar + tableDetails.getPath();
      }

      fullPath += File.separatorChar;
      return fullPath;
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
   public QueryResult executeQuery(QueryRequest queryRequest) throws QException
   {
      try
      {
         QueryResult   rs      = new QueryResult();
         List<QRecord> records = new ArrayList<>();
         rs.setRecords(records);

         QTableMetaData                        table        = queryRequest.getTable();
         AbstractFilesystemTableBackendDetails tableDetails = getTableBackendDetails(AbstractFilesystemTableBackendDetails.class, table);
         List<FILE>                            files        = listFiles(table, queryRequest.getBackend());

         for(FILE file : files)
         {
            switch(tableDetails.getRecordFormat())
            {
               case "csv":
               {
                  String        fileContents  = IOUtils.toString(readFile(file));
                  List<QRecord> recordsInFile = new CsvToQRecordAdapter().buildRecordsFromCsv(fileContents, table, null);
                  addBackendDetailsToRecords(recordsInFile, file);

                  records.addAll(recordsInFile);
                  break;
               }
               case "json":
               {
                  String        fileContents  = IOUtils.toString(readFile(file));
                  List<QRecord> recordsInFile = new JsonToQRecordAdapter().buildRecordsFromJson(fileContents, table, null);
                  addBackendDetailsToRecords(recordsInFile, file);

                  records.addAll(recordsInFile);
                  break;
               }
               default:
               {
                  throw new NotImplementedException("Filesystem record format " + tableDetails.getRecordFormat() + " is not yet implemented");
               }
            }
         }

         return rs;
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new QException("Error executing query", e);
      }
   }
}
