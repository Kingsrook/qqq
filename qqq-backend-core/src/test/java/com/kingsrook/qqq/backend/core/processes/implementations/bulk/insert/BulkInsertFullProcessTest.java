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


import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendFieldMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfileField;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for full bulk insert process
 *******************************************************************************/
class BulkInsertFullProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvRow1()
   {
      return ("""
         "0","2021-10-26 14:39:37","2021-10-26 14:39:37","John","Doe","1980-01-01","john@doe.com","Missouri",42
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvRow2()
   {
      return ("""
         "0","2021-10-26 14:39:37","2021-10-26 14:39:37","Jane","Doe","1981-01-01","john@doe.com","Illinois",
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvHeaderUsingLabels()
   {
      return ("""
         "Id","Create Date","Modify Date","First Name","Last Name","Birth Date","Email","Home State",noOfShoes
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws Exception
   {
      String defaultEmail = "noone@kingsrook.com";

      ///////////////////////////////////////
      // make sure table is empty to start //
      ///////////////////////////////////////
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY)).isEmpty();

      QInstance qInstance   = QContext.getQInstance();
      String    processName = "PersonBulkInsertV2";
      new QInstanceEnricher(qInstance).defineTableBulkInsert(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), processName);

      /////////////////////////////////////////////////////////
      // start the process - expect to go to the upload step //
      /////////////////////////////////////////////////////////
      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(processName);
      runProcessInput.addValue("tableName", TestUtils.TABLE_NAME_PERSON_MEMORY);
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
      String           processUUID      = runProcessOutput.getProcessUUID();
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo("upload");

      //////////////////////////////
      // simulate the file upload //
      //////////////////////////////
      String       storageReference = UUID.randomUUID() + ".csv";
      StorageInput storageInput     = new StorageInput(TestUtils.TABLE_NAME_MEMORY_STORAGE).withReference(storageReference);
      try(OutputStream outputStream = new StorageAction().createOutputStream(storageInput))
      {
         outputStream.write((getPersonCsvHeaderUsingLabels() + getPersonCsvRow1() + getPersonCsvRow2()).getBytes());
      }
      catch(IOException e)
      {
         throw (e);
      }

      //////////////////////////
      // continue post-upload //
      //////////////////////////
      runProcessInput.setProcessUUID(processUUID);
      runProcessInput.setStartAfterStep("upload");
      runProcessInput.addValue("theFile", new ArrayList<>(List.of(storageInput)));
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertEquals(List.of("Id", "Create Date", "Modify Date", "First Name", "Last Name", "Birth Date", "Email", "Home State", "noOfShoes"), runProcessOutput.getValue("headerValues"));
      assertEquals(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I"), runProcessOutput.getValue("headerLetters"));

      //////////////////////////////////////////////////////
      // assert about the suggested mapping that was done //
      //////////////////////////////////////////////////////
      Serializable bulkLoadProfile = runProcessOutput.getValue("bulkLoadProfile");
      assertThat(bulkLoadProfile).isInstanceOf(BulkLoadProfile.class);
      assertThat(((BulkLoadProfile) bulkLoadProfile).getFieldList()).hasSizeGreaterThan(5);
      assertEquals("firstName", ((BulkLoadProfile) bulkLoadProfile).getFieldList().get(0).getFieldName());
      assertEquals(3, ((BulkLoadProfile) bulkLoadProfile).getFieldList().get(0).getColumnIndex());
      assertEquals("lastName", ((BulkLoadProfile) bulkLoadProfile).getFieldList().get(1).getFieldName());
      assertEquals(4, ((BulkLoadProfile) bulkLoadProfile).getFieldList().get(1).getColumnIndex());
      assertEquals("birthDate", ((BulkLoadProfile) bulkLoadProfile).getFieldList().get(2).getFieldName());
      assertEquals(5, ((BulkLoadProfile) bulkLoadProfile).getFieldList().get(2).getColumnIndex());

      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo("fileMapping");

      ////////////////////////////////////////////////////////////////////////////////
      // all subsequent steps will want these data - so set up a lambda to set them //
      ////////////////////////////////////////////////////////////////////////////////
      Consumer<RunProcessInput> addProfileToRunProcessInput = (RunProcessInput input) ->
      {
         input.addValue("version", "v1");
         input.addValue("layout", "FLAT");
         input.addValue("hasHeaderRow", "true");
         input.addValue("fieldListJSON", JsonUtils.toJson(List.of(
            new BulkLoadProfileField().withFieldName("firstName").withColumnIndex(3),
            new BulkLoadProfileField().withFieldName("lastName").withColumnIndex(4),
            new BulkLoadProfileField().withFieldName("email").withDefaultValue(defaultEmail),
            new BulkLoadProfileField().withFieldName("homeStateId").withColumnIndex(7).withDoValueMapping(true).withValueMappings(Map.of("Illinois", 1)),
            new BulkLoadProfileField().withFieldName("noOfShoes").withColumnIndex(8)
         )));
      };

      ////////////////////////////////
      // continue post file-mapping //
      ////////////////////////////////
      runProcessInput.setStartAfterStep("fileMapping");
      addProfileToRunProcessInput.accept(runProcessInput);
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      Serializable valueMappingField = runProcessOutput.getValue("valueMappingField");
      assertThat(valueMappingField).isInstanceOf(QFrontendFieldMetaData.class);
      assertEquals("homeStateId", ((QFrontendFieldMetaData) valueMappingField).getName());
      assertEquals(List.of("Missouri", "Illinois"), runProcessOutput.getValue("fileValues"));
      assertEquals(List.of("homeStateId"), runProcessOutput.getValue("fieldNamesToDoValueMapping"));
      assertEquals(Map.of(1, "IL"), runProcessOutput.getValue("mappedValueLabels"));
      assertEquals(0, runProcessOutput.getValue("valueMappingFieldIndex"));
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo("valueMapping");

      /////////////////////////////////
      // continue post value-mapping //
      /////////////////////////////////
      runProcessInput.setStartAfterStep("valueMapping");
      runProcessInput.addValue("mappedValuesJSON", JsonUtils.toJson(Map.of("Illinois", 1, "Missouri", 2)));
      addProfileToRunProcessInput.accept(runProcessInput);
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo("review");

      /////////////////////////////////
      // continue post review screen //
      /////////////////////////////////
      runProcessInput.setStartAfterStep("review");
      addProfileToRunProcessInput.accept(runProcessInput);
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertThat(runProcessOutput.getRecords()).hasSize(2);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo("result");
      assertThat(runProcessOutput.getValues().get(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY)).isNotNull().isInstanceOf(List.class);
      assertThat(runProcessOutput.getException()).isEmpty();

      ////////////////////////////////////
      // query for the inserted records //
      ////////////////////////////////////
      List<QRecord> records = TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY);

      assertEquals("John", records.get(0).getValueString("firstName"));
      assertEquals("Jane", records.get(1).getValueString("firstName"));

      assertNotNull(records.get(0).getValue("id"));
      assertNotNull(records.get(1).getValue("id"));

      assertEquals(2, records.get(0).getValueInteger("homeStateId"));
      assertEquals(1, records.get(1).getValueInteger("homeStateId"));

      assertEquals(defaultEmail, records.get(0).getValueString("email"));
      assertEquals(defaultEmail, records.get(1).getValueString("email"));

      assertEquals(42, records.get(0).getValueInteger("noOfShoes"));
      assertNull(records.get(1).getValue("noOfShoes"));
   }

}