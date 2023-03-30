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


import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for ObjectUtils
 *******************************************************************************/
class ObjectUtilsTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      assertThatThrownBy(() -> ObjectUtils.requireNonNullElse(null)).isInstanceOf(NullPointerException.class);
      assertThatThrownBy(() -> ObjectUtils.requireNonNullElse(null, null)).isInstanceOf(NullPointerException.class);
      assertThatThrownBy(() -> ObjectUtils.requireNonNullElse(null, null, null)).isInstanceOf(NullPointerException.class);
      assertEquals("a", ObjectUtils.requireNonNullElse("a", "b"));
      assertEquals("b", ObjectUtils.requireNonNullElse(null, "b", "c"));
      assertEquals("c", ObjectUtils.requireNonNullElse(null, null, "c"));
   }

}