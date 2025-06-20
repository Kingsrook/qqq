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

package com.kingsrook.qqq.backend.core.actions.tables;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 **
 *******************************************************************************/
public class UpdateActionInstanceLevelTableCustomizersTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInstanceLevelCustomizers() throws QException
   {
      QRecord record = new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_SHAPE).withRecord(new QRecord().withValue("name", "octogon"))).getRecords().get(0);

      QContext.getQInstance().withTableCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(InsertActionInstanceLevelTableCustomizersTest.BreaksEverythingCustomizer.class));
      record = new UpdateAction().execute(new UpdateInput(TestUtils.TABLE_NAME_SHAPE).withRecord(new QRecord().withValue("id", record.getValue("id")).withValue("name", "octogon"))).getRecords().get(0);
      assertEquals("Everything is broken", record.getErrorsAsString());

      QContext.getQInstance().setTableCustomizers(new ListingHash<>());
      QContext.getQInstance().withTableCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(InsertActionInstanceLevelTableCustomizersTest.SetsFirstName.class));
      QContext.getQInstance().withTableCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(InsertActionInstanceLevelTableCustomizersTest.SetsLastName.class));
      QContext.getQInstance().withTableCustomizer(TableCustomizers.POST_UPDATE_RECORD, new QCodeReference(InsertActionInstanceLevelTableCustomizersTest.DoesNothing.class));
      InsertActionInstanceLevelTableCustomizersTest.DoesNothing.callCount = 0;
      record = new UpdateAction().execute(new UpdateInput(TestUtils.TABLE_NAME_SHAPE).withRecord(new QRecord().withValue("id", record.getValue("id")).withValue("name", "octogon"))).getRecords().get(0);
      assertEquals("Jeff", record.getValueString("firstName"));
      assertEquals("Smith", record.getValueString("lastName"));
      assertNotNull(record.getValueInteger("id"));
      assertEquals(1, InsertActionInstanceLevelTableCustomizersTest.DoesNothing.callCount);
   }

}
