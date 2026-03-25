/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit tests for {@link StringLengthFunction}
 *******************************************************************************/
class StringLengthFunctionTest extends BaseTest
{
   private final StringLengthFunction function = new StringLengthFunction();



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testBasicLength()
   {
      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
         .withFieldName("value");

      assertEquals(5, function.apply(ff, new QRecord().withValue("value", "Hello")));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testEmptyString()
   {
      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
         .withFieldName("value");

      assertEquals(0, function.apply(ff, new QRecord().withValue("value", "")));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testNullSource()
   {
      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
         .withFieldName("value");

      assertNull(function.apply(ff, new QRecord().withValue("value", null)));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testLongerString()
   {
      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
         .withFieldName("value");

      assertEquals(11, function.apply(ff, new QRecord().withValue("value", "Hello World")));
   }

}
