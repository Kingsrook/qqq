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

package com.kingsrook.qqq.backend.core.state;


import java.util.UUID;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for InMemoryStateProvider
 *******************************************************************************/
public class InMemoryStateProviderTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testStateNotFound()
   {
      InMemoryStateProvider stateProvider = InMemoryStateProvider.getInstance();
      UUIDAndTypeStateKey   key           = new UUIDAndTypeStateKey(StateType.PROCESS_STATUS);

      Assertions.assertTrue(stateProvider.get(QRecord.class, key).isEmpty(), "Key not found in state should return empty");
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testSimpleStateFound()
   {
      InMemoryStateProvider stateProvider = InMemoryStateProvider.getInstance();
      UUIDAndTypeStateKey   key           = new UUIDAndTypeStateKey(StateType.PROCESS_STATUS);

      String uuid = UUID.randomUUID().toString();
      QRecord qRecord = new QRecord().withValue("uuid", uuid);
      stateProvider.put(key, qRecord);

      QRecord qRecordFromState = stateProvider.get(QRecord.class, key).get();
      Assertions.assertEquals(uuid, qRecordFromState.getValueString("uuid"), "Should read value from state persistence");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testWrongTypeOnGet()
   {
      InMemoryStateProvider stateProvider = InMemoryStateProvider.getInstance();
      UUIDAndTypeStateKey   key           = new UUIDAndTypeStateKey(StateType.PROCESS_STATUS);

      String uuid = UUID.randomUUID().toString();
      QRecord qRecord = new QRecord().withValue("uuid", uuid);
      stateProvider.put(key, qRecord);

      Assertions.assertThrows(Exception.class, () ->
      {
         stateProvider.get(QTableMetaData.class, key);
      });
   }

}