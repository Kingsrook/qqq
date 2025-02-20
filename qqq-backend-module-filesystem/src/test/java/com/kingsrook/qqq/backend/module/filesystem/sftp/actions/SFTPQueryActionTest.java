/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.filesystem.sftp.actions;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.sftp.BaseSFTPTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Unit test for SFTPQueryAction 
 *******************************************************************************/
class SFTPQueryActionTest extends BaseSFTPTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testSimpleQuery() throws QException
   {
      QueryInput  queryInput  = new QueryInput(TestUtils.TABLE_NAME_SFTP_FILE);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      Assertions.assertEquals(5, queryOutput.getRecords().size(), "Expected # of rows from unfiltered query");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueryWithPath() throws Exception
   {
      String subfolderPath = "/home/" + USERNAME + "/" + BACKEND_FOLDER + "/" + TABLE_FOLDER + "/subfolder/";
      try
      {
         copyFileToContainer("files/testfile.txt", subfolderPath + "/sub1.txt");
         copyFileToContainer("files/testfile.txt", subfolderPath + "/sub2.txt");

         QueryInput queryInput = new QueryInput(TestUtils.TABLE_NAME_SFTP_FILE)
            .withFilter(new QQueryFilter(new QFilterCriteria("path", QCriteriaOperator.EQUALS, "subfolder")));
         QueryOutput queryOutput = new QueryAction().execute(queryInput);
         Assertions.assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows from subfolder path query");
      }
      finally
      {
         rmrfInContainer(subfolderPath);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueryWithPathAndNameLike() throws Exception
   {
      String subfolderPath = "/home/" + USERNAME + "/" + BACKEND_FOLDER + "/" + TABLE_FOLDER + "/subfolder/";
      try
      {
         copyFileToContainer("files/testfile.txt", subfolderPath + "/sub1.txt");
         copyFileToContainer("files/testfile.txt", subfolderPath + "/sub2.txt");
         copyFileToContainer("files/testfile.txt", subfolderPath + "/who.txt");

         Map<String, Integer> patternExpectedCountMap = Map.of(
            "%.txt", 3,
            "sub%", 2,
            "%1%", 1,
            "%", 3,
            "*", 0
         );

         for(Map.Entry<String, Integer> entry : patternExpectedCountMap.entrySet())
         {
            QueryInput queryInput = new QueryInput(TestUtils.TABLE_NAME_SFTP_FILE).withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("path", QCriteriaOperator.EQUALS, "subfolder"))
               .withCriteria(new QFilterCriteria("baseName", QCriteriaOperator.LIKE, entry.getKey())));
            QueryOutput queryOutput = new QueryAction().execute(queryInput);
            Assertions.assertEquals(entry.getValue(), queryOutput.getRecords().size(), "Expected # of rows from subfolder path, baseName like: " + entry.getKey());
         }
      }
      finally
      {
         rmrfInContainer(subfolderPath);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testQueryVariantsTable() throws Exception
   {
      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_VARIANT_OPTIONS).withRecords(List.of(
         new QRecord().withValue("id", 1).withValue("basePath", BaseSFTPTest.BACKEND_FOLDER),
         new QRecord().withValue("id", 2).withValue("basePath", "empty-folder"),
         new QRecord().withValue("id", 3).withValue("basePath", "non-existing-path")
      )));

      mkdirInSftpContainerUnderHomeTestuser("empty-folder/files");

      QueryInput queryInput = new QueryInput(TestUtils.TABLE_NAME_SFTP_FILE_VARIANTS);
      assertThatThrownBy(() -> new QueryAction().execute(queryInput))
         .hasMessageContaining("Could not find Backend Variant information for Backend");

      QContext.getQSession().setBackendVariants(MapBuilder.of(TestUtils.TABLE_NAME_VARIANT_OPTIONS, 1));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      Assertions.assertEquals(5, queryOutput.getRecords().size(), "Expected # of rows from unfiltered query");

      QContext.getQSession().setBackendVariants(MapBuilder.of(TestUtils.TABLE_NAME_VARIANT_OPTIONS, 2));
      queryOutput = new QueryAction().execute(queryInput);
      Assertions.assertEquals(0, queryOutput.getRecords().size(), "Expected # of rows from unfiltered query");

      QContext.getQSession().setBackendVariants(MapBuilder.of(TestUtils.TABLE_NAME_VARIANT_OPTIONS, 3));
      assertThatThrownBy(() -> new QueryAction().execute(queryInput))
         .rootCause()
         .hasMessageContaining("No such file");

      // Assertions.assertEquals(5, queryOutput.getRecords().size(), "Expected # of rows from unfiltered query");
   }

}