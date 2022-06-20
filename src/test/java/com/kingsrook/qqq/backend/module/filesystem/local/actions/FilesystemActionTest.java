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
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import org.apache.commons.io.FileUtils;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Base class for Filesystem action tests.
 **
 ** Knows how to set up the filesystem for the tests.
 *******************************************************************************/
public class FilesystemActionTest
{

   /*******************************************************************************
    ** Set up the file system
    *******************************************************************************/
   protected void primeFilesystem() throws IOException
   {
      TestUtils.cleanInstanceFiles();
      TestUtils.increaseTestInstanceCounter();
      FilesystemBackendMetaData filesystemBackendMetaData = TestUtils.defineLocalFilesystemBackend();

      File    baseDirectory = new File(filesystemBackendMetaData.getBasePath());
      boolean mkdirsResult  = baseDirectory.mkdirs();
      if(!mkdirsResult)
      {
         fail("Failed to make directories at [" + baseDirectory + "] for filesystem backend module");
      }

      writePersonFiles(baseDirectory);
   }



   private void writePersonFiles(File baseDirectory) throws IOException
   {
      String fullPath = baseDirectory.getAbsolutePath();
      if (TestUtils.defineLocalFilesystemCSVPersonTable().getBackendDetails() instanceof FilesystemTableBackendDetails details)
      {
         if (StringUtils.hasContent(details.getPath()))
         {
            fullPath += File.separatorChar + details.getPath();
         }
      }
      fullPath += File.separatorChar;

      String csvHeader = """
         "id","createDate","modifyDate","firstName","lastName","birthDate","email"
         """;

      String csvData1 = csvHeader + """
         "1","2021-10-26 14:39:37","2021-10-26 14:39:37","John","Doe","1981-01-01","john@kingsrook.com"
         "2","2022-06-17 14:52:59","2022-06-17 14:52:59","Jane","Smith","1982-02-02","jane@kingsrook.com"
         """;
      FileUtils.writeStringToFile(new File(fullPath + "DATA-1.csv"), csvData1);

      String csvData2 = csvHeader + """
         "3","2021-11-27 15:40:38","2021-11-27 15:40:38","Homer","S","1983-03-03","homer.s@kingsrook.com"
         "4","2022-07-18 15:53:00","2022-07-18 15:53:00","Marge","S","1984-04-04","marge.s@kingsrook.com"
         "5","2022-11-11 12:00:00","2022-11-12 13:00:00","Bart","S","1985-05-05","bart.s@kingsrook.com"
         """;
      FileUtils.writeStringToFile(new File(fullPath + "DATA-2.csv"), csvData2);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void cleanFilesystem() throws IOException
   {
      TestUtils.cleanInstanceFiles();
   }
}
