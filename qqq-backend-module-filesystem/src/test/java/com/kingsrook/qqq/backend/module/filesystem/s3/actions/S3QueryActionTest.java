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

package com.kingsrook.qqq.backend.module.filesystem.s3.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;
import com.kingsrook.qqq.backend.module.filesystem.s3.BaseS3Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 **
 *******************************************************************************/
public class S3QueryActionTest extends BaseS3Test
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testQuery1() throws QException
   {
      QueryInput    queryInput    = initQueryRequest();
      S3QueryAction s3QueryAction = new S3QueryAction();
      s3QueryAction.setS3Utils(getS3Utils());
      QueryOutput queryOutput = s3QueryAction.execute(queryInput);
      Assertions.assertEquals(5, queryOutput.getRecords().size(), "Expected # of rows from unfiltered query");
      Assertions.assertTrue(queryOutput.getRecords().stream()
            .allMatch(record -> record.getBackendDetailString(FilesystemRecordBackendDetailFields.FULL_PATH).contains(BaseS3Test.TEST_FOLDER)),
         "All records should have a full-path in their backend details, matching the test folder name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QueryInput initQueryRequest() throws QInstanceValidationException
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setInstance(TestUtils.defineInstance());
      queryInput.setTableName(TestUtils.defineS3CSVPersonTable().getName());
      return queryInput;
   }

}