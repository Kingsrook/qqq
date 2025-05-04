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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfileField;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for BulkInsertPrepareMappingStep 
 *******************************************************************************/
class BulkInsertPrepareFileMappingStepTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("PointlessArithmeticExpression")
   @Test
   void testToHeaderLetter()
   {
      assertEquals("A", BulkInsertPrepareFileMappingStep.toHeaderLetter(0));
      assertEquals("B", BulkInsertPrepareFileMappingStep.toHeaderLetter(1));
      assertEquals("Z", BulkInsertPrepareFileMappingStep.toHeaderLetter(25));

      assertEquals("AA", BulkInsertPrepareFileMappingStep.toHeaderLetter(26 + 0));
      assertEquals("AB", BulkInsertPrepareFileMappingStep.toHeaderLetter(26 + 1));
      assertEquals("AZ", BulkInsertPrepareFileMappingStep.toHeaderLetter(26 + 25));

      assertEquals("BA", BulkInsertPrepareFileMappingStep.toHeaderLetter(2 * 26 + 0));
      assertEquals("BB", BulkInsertPrepareFileMappingStep.toHeaderLetter(2 * 26 + 1));
      assertEquals("BZ", BulkInsertPrepareFileMappingStep.toHeaderLetter(2 * 26 + 25));

      assertEquals("ZA", BulkInsertPrepareFileMappingStep.toHeaderLetter(26 * 26 + 0));
      assertEquals("ZB", BulkInsertPrepareFileMappingStep.toHeaderLetter(26 * 26 + 1));
      assertEquals("ZZ", BulkInsertPrepareFileMappingStep.toHeaderLetter(26 * 26 + 25));

      assertEquals("AAA", BulkInsertPrepareFileMappingStep.toHeaderLetter(27 * 26 + 0));
      assertEquals("AAB", BulkInsertPrepareFileMappingStep.toHeaderLetter(27 * 26 + 1));
      assertEquals("AAC", BulkInsertPrepareFileMappingStep.toHeaderLetter(27 * 26 + 2));

      assertEquals("ABA", BulkInsertPrepareFileMappingStep.toHeaderLetter(28 * 26 + 0));
      assertEquals("ABB", BulkInsertPrepareFileMappingStep.toHeaderLetter(28 * 26 + 1));

      assertEquals("BAA", BulkInsertPrepareFileMappingStep.toHeaderLetter(2 * 26 * 26 + 26 + 0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws Exception
   {
      String fileName = "personFile.csv";

      StorageInput    storageInput    = new StorageInput(TestUtils.TABLE_NAME_MEMORY_STORAGE).withReference(fileName);
      OutputStream    outputStream    = new StorageAction().createOutputStream(storageInput);
      outputStream.write("""
         name,noOfShoes
         John,2
         Jane,4
         """.getBytes(StandardCharsets.UTF_8));
      outputStream.close();

      RunProcessInput runProcessInput = new RunProcessInput();
      BulkInsertStepUtils.setStorageInputForTheFile(runProcessInput, storageInput);
      runProcessInput.addValue("tableName", TestUtils.TABLE_NAME_PERSON_MEMORY);
      runProcessInput.addValue("prepopulatedValues", JsonUtils.toJson(Map.of("homeStateId", 1)));

      RunBackendStepInput runBackendStepInput = new RunBackendStepInput(runProcessInput.getProcessState());
      RunBackendStepOutput runBackendStepOutput = new RunBackendStepOutput();

      new BulkInsertPrepareFileMappingStep().run(runBackendStepInput, runBackendStepOutput);

      BulkLoadProfile bulkLoadProfile = (BulkLoadProfile) runBackendStepOutput.getValue("suggestedBulkLoadProfile");
      Optional<BulkLoadProfileField> homeStateId = bulkLoadProfile.getFieldList().stream().filter(f -> f.getFieldName().equals("homeStateId")).findFirst();
      assertThat(homeStateId).isPresent();
      assertEquals("1", homeStateId.get().getDefaultValue());
   }

}