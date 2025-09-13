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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.metadata.personalization.ExamplePersonalizer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.CaseChangeBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for GetAction
 **
 *******************************************************************************/
class GetActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
      MemoryRecordStore.resetStatistics();
   }



   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      GetInput request = new GetInput();
      request.setTableName("person");
      request.setPrimaryKey(1);
      request.setShouldGenerateDisplayValues(true);
      request.setShouldTranslatePossibleValues(true);
      GetOutput result = new GetAction().execute(request);
      assertNotNull(result);
      assertNotNull(result.getRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFilterFieldBehaviors() throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // insert one shape with a mixed-case name, one with an all-lower name //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_SHAPE).withRecords(List.of(
         new QRecord().withValue("name", "Triangle"),
         new QRecord().withValue("name", "square")
      )));

      ///////////////////////////////////////////////////////////////////////////
      // now set the shape table's name field to have a to-lower-case behavior //
      ///////////////////////////////////////////////////////////////////////////
      QInstance      qInstance = QContext.getQInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_SHAPE);
      table.withUniqueKey(new UniqueKey("name"));
      QFieldMetaData field = table.getField("name");
      field.setBehaviors(Set.of(CaseChangeBehavior.TO_LOWER_CASE));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // confirm that if we query for "Triangle", we can't find it (because query will to-lower-case the criteria) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertNull(GetAction.execute(TestUtils.TABLE_NAME_SHAPE, Map.of("name", "Triangle")));

      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      // confirm that if we query for "SQUARE", we do find it (because query will to-lower-case the criteria) //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertNotNull(GetAction.execute(TestUtils.TABLE_NAME_SHAPE, Map.of("name", "sQuArE")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTablePersonalization() throws QException
   {
      QContext.getQSession().getUser().setIdReference("jdoe");
      ExamplePersonalizer.registerInQInstance();
      ExamplePersonalizer.addCustomizableTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withRecord(new QRecord().withValue("firstName", "Darin")));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // customize firstName field to do a to-upper-case                                                   //
      // this is verifying that QueryAction.postRecordActions has access to the personalized tableMetaData //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      assertEquals("Darin", new GetAction().executeForRecord(new GetInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withPrimaryKey(1).withInputSource(QInputSource.USER)).getValueString("firstName"));
      ExamplePersonalizer.addFieldToAddForUserId(TestUtils.TABLE_NAME_PERSON_MEMORY,
         QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getField("firstName").clone().withBehavior(CaseChangeBehavior.TO_UPPER_CASE),
         QContext.getQSession().getUser().getIdReference());
      assertEquals("DARIN", new GetAction().executeForRecord(new GetInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withPrimaryKey(1).withInputSource(QInputSource.USER)).getValueString("firstName"));
   }

}
