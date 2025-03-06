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


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 **
 *******************************************************************************/
public class FilesystemDeleteActionTest extends FilesystemActionTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testSuccessfulDeleteMultiple() throws QException
   {
      int initialCount = new CountAction().execute(new CountInput(TestUtils.TABLE_NAME_BLOB_LOCAL_FS)).getCount();

      String filename1 = "A.txt";
      String filename2 = "B.txt";
      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_BLOB_LOCAL_FS).withRecords(List.of(
         new QRecord().withValue("fileName", filename1).withValue("contents", "bytes"),
         new QRecord().withValue("fileName", filename2).withValue("contents", "bytes"))));
      assertEquals(initialCount + 2, new CountAction().execute(new CountInput(TestUtils.TABLE_NAME_BLOB_LOCAL_FS)).getCount());

      DeleteOutput deleteOutput = new DeleteAction().execute(new DeleteInput(TestUtils.TABLE_NAME_BLOB_LOCAL_FS).withPrimaryKeys(List.of(filename1, filename2)));
      assertEquals(2, deleteOutput.getDeletedRecordCount());
      assertEquals(0, deleteOutput.getRecordsWithErrors().size());
      assertEquals(initialCount, new CountAction().execute(new CountInput(TestUtils.TABLE_NAME_BLOB_LOCAL_FS)).getCount());
   }

}