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


import java.util.function.Function;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 **
 *******************************************************************************/
public class FilesystemQueryActionTest extends FilesystemActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testQuery1() throws QException
   {
      QueryRequest queryRequest = new QueryRequest();
      queryRequest.setInstance(TestUtils.defineInstance());
      queryRequest.setTableName(TestUtils.defineLocalFilesystemJSONPersonTable().getName());
      QueryResult queryResult = new FilesystemQueryAction().execute(queryRequest);
      Assertions.assertEquals(3, queryResult.getRecords().size(), "Unfiltered query should find all rows");
      Assertions.assertTrue(queryResult.getRecords().stream()
            .allMatch(record -> record.getBackendDetailString(FilesystemRecordBackendDetailFields.FULL_PATH).contains(TestUtils.BASE_PATH)),
         "All records should have a full-path in their backend details, matching the test folder name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testQueryWithFileCustomizer() throws QException
   {
      QueryRequest queryRequest = new QueryRequest();
      QInstance    instance     = TestUtils.defineInstance();

      QTableMetaData table = instance.getTable(TestUtils.TABLE_NAME_PERSON_LOCAL_FS);
      table.withCustomizer(FilesystemBackendModuleInterface.CUSTOMIZER_FILE_POST_FILE_READ, new QCodeReference()
         .withName(ValueUpshifter.class.getName())
         .withCodeType(QCodeType.JAVA)
         .withCodeUsage(QCodeUsage.CUSTOMIZER));

      queryRequest.setInstance(instance);
      queryRequest.setTableName(TestUtils.defineLocalFilesystemJSONPersonTable().getName());
      QueryResult queryResult = new FilesystemQueryAction().execute(queryRequest);
      Assertions.assertEquals(3, queryResult.getRecords().size(), "Unfiltered query should find all rows");
      Assertions.assertTrue(
         queryResult.getRecords().stream().allMatch(record -> record.getValueString("email").matches(".*KINGSROOK.COM")),
         "All records should have their email addresses up-shifted.");
   }



   public static class ValueUpshifter implements Function<String, String>
   {
      @Override
      public String apply(String s)
      {
         return (s.replaceAll("kingsrook.com", "KINGSROOK.COM"));
      }
   }

}