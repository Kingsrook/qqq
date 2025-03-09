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


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QQQTableTableManager 
 *******************************************************************************/
class QQQTableTableManagerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTablesGetInsertedUponRequest() throws QException
   {
      new QQQTablesMetaDataProvider().defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);

      Integer personMemoryTableId = QQQTableTableManager.getQQQTableId(QContext.getQInstance(), TestUtils.TABLE_NAME_PERSON_MEMORY);
      assertEquals(1, personMemoryTableId);

      assertEquals(1, QueryAction.execute(QQQTablesMetaDataProvider.QQQ_TABLE_CACHE_TABLE_NAME, new QQueryFilter()).size());
      assertEquals(1, QueryAction.execute(QQQTable.TABLE_NAME, new QQueryFilter()).size());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExistingTableComesBack() throws QException
   {
      new QQQTablesMetaDataProvider().defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);

      new InsertAction().execute(new InsertInput(QQQTable.TABLE_NAME).withRecordEntity(new QQQTable().withName(TestUtils.TABLE_NAME_SHAPE)));
      new InsertAction().execute(new InsertInput(QQQTable.TABLE_NAME).withRecordEntity(new QQQTable().withName(TestUtils.TABLE_NAME_ID_AND_NAME_ONLY)));

      assertEquals(0, QueryAction.execute(QQQTablesMetaDataProvider.QQQ_TABLE_CACHE_TABLE_NAME, new QQueryFilter()).size());
      assertEquals(2, QueryAction.execute(QQQTable.TABLE_NAME, new QQueryFilter()).size());

      assertEquals(2, QQQTableTableManager.getQQQTableId(QContext.getQInstance(), TestUtils.TABLE_NAME_ID_AND_NAME_ONLY));

      assertEquals(1, QueryAction.execute(QQQTablesMetaDataProvider.QQQ_TABLE_CACHE_TABLE_NAME, new QQueryFilter()).size());
      assertEquals(2, QueryAction.execute(QQQTable.TABLE_NAME, new QQueryFilter()).size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBogusTableName() throws QException
   {
      new QQQTablesMetaDataProvider().defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);
      assertNull(QQQTableTableManager.getQQQTableId(QContext.getQInstance(), "not a table"));
      assertEquals(0, QueryAction.execute(QQQTablesMetaDataProvider.QQQ_TABLE_CACHE_TABLE_NAME, new QQueryFilter()).size());
      assertEquals(0, QueryAction.execute(QQQTable.TABLE_NAME, new QQueryFilter()).size());
   }

}