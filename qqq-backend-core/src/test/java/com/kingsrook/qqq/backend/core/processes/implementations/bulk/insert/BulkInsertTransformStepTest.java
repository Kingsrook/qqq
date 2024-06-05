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


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for BulkInsertTransformStep
 *******************************************************************************/
class BulkInsertTransformStepTest extends BaseTest
{
   public static final String TABLE_NAME = "ukTest";



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
   @Test
   void testMultipleUniqueKeys() throws Exception
   {
      QInstance instance = TestUtils.defineInstance();
      QTableMetaData table = defineTable(new QTableMetaData()
         .withName(TABLE_NAME)
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withUniqueKey(new UniqueKey().withFieldName("uuid"))
         .withUniqueKey(new UniqueKey().withFieldName("sku").withFieldName("storeId")), instance);

      reInitInstanceInContext(instance);

      ////////////////////////////////////////////////////////////
      // insert some records that will cause some UK violations //
      ////////////////////////////////////////////////////////////
      TestUtils.insertRecords(instance, table, List.of(
         newQRecord("uuid-A", "SKU-1", 1),
         newQRecord("uuid-B", "SKU-2", 1),
         newQRecord("uuid-C", "SKU-2", 2)
      ));

      ///////////////////////////////////////////
      // setup & run the bulk insert transform //
      ///////////////////////////////////////////
      BulkInsertTransformStep bulkInsertTransformStep = new BulkInsertTransformStep();
      RunBackendStepInput     input                   = new RunBackendStepInput();
      RunBackendStepOutput    output                  = new RunBackendStepOutput();

      input.setTableName(TABLE_NAME);
      input.setStepName(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE);
      input.setRecords(List.of(
         newQRecord("uuid-1", "SKU-A", 1), // OK.
         newQRecord("uuid-1", "SKU-B", 1), // violate uuid UK in this set
         newQRecord("uuid-2", "SKU-C", 1), // OK.
         newQRecord("uuid-3", "SKU-C", 2), // OK.
         newQRecord("uuid-4", "SKU-C", 1), // violate sku/storeId UK in this set
         newQRecord("uuid-A", "SKU-X", 1), // violate uuid UK from pre-existing records
         newQRecord("uuid-D", "SKU-2", 1)  // violate sku/storeId UK from pre-existing records
      ));
      bulkInsertTransformStep.preRun(input, output);
      bulkInsertTransformStep.runOnePage(input, output);

      ///////////////////////////////////////////////////////
      // assert about the records that passed successfully //
      ///////////////////////////////////////////////////////
      assertEquals(3, output.getRecords().size());
      assertThat(output.getRecords())
         .anyMatch(r -> recordEquals(r, "uuid-1", "SKU-A", 1))
         .anyMatch(r -> recordEquals(r, "uuid-2", "SKU-C", 1))
         .anyMatch(r -> recordEquals(r, "uuid-3", "SKU-C", 2));

      /////////////////////////////
      // assert about the errors //
      /////////////////////////////
      ArrayList<ProcessSummaryLineInterface> processSummary = bulkInsertTransformStep.doGetProcessSummary(output, false);
      List<ProcessSummaryLine> errorLines = processSummary.stream()
         .filter(pl -> pl instanceof ProcessSummaryLine psl && psl.getStatus().equals(Status.ERROR))
         .map(pl -> ((ProcessSummaryLine) pl))
         .collect(Collectors.toList());
      assertEquals(2, errorLines.size());
      assertThat(errorLines)
         .anyMatch(psl -> psl.getMessage().contains("Uuid") && psl.getCount().equals(2))
         .anyMatch(psl -> psl.getMessage().contains("Sku and Store Id") && psl.getCount().equals(2));
   }



   private QTableMetaData defineTable(QTableMetaData TABLE_NAME, QInstance instance)
   {
      QTableMetaData table = TABLE_NAME
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("uuid", QFieldType.STRING))
         .withField(new QFieldMetaData("sku", QFieldType.STRING))
         .withField(new QFieldMetaData("storeId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING));
      instance.addTable(table);
      return table;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoUniqueKeys() throws Exception
   {
      QInstance instance = TestUtils.defineInstance();
      QTableMetaData table = defineTable(new QTableMetaData()
         .withName(TABLE_NAME)
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME), instance);
      reInitInstanceInContext(instance);

      ////////////////////////////////////////////////////////////
      // insert some records that will cause some UK violations //
      ////////////////////////////////////////////////////////////
      TestUtils.insertRecords(instance, table, List.of(
         newQRecord("uuid-A", "SKU-1", 1),
         newQRecord("uuid-B", "SKU-2", 1),
         newQRecord("uuid-C", "SKU-2", 2)
      ));

      ///////////////////////////////////////////
      // setup & run the bulk insert transform //
      ///////////////////////////////////////////
      BulkInsertTransformStep bulkInsertTransformStep = new BulkInsertTransformStep();
      RunBackendStepInput     input                   = new RunBackendStepInput();
      RunBackendStepOutput    output                  = new RunBackendStepOutput();

      input.setTableName(TABLE_NAME);
      input.setStepName(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE);
      input.setRecords(List.of(
         newQRecord("uuid-1", "SKU-A", 1), // OK.
         newQRecord("uuid-1", "SKU-B", 1), // violate uuid UK in this set
         newQRecord("uuid-2", "SKU-C", 1), // OK.
         newQRecord("uuid-3", "SKU-C", 2), // OK.
         newQRecord("uuid-4", "SKU-C", 1), // violate sku/storeId UK in this set
         newQRecord("uuid-A", "SKU-X", 1), // violate uuid UK from pre-existing records
         newQRecord("uuid-D", "SKU-2", 1)  // violate sku/storeId UK from pre-existing records
      ));
      bulkInsertTransformStep.preRun(input, output);
      bulkInsertTransformStep.runOnePage(input, output);

      ///////////////////////////////////////////////////////
      // assert that all records pass.
      ///////////////////////////////////////////////////////
      assertEquals(7, output.getRecords().size());
   }



   private boolean recordEquals(QRecord record, String uuid, String sku, Integer storeId)
   {
      return (record.getValue("uuid").equals(uuid)
         && record.getValue("sku").equals(sku)
         && record.getValue("storeId").equals(storeId));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord newQRecord(String uuid, String sku, int storeId)
   {
      return new QRecord()
         .withValue("uuid", uuid)
         .withValue("sku", sku)
         .withValue("storeId", storeId)
         .withValue("name", "Some Item");
   }

}