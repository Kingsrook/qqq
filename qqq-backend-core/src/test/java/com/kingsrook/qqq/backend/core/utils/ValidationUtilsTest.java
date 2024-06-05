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

package com.kingsrook.qqq.backend.core.utils;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for ValidationUtils 
 *******************************************************************************/
class ValidationUtilsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QUserFacingException
   {
      assertThatThrownBy(() -> ValidationUtils.parseAndValidateEmailAddresses("notEmail"))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("email addresses were invalid: notEmail");

      assertThatThrownBy(() -> ValidationUtils.parseAndValidateEmailAddresses("foo@bar.com, whatever"))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("email addresses were invalid: whatever");

      assertThatThrownBy(() -> ValidationUtils.parseAndValidateEmailAddresses("foo whatever"))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("email addresses were invalid: foo,whatever");

      assertEquals(List.of("foo@bar.com"), ValidationUtils.parseAndValidateEmailAddresses("foo@bar.com ")); // space here intentional!
      assertEquals(List.of("foo@bar.com"), ValidationUtils.parseAndValidateEmailAddresses("foo@bar.com;"));
      assertEquals(List.of("foo@bar.com", "fiz@buz.com"), ValidationUtils.parseAndValidateEmailAddresses("foo@bar.com, fiz@buz.com"));
   }

}