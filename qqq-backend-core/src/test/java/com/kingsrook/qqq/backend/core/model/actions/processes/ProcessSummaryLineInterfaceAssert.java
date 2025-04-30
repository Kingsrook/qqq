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

package com.kingsrook.qqq.backend.core.model.actions.processes;


import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** AssertJ assert class for ProcessSummaryLine.
 *******************************************************************************/
public class ProcessSummaryLineInterfaceAssert extends AbstractAssert<ProcessSummaryLineInterfaceAssert, ProcessSummaryLineInterface>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   protected ProcessSummaryLineInterfaceAssert(ProcessSummaryLineInterface actual, Class<?> selfType)
   {
      super(actual, selfType);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ProcessSummaryLineInterfaceAssert assertThat(ProcessSummaryLineInterface actual)
   {
      return (new ProcessSummaryLineInterfaceAssert(actual, ProcessSummaryLineInterfaceAssert.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLineInterfaceAssert hasCount(Integer count)
   {
      if(actual instanceof ProcessSummaryLine psl)
      {
         assertEquals(count, psl.getCount(), "Expected count in process summary line");
      }
      else
      {
         failWithMessage("ProcessSummaryLineInterface is not of concrete type ProcessSummaryLine (is: " + actual.getClass().getSimpleName() + ")");
      }

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLineInterfaceAssert hasStatus(Status status)
   {
      assertEquals(status, actual.getStatus(), "Expected status in process summary line");
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLineInterfaceAssert hasMessageMatching(String regExp)
   {
      if(actual.getMessage() == null)
      {
         actual.prepareForFrontend(false);
      }

      Assertions.assertThat(actual.getMessage()).matches(regExp);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLineInterfaceAssert hasMessageContaining(String substring)
   {
      if(actual.getMessage() == null)
      {
         actual.prepareForFrontend(false);
      }

      Assertions.assertThat(actual.getMessage()).contains(substring);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLineInterfaceAssert doesNotHaveMessageMatching(String regExp)
   {
      if(actual.getMessage() == null)
      {
         actual.prepareForFrontend(false);
      }

      Assertions.assertThat(actual.getMessage()).doesNotMatch(regExp);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLineInterfaceAssert doesNotHaveMessageContaining(String substring)
   {
      if(actual.getMessage() == null)
      {
         actual.prepareForFrontend(false);
      }

      Assertions.assertThat(actual.getMessage()).doesNotContain(substring);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLineInterfaceAssert hasAnyBulletsOfTextContaining(String substring)
   {
      if(actual instanceof ProcessSummaryLine psl)
      {
         Assertions.assertThat(psl.getBulletsOfText())
            .isNotNull()
            .anyMatch(s -> s.contains(substring));
      }
      else
      {
         Assertions.fail("Process Summary Line was not the expected type.");
      }

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryLineInterfaceAssert doesNotHaveAnyBulletsOfTextContaining(String substring)
   {
      if(actual instanceof ProcessSummaryLine psl)
      {
         if(psl.getBulletsOfText() != null)
         {
            Assertions.assertThat(psl.getBulletsOfText())
               .noneMatch(s -> s.contains(substring));
         }
      }

      return (this);
   }


   /***************************************************************************
    **
    ***************************************************************************/
   public ProcessSummaryLineInterface getLine()
   {
      return actual;
   }

}
