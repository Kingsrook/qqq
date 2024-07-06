/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for QFieldMetaData 
 *******************************************************************************/
class QFieldMetaDataTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldBehaviors()
   {
      /////////////////////////////////////////
      // create field - assert default state //
      /////////////////////////////////////////
      QFieldMetaData field = new QFieldMetaData("createDate", QFieldType.DATE_TIME);
      assertTrue(CollectionUtils.nullSafeIsEmpty(field.getBehaviors()));
      assertNull(field.getBehaviorOnlyIfSet(DynamicDefaultValueBehavior.class));
      assertEquals(DynamicDefaultValueBehavior.NONE, field.getBehaviorOrDefault(new QInstance(), DynamicDefaultValueBehavior.class));

      //////////////////////////////////////
      // add NONE behavior - assert state //
      //////////////////////////////////////
      field.withBehavior(DynamicDefaultValueBehavior.NONE);
      assertEquals(1, field.getBehaviors().size());
      assertEquals(DynamicDefaultValueBehavior.NONE, field.getBehaviorOnlyIfSet(DynamicDefaultValueBehavior.class));
      assertEquals(DynamicDefaultValueBehavior.NONE, field.getBehaviorOrDefault(new QInstance(), DynamicDefaultValueBehavior.class));

      /////////////////////////////////////////////////////////
      // replace behavior - assert it got rid of the old one //
      /////////////////////////////////////////////////////////
      field.withBehavior(DynamicDefaultValueBehavior.CREATE_DATE);
      assertEquals(1, field.getBehaviors().size());
      assertEquals(DynamicDefaultValueBehavior.CREATE_DATE, field.getBehaviorOnlyIfSet(DynamicDefaultValueBehavior.class));
      assertEquals(DynamicDefaultValueBehavior.CREATE_DATE, field.getBehaviorOrDefault(new QInstance(), DynamicDefaultValueBehavior.class));
   }

}