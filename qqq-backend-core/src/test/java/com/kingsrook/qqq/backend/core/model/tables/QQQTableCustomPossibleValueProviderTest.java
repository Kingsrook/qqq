/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.tables;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QQQTableCustomPossibleValueProvider 
 *******************************************************************************/
class QQQTableCustomPossibleValueProviderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      qInstance.addTable(new QTableMetaData()
         .withName("hidden")
         .withIsHidden(true)
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER)));

      qInstance.addTable(new QTableMetaData()
         .withName("restricted")
         .withPermissionRules(new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION))
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER)));

      new QQQTablesMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);

      QContext.init(qInstance, newSession());

      for(String tableName : qInstance.getTables().keySet())
      {
         QQQTableTableManager.getQQQTableId(qInstance, tableName);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetPossibleValue() throws QException
   {
      Integer                             personTableId = QQQTableTableManager.getQQQTableId(QContext.getQInstance(), TestUtils.TABLE_NAME_PERSON);
      QQQTableCustomPossibleValueProvider provider      = new QQQTableCustomPossibleValueProvider();

      QPossibleValue<Integer> possibleValue = provider.getPossibleValue(personTableId);
      assertEquals(personTableId, possibleValue.getId());
      assertEquals("Person", possibleValue.getLabel());

      assertNull(provider.getPossibleValue(-1));

      Integer hiddenTableId = QQQTableTableManager.getQQQTableId(QContext.getQInstance(), "hidden");
      assertNull(provider.getPossibleValue(hiddenTableId));

      Integer restrictedTableId = QQQTableTableManager.getQQQTableId(QContext.getQInstance(), "restricted");
      assertNull(provider.getPossibleValue(restrictedTableId));

      QContext.getQSession().withPermission("restricted.hasAccess");
      assertNotNull(provider.getPossibleValue(restrictedTableId));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSearchPossibleValue() throws QException
   {
      Integer personTableId     = QQQTableTableManager.getQQQTableId(QContext.getQInstance(), TestUtils.TABLE_NAME_PERSON);
      Integer shapeTableId      = QQQTableTableManager.getQQQTableId(QContext.getQInstance(), TestUtils.TABLE_NAME_SHAPE);
      Integer hiddenTableId     = QQQTableTableManager.getQQQTableId(QContext.getQInstance(), "hidden");
      Integer restrictedTableId = QQQTableTableManager.getQQQTableId(QContext.getQInstance(), "restricted");

      QQQTableCustomPossibleValueProvider provider = new QQQTableCustomPossibleValueProvider();

      List<QPossibleValue<Integer>> list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(QQQTable.TABLE_NAME));
      assertThat(list).anyMatch(p -> p.getId().equals(personTableId));
      assertThat(list).noneMatch(p -> p.getId().equals(-1));
      assertThat(list).noneMatch(p -> p.getId().equals(hiddenTableId));
      assertThat(list).noneMatch(p -> p.getId().equals(restrictedTableId));
      assertNull(provider.getPossibleValue("restricted"));

      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(QQQTable.TABLE_NAME)
         .withIdList(List.of(personTableId, shapeTableId, hiddenTableId)));
      assertEquals(2, list.size());
      assertThat(list).anyMatch(p -> p.getId().equals(personTableId));
      assertThat(list).anyMatch(p -> p.getId().equals(shapeTableId));
      assertThat(list).noneMatch(p -> p.getId().equals(hiddenTableId));

      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(QQQTable.TABLE_NAME)
         .withLabelList(List.of("Person", "Shape", "Restricted")));
      assertEquals(2, list.size());
      assertThat(list).anyMatch(p -> p.getId().equals(personTableId));
      assertThat(list).anyMatch(p -> p.getId().equals(shapeTableId));
      assertThat(list).noneMatch(p -> p.getId().equals(restrictedTableId));

      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(QQQTable.TABLE_NAME)
         .withSearchTerm("restricted"));
      assertEquals(0, list.size());

      /////////////////////////////////////////
      // add permission for restricted table //
      /////////////////////////////////////////
      QContext.getQSession().withPermission("restricted.hasAccess");
      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(QQQTable.TABLE_NAME)
         .withSearchTerm("restricted"));
      assertEquals(1, list.size());

      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(QQQTable.TABLE_NAME)
         .withLabelList(List.of("Person", "Shape", "Restricted")));
      assertEquals(3, list.size());
      assertThat(list).anyMatch(p -> p.getId().equals(personTableId));
      assertThat(list).anyMatch(p -> p.getId().equals(shapeTableId));
      assertThat(list).anyMatch(p -> p.getId().equals(restrictedTableId));

   }

}