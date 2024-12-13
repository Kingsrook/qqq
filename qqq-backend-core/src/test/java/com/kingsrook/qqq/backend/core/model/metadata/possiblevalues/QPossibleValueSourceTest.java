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

package com.kingsrook.qqq.backend.core.model.metadata.possiblevalues;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Unit test for QPossibleValueSource 
 *******************************************************************************/
class QPossibleValueSourceTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithValuesFromEnum()
   {
      assertThatThrownBy(() -> new QPossibleValueSource().withValuesFromEnum(DupeIds.values()))
         .isInstanceOf(QRuntimeException.class)
         .hasMessageContaining("Duplicated id(s)")
         .hasMessageMatching(".*: \\[1]$");
   }


   /***************************************************************************
    **
    ***************************************************************************/
   private enum DupeIds implements PossibleValueEnum<Integer>
   {
      ONE_A(1, "A"),
      TWO_B(2, "B"),
      ONE_C(1, "C");


      private final int id;
      private final String label;



      /***************************************************************************
       **
       ***************************************************************************/
      DupeIds(int id, String label)
      {
         this.id = id;
         this.label = label;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public Integer getPossibleValueId()
      {
         return id;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public String getPossibleValueLabel()
      {
         return label;
      }
   }
}