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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.QUploadedFile;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.state.StateType;
import com.kingsrook.qqq.backend.core.state.TempFileStateProvider;
import com.kingsrook.qqq.backend.core.state.UUIDAndTypeStateKey;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for BulkInsertReceiveFileStep
 *******************************************************************************/
class BulkInsertReceiveFileStepTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      ////////////////////////////////////////////////////////////////
      // create an uploaded file, similar to how an http server may //
      ////////////////////////////////////////////////////////////////
      QUploadedFile qUploadedFile = new QUploadedFile();
      qUploadedFile.setBytes((TestUtils.getPersonCsvHeaderUsingLabels() + TestUtils.getPersonCsvRow1() + TestUtils.getPersonCsvRow2()).getBytes());
      qUploadedFile.setFilename("test.csv");
      UUIDAndTypeStateKey key = new UUIDAndTypeStateKey(StateType.UPLOADED_FILE);
      TempFileStateProvider.getInstance().put(key, qUploadedFile);

      ////////////////////////////
      // setup and run the step //
      ////////////////////////////
      RunBackendStepInput stepInput = new RunBackendStepInput(TestUtils.defineInstance());
      stepInput.setSession(TestUtils.getMockSession());
      stepInput.setTableName(TestUtils.defineTablePerson().getName());
      stepInput.addValue(QUploadedFile.DEFAULT_UPLOADED_FILE_FIELD_NAME, key);

      RunBackendStepOutput stepOutput = new RunBackendStepOutput();
      new BulkInsertReceiveFileStep().run(stepInput, stepOutput);

      List<QRecord> records = stepOutput.getRecords();
      assertEquals(2, records.size());
      assertEquals("John", records.get(0).getValueString("firstName"));
      assertEquals("Jane", records.get(1).getValueString("firstName"));
      assertNull(records.get(0).getValue("id"));
      assertNull(records.get(1).getValue("id"));

      assertEquals(2, stepOutput.getValueInteger("noOfFileRows"));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBadFileType() throws QException
   {
      ////////////////////////////////////////////////////////////////
      // create an uploaded file, similar to how an http server may //
      ////////////////////////////////////////////////////////////////
      QUploadedFile qUploadedFile = new QUploadedFile();
      qUploadedFile.setBytes((TestUtils.getPersonCsvHeaderUsingLabels() + TestUtils.getPersonCsvRow1() + TestUtils.getPersonCsvRow2()).getBytes()); // todo - this is NOT excel content...
      qUploadedFile.setFilename("test.xslx");
      UUIDAndTypeStateKey key = new UUIDAndTypeStateKey(StateType.UPLOADED_FILE);
      TempFileStateProvider.getInstance().put(key, qUploadedFile);

      ////////////////////////////
      // setup and run the step //
      ////////////////////////////
      RunBackendStepInput stepInput = new RunBackendStepInput(TestUtils.defineInstance());
      stepInput.setSession(TestUtils.getMockSession());
      stepInput.setTableName(TestUtils.defineTablePerson().getName());
      stepInput.addValue(QUploadedFile.DEFAULT_UPLOADED_FILE_FIELD_NAME, key);

      RunBackendStepOutput stepOutput = new RunBackendStepOutput();

      assertThrows(QUserFacingException.class, () ->
      {
         new BulkInsertReceiveFileStep().run(stepInput, stepOutput);
      });
   }

}