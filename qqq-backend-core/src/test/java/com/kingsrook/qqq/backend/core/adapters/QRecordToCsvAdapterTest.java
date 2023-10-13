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

package com.kingsrook.qqq.backend.core.adapters;


import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QRecordToCsvAdapter 
 *******************************************************************************/
class QRecordToCsvAdapterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSanitize()
   {
      assertEquals("foo", QRecordToCsvAdapter.sanitize("foo"));

      assertEquals("""
         Homer ""Jay"" Simpson""", QRecordToCsvAdapter.sanitize("""
         Homer "Jay" Simpson"""));

      assertEquals("""
         one ""quote"" two ""quotes"".""", QRecordToCsvAdapter.sanitize("""
         one "quote" two "quotes"."""));

      assertEquals("""
         new line""", QRecordToCsvAdapter.sanitize("""
         new
         line"""));

      assertEquals("""
         end ""quote"" new line""", QRecordToCsvAdapter.sanitize("""
         end "quote" new
         line"""));
   }

}