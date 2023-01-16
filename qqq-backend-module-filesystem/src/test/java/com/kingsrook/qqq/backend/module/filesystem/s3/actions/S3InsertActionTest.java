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


import java.io.IOException;
import java.util.List;
import com.amazonaws.services.s3.model.S3Object;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;
import com.kingsrook.qqq.backend.module.filesystem.s3.BaseS3Test;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 **
 *******************************************************************************/
public class S3InsertActionTest extends BaseS3Test
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testCardinalityOne() throws QException, IOException
   {
      QInstance qInstance = TestUtils.defineInstance();

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_BLOB_S3);
      insertInput.setRecords(List.of(
         new QRecord().withValue("fileName", "file2.txt").withValue("contents", "Hi, Bob.")
      ));

      S3InsertAction insertAction = new S3InsertAction();
      insertAction.setS3Utils(getS3Utils());

      InsertOutput insertOutput = insertAction.execute(insertInput);
      assertThat(insertOutput.getRecords())
         .allMatch(record -> record.getBackendDetailString(FilesystemRecordBackendDetailFields.FULL_PATH).contains("blobs"));

      String   fullPath = insertOutput.getRecords().get(0).getBackendDetailString(FilesystemRecordBackendDetailFields.FULL_PATH);
      S3Object object   = getAmazonS3().getObject(BUCKET_NAME, fullPath);
      List     lines    = IOUtils.readLines(object.getObjectContent());
      assertEquals("Hi, Bob.", lines.get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testCardinalityMany() throws QException, IOException
   {
      QInstance   qInstance   = TestUtils.defineInstance();
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_S3);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", "1").withValue("firstName", "Bob")
      ));
      assertThatThrownBy(() -> new InsertAction().execute(insertInput))
         .hasRootCauseInstanceOf(NotImplementedException.class);
   }
}