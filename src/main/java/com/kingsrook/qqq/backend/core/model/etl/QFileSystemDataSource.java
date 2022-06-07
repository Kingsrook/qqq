/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.model.etl;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.adapters.CsvToQRecordAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.oro.io.GlobFilenameFilter;


/*******************************************************************************
 **
 *******************************************************************************/
public class QFileSystemDataSource implements QDataSource
{
   private static final Logger LOG = LogManager.getLogger(QFileSystemDataSource.class);

   private String path;
   private String glob;



   /*******************************************************************************
    ** Getter for path
    **
    *******************************************************************************/
   public String getPath()
   {
      return path;
   }



   /*******************************************************************************
    ** Setter for path
    **
    *******************************************************************************/
   public void setPath(String path)
   {
      this.path = path;
   }



   /*******************************************************************************
    ** Fluent setter for path
    **
    *******************************************************************************/
   public QFileSystemDataSource withPath(String path)
   {
      this.path = path;
      return this;
   }



   /*******************************************************************************
    ** Getter for glob
    **
    *******************************************************************************/
   public String getGlob()
   {
      return glob;
   }



   /*******************************************************************************
    ** Setter for glob
    **
    *******************************************************************************/
   public void setGlob(String glob)
   {
      this.glob = glob;
   }



   /*******************************************************************************
    ** Fluent setter for glob
    **
    *******************************************************************************/
   public QFileSystemDataSource withGlob(String glob)
   {
      this.glob = glob;
      return this;
   }



   @Override
   public List<String> listAvailableBatches()
   {
      List<String> rs = new ArrayList<>();
      File directory = new File(path);
      System.out.println("Listing available batches at [" + path + "].");
      for(String entry : Objects.requireNonNull(directory.list(new GlobFilenameFilter(glob))))
      {
         String entryPath = directory + File.separator + entry;
         if(new File(entryPath).isFile())
         {
            rs.add(entryPath);
         }
      }
      System.out.println("Found [" + rs.size() + "] batches.");

      return (rs);
   }



   @Override
   public QDataBatch getBatch(String identity, QTableMetaData table) throws QException
   {
      File file = new File(identity);
      if(!file.exists())
      {
         throw new QException("File [" + identity + "] was not found.");
      }
      if(!file.isFile())
      {
         throw new QException("File [" + identity + "] is not a regular file.");
      }

      try
      {
         System.out.println("Reading batch file [" + identity + "].");
         String contents = FileUtils.readFileToString(file);
         List<QRecord> qRecords = new CsvToQRecordAdapter().buildRecordsFromCsv(contents, table, null);// todo!!
         System.out.println("Read [" + qRecords.size() + "] records from batch file.");

         return (new QDataBatch().withIdentity(identity).withRecords(qRecords));
      }
      catch(IOException e)
      {
         throw new QException("Error reading file", e);
      }
   }



   @Override
   public void discardBatch(QDataBatch batch)
   {
      File file = new File(batch.getIdentity());
      File trashFile = new File("/tmp/" + UUID.randomUUID());
      if(file.renameTo(trashFile))
      {
         System.out.println("Discard batch file [" + batch.getIdentity() + "] to trash file [" + trashFile + "].");
      }
   }
}
