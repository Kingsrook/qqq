/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.util.List;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for com.kingsrook.qqq.backend.core.actions.scripts.ScriptApi
 *******************************************************************************/
class ScriptApiTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      ScriptApi api = new ScriptApi();
      assertThat(api.newFilterCriteria()).isInstanceOf(QFilterCriteria.class);
      assertThat(api.newFilterOrderBy()).isInstanceOf(QFilterOrderBy.class);
      assertThat(api.newQueryFilter()).isInstanceOf(QQueryFilter.class);
      assertThat(api.newRecord()).isInstanceOf(QRecord.class);
      assertThat(api.newQueryInput()).isInstanceOf(QueryInput.class);

      String tableName = TestUtils.TABLE_NAME_PERSON_MEMORY;
      String uuid      = UUID.randomUUID().toString();
      api.insert(tableName, new QRecord().withValue("firstName", uuid));
      List<QRecord> queryResult = api.query(api.newQueryInput()
         .withTableName(tableName)
         .withFilter(api.newQueryFilter()
            .withCriteria(api.newFilterCriteria()
               .withFieldName("firstName")
               .withOperator("EQUALS")
               .withValues(List.of(uuid)))));
      assertEquals(1, queryResult.size());
      assertEquals(uuid, queryResult.get(0).getValueString("firstName"));

      String newUUID = UUID.randomUUID().toString();
      api.update(tableName, api.newRecord().withValue("id", queryResult.get(0).getValue("id")).withValue("lastName", newUUID));
      QQueryFilter filter = api.newQueryFilter()
         .withCriteria(api.newFilterCriteria()
            .withFieldName("lastName")
            .withOperator("EQUALS")
            .withValues(List.of(newUUID)));
      queryResult = api.query(tableName, filter);
      assertEquals(1, queryResult.size());
      assertEquals(newUUID, queryResult.get(0).getValueString("lastName"));

      api.delete(tableName, queryResult.get(0).getValue("id"));
      queryResult = api.query(tableName, filter);
      assertEquals(0, queryResult.size());

      api.delete(tableName, filter);
   }

}