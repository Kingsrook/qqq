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

package com.kingsrook.qqq.backend.core.utils;


import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QRecordUtils 
 *******************************************************************************/
class QRecordUtilsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetChangedFields()
   {
      QFieldMetaData id   = new QFieldMetaData("id", QFieldType.INTEGER);
      QFieldMetaData name = new QFieldMetaData("name", QFieldType.STRING);

      assertEquals(Collections.emptyList(), QRecordUtils.getChangedFields(null, null, null));
      assertEquals(Collections.emptyList(), QRecordUtils.getChangedFields(new QRecord(), null, null));
      assertEquals(Collections.emptyList(), QRecordUtils.getChangedFields(null, new QRecord(), null));
      assertEquals(Collections.emptyList(), QRecordUtils.getChangedFields(null, null, List.of(id)));
      assertEquals(Collections.emptyList(), QRecordUtils.getChangedFields(new QRecord(), new QRecord(), List.of(id)));
      assertEquals(Collections.emptyList(), QRecordUtils.getChangedFields(new QRecord().withValue("id", 1), new QRecord().withValue("id", 1), List.of(id)));

      //////////////////////////////////////////////////////////////////
      // show that we ignore fields that aren't in the list of fields //
      //////////////////////////////////////////////////////////////////
      assertEquals(Collections.emptyList(), QRecordUtils.getChangedFields(new QRecord().withValue("id", 1), new QRecord().withValue("id", 2), List.of(name)));

      ////////////////////////////////////////////////////////////
      // show that we'll "type-convert" the values, so 1 == "1" //
      ////////////////////////////////////////////////////////////
      assertEquals(Collections.emptyList(), QRecordUtils.getChangedFields(new QRecord().withValue("id", 1), new QRecord().withValue("id", "1"), List.of(id)));

      assertEquals(List.of(id), QRecordUtils.getChangedFields(new QRecord().withValue("id", 1), new QRecord().withValue("id", 2), List.of(id)));
      assertEquals(List.of(id), QRecordUtils.getChangedFields(new QRecord(), new QRecord().withValue("id", 2), List.of(id)));
      assertEquals(List.of(id), QRecordUtils.getChangedFields(null, new QRecord().withValue("id", 2), List.of(id)));
      assertEquals(List.of(id), QRecordUtils.getChangedFields(new QRecord().withValue("id", 1), new QRecord(), List.of(id)));
      assertEquals(List.of(id), QRecordUtils.getChangedFields(new QRecord().withValue("id", 1), null, List.of(id)));
      assertEquals(List.of(id, name), QRecordUtils.getChangedFields(new QRecord().withValue("id", 1).withValue("name", "Bob"), new QRecord().withValue("id", 2).withValue("name", "N."), List.of(id, name)));
   }

}