/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.instances.loaders;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for MetaDataLoaderHelper 
 *******************************************************************************/
class MetaDataLoaderHelperTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws Exception
   {
      Path tempDirectory = Files.createTempDirectory(getClass().getSimpleName());

      writeFile("myTable", ".yaml", tempDirectory, """
         class: QTableMetaData
         version:  1
         name: myTable
         label: This is My Table
         primaryKeyField:  id
         fields:
         -  name: id
            type: INTEGER
         -  name: name
            type: STRING
         -  name: createDate
            type: DATE_TIME
         """);

      writeFile("yourTable", ".yaml", tempDirectory, """
         class: QTableMetaData
         version:  1
         name: yourTable
         label: Someone else's table
         primaryKeyField:  id
         fields:
         -  name: id
            type: INTEGER
         -  name: name
            type: STRING
         """);

      QInstance qInstance = new QInstance();
      MetaDataLoaderHelper.processAllMetaDataFilesInDirectory(qInstance, tempDirectory.toFile().getAbsolutePath());

      assertEquals(2, qInstance.getTables().size());

      QTableMetaData myTable = qInstance.getTable("myTable");
      assertEquals("This is My Table", myTable.getLabel());
      assertEquals(3, myTable.getFields().size());
      assertEquals("id", myTable.getField("id").getName());
      assertEquals(QFieldType.INTEGER, myTable.getField("id").getType());

      QTableMetaData yourTable = qInstance.getTable("yourTable");
      assertEquals("Someone else's table", yourTable.getLabel());
      assertEquals(2, yourTable.getFields().size());
   }



   void writeFile(String prefix, String suffix, Path directory, String content) throws IOException
   {
      FileUtils.writeStringToFile(File.createTempFile(prefix, suffix, directory.toFile()), content, StandardCharsets.UTF_8);
   }
}