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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


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
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for TablesCustomPossibleValueProvider 
 *******************************************************************************/
class TablesCustomPossibleValueProviderTest extends BaseTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach()
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

      qInstance.addPossibleValueSource(TablesPossibleValueSourceMetaDataProvider.defineTablesPossibleValueSource(qInstance));

      QContext.init(qInstance, newSession());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetPossibleValue()
   {
      TablesCustomPossibleValueProvider provider = new TablesCustomPossibleValueProvider();

      QPossibleValue<String> possibleValue = provider.getPossibleValue(TestUtils.TABLE_NAME_PERSON);
      assertEquals(TestUtils.TABLE_NAME_PERSON, possibleValue.getId());
      assertEquals("Person", possibleValue.getLabel());

      assertNull(provider.getPossibleValue("no-such-table"));
      assertNull(provider.getPossibleValue("hidden"));
      assertNull(provider.getPossibleValue("restricted"));

      QContext.getQSession().withPermission("restricted.hasAccess");
      assertNotNull(provider.getPossibleValue("restricted"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSearchPossibleValue() throws QException
   {
      TablesCustomPossibleValueProvider provider = new TablesCustomPossibleValueProvider();

      List<QPossibleValue<String>> list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(TablesPossibleValueSourceMetaDataProvider.NAME));
      assertThat(list).anyMatch(p -> p.getId().equals(TestUtils.TABLE_NAME_PERSON));
      assertThat(list).noneMatch(p -> p.getId().equals("no-such-table"));
      assertThat(list).noneMatch(p -> p.getId().equals("hidden"));
      assertThat(list).noneMatch(p -> p.getId().equals("restricted"));
      assertNull(provider.getPossibleValue("restricted"));

      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(TablesPossibleValueSourceMetaDataProvider.NAME)
         .withIdList(List.of(TestUtils.TABLE_NAME_PERSON, TestUtils.TABLE_NAME_SHAPE, "hidden")));
      assertEquals(2, list.size());
      assertThat(list).anyMatch(p -> p.getId().equals(TestUtils.TABLE_NAME_PERSON));
      assertThat(list).anyMatch(p -> p.getId().equals(TestUtils.TABLE_NAME_SHAPE));
      assertThat(list).noneMatch(p -> p.getId().equals("hidden"));

      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(TablesPossibleValueSourceMetaDataProvider.NAME)
         .withLabelList(List.of("Person", "Shape", "Restricted")));
      assertEquals(2, list.size());
      assertThat(list).anyMatch(p -> p.getId().equals(TestUtils.TABLE_NAME_PERSON));
      assertThat(list).anyMatch(p -> p.getId().equals(TestUtils.TABLE_NAME_SHAPE));
      assertThat(list).noneMatch(p -> p.getId().equals("restricted"));

      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(TablesPossibleValueSourceMetaDataProvider.NAME)
         .withSearchTerm("restricted"));
      assertEquals(0, list.size());

      /////////////////////////////////////////
      // add permission for restricted table //
      /////////////////////////////////////////
      QContext.getQSession().withPermission("restricted.hasAccess");
      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(TablesPossibleValueSourceMetaDataProvider.NAME)
         .withSearchTerm("restricted"));
      assertEquals(1, list.size());

      list = provider.search(new SearchPossibleValueSourceInput()
         .withPossibleValueSourceName(TablesPossibleValueSourceMetaDataProvider.NAME)
         .withLabelList(List.of("Person", "Shape", "Restricted")));
      assertEquals(3, list.size());
      assertThat(list).anyMatch(p -> p.getId().equals(TestUtils.TABLE_NAME_PERSON));
      assertThat(list).anyMatch(p -> p.getId().equals(TestUtils.TABLE_NAME_SHAPE));
      assertThat(list).anyMatch(p -> p.getId().equals("restricted"));

   }

}