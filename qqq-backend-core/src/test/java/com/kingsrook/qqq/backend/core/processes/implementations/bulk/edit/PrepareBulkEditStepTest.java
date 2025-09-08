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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.edit;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator.EQUALS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for prepare bulk edit step
 *******************************************************************************/
class PrepareBulkEditStepTest extends BaseTest
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
   @Test
   void testMultipleValuesInDependentField() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().setSecurityKeyValues(Map.of(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, List.of(true)));
      qInstance.getTable(TestUtils.TABLE_NAME_ORDER).getField("storeId").withPossibleValueSourceFilter(new QQueryFilter(new QFilterCriteria("storeId", EQUALS, "${input.storeId}??${processValues.possibleValueFilterValueStoreId}")));

      //////////////////////////////
      // insert some test records //
      //////////////////////////////
      TestUtils.insertRecords(qInstance.getTable(TestUtils.TABLE_NAME_ORDER), List.of(
         new QRecord().withValue("id", 1).withValue("storeId", 1),
         new QRecord().withValue("id", 2).withValue("storeId", 2),
         new QRecord().withValue("id", 3).withValue("storeId", 3)
      ));

      RunBackendStepInput input = new RunBackendStepInput();
      input.setTableName(TestUtils.TABLE_NAME_ORDER);
      input.addValue("recordIds", "1,2,3");
      RunBackendStepOutput output = new RunBackendStepOutput();
      new PrepareBulkEditStep().run(input, output);

      assertNotNull(output.getValue("nonDistinctPVSFields"));
      ListingHash<String, String> nonDistinctFields = (ListingHash<String, String>) output.getValue("nonDistinctPVSFields");
      assertNotNull(nonDistinctFields.get("Store Id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSameValueInDependentField() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().setSecurityKeyValues(Map.of(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, List.of(true)));
      qInstance.getTable(TestUtils.TABLE_NAME_ORDER).getField("storeId").withPossibleValueSourceFilter(new QQueryFilter(new QFilterCriteria("storeId", EQUALS, "${input.storeId}??${processValues.possibleValueFilterValueStoreId}")));

      //////////////////////////////
      // insert some test records //
      //////////////////////////////
      TestUtils.insertRecords(qInstance.getTable(TestUtils.TABLE_NAME_ORDER), List.of(
         new QRecord().withValue("id", 1).withValue("storeId", 3),
         new QRecord().withValue("id", 2).withValue("storeId", 3),
         new QRecord().withValue("id", 3).withValue("storeId", 3)
      ));

      RunBackendStepInput input = new RunBackendStepInput();
      input.setTableName(TestUtils.TABLE_NAME_ORDER);
      input.addValue("recordIds", "1,2,3");
      RunBackendStepOutput output = new RunBackendStepOutput();
      new PrepareBulkEditStep().run(input, output);

      assertNull(output.getValue("nonDistinctPVSFields"));
      assertNotNull(output.getValue("possibleValueFilterValueStoreId"));
      assertThat(output.getValueInteger("possibleValueFilterValueStoreId")).isEqualTo(3);
   }
}
