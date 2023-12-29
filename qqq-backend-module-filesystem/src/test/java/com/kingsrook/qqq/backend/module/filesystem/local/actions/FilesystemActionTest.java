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

package com.kingsrook.qqq.backend.module.filesystem.local.actions;


import java.io.File;
import java.io.IOException;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.BaseTest;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Base class for Filesystem action tests.
 **
 ** Knows how to set up the filesystem for the tests.
 *******************************************************************************/
public class FilesystemActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      primeFilesystem();
      QContext.init(TestUtils.defineInstance(), new QSession());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   public void afterEach() throws Exception
   {
      cleanFilesystem();
   }



   /*******************************************************************************
    ** Set up the file system
    *******************************************************************************/
   public void primeFilesystem() throws IOException
   {
      TestUtils.cleanInstanceFiles();
      TestUtils.increaseTestInstanceCounter();
      FilesystemBackendMetaData filesystemBackendMetaData = TestUtils.defineLocalFilesystemBackend();

      File baseDirectory = new File(filesystemBackendMetaData.getBasePath());
      if(!baseDirectory.exists())
      {
         boolean mkdirsResult = baseDirectory.mkdirs();
         if(!mkdirsResult)
         {
            fail("Failed to make directories at [" + baseDirectory + "] for filesystem backend module");
         }
      }

      writePersonJSONFiles(baseDirectory);
      writePersonCSVFiles(baseDirectory);
      writeBlobFiles(baseDirectory);
   }



   /*******************************************************************************
    ** Write some data files into the directory for the filesystem module.
    *******************************************************************************/
   private void writePersonJSONFiles(File baseDirectory) throws IOException
   {
      String fullPath = baseDirectory.getAbsolutePath();
      if(TestUtils.defineLocalFilesystemJSONPersonTable().getBackendDetails() instanceof FilesystemTableBackendDetails details)
      {
         if(StringUtils.hasContent(details.getBasePath()))
         {
            fullPath += File.separatorChar + details.getBasePath();
         }
      }
      fullPath += File.separatorChar;

      String jsonData1 = """
         [
            {"id":1,"createDate":"2021-10-26 14:39:37","modifyDate":"2021-10-26 14:39:37","firstName":"John","lastName":"Doe","birthDate":"1981-01-01","email":"john@kingsrook.com"},
            {"id":2,"createDate":"2022-06-17 14:52:59","modifyDate":"2022-06-17 14:52:59","firstName":"Jane","lastName":"Smith","birthDate":"1982-02-02","email":"jane@kingsrook.com"}
         ]
         """;
      FileUtils.writeStringToFile(new File(fullPath + "DATA-1.json"), jsonData1);

      String jsonData2 = """
         [
            {"id":3,"createDate":"2021-11-27 15:40:38","modifyDate":"2021-11-27 15:40:38","firstName":"Homer","lastName":"S","birthDate":"1983-03-03","email":"homer.s@kingsrook.com"}
         ]
         """;
      FileUtils.writeStringToFile(new File(fullPath + "DATA-2.json"), jsonData2);
   }



   /*******************************************************************************
    ** Write some data files into the directory for the filesystem module.
    *******************************************************************************/
   private void writePersonCSVFiles(File baseDirectory) throws IOException
   {
      String fullPath = baseDirectory.getAbsolutePath();
      if(TestUtils.defineLocalFilesystemCSVPersonTable().getBackendDetails() instanceof FilesystemTableBackendDetails details)
      {
         if(StringUtils.hasContent(details.getBasePath()))
         {
            fullPath += File.separatorChar + details.getBasePath();
         }
      }
      fullPath += File.separatorChar;

      String csvData1 = """
         "id","createDate","modifyDate","firstName","lastName","birthDate","email"
         "1","2021-10-26 14:39:37","2021-10-26 14:39:37","John","Doe","1981-01-01","john@kingsrook.com"
         "2","2022-06-17 14:52:59","2022-06-17 14:52:59","Jane","Smith","1982-02-02","jane@kingsrook.com"
         """;
      FileUtils.writeStringToFile(new File(fullPath + "FILE-1.csv"), csvData1);

      String csvData2 = """
         "id","createDate","modifyDate","firstName","lastName","birthDate","email"
         "3","2021-11-27 15:40:38","2021-11-27 15:40:38","Homer","S","1983-03-03","homer.s@kingsrook.com"
         "4","2022-07-18 15:53:00","2022-07-18 15:53:00","Marge","S","1984-04-04","marge.s@kingsrook.com"
         "5","2022-11-11 12:00:00","2022-11-12 13:00:00","Bart","S","1985-05-05","bart.s@kingsrook.com\""""; // intentionally no \n at EOL here
      FileUtils.writeStringToFile(new File(fullPath + "FILE-2.csv"), csvData2);
   }



   /*******************************************************************************
    ** Write some data files into the directory for the filesystem module.
    *******************************************************************************/
   private void writeBlobFiles(File baseDirectory) throws IOException
   {
      String fullPath = baseDirectory.getAbsolutePath();
      if(TestUtils.defineLocalFilesystemBlobTable().getBackendDetails() instanceof FilesystemTableBackendDetails details)
      {
         if(StringUtils.hasContent(details.getBasePath()))
         {
            fullPath += File.separatorChar + details.getBasePath();
         }
      }
      fullPath += File.separatorChar;

      String data1 = """
         Hello, Blob
         """;
      FileUtils.writeStringToFile(new File(fullPath + "BLOB-1.txt"), data1);

      String data2 = """
         Hi Bob""";
      FileUtils.writeStringToFile(new File(fullPath + "BLOB-2.txt"), data2);

      String data3 = """
         # Hi MD...""";
      FileUtils.writeStringToFile(new File(fullPath + "BLOB-3.md"), data3);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void cleanFilesystem() throws IOException
   {
      TestUtils.cleanInstanceFiles();
   }
}
