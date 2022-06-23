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
   public void primeFilesystem() throws IOException
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
    **
    *******************************************************************************/
   public void cleanFilesystem() throws IOException
   {
      TestUtils.cleanInstanceFiles();
   }
}
