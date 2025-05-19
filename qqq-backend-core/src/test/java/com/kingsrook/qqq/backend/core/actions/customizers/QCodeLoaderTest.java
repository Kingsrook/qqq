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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.code.InitializableViaCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReferenceWithProperties;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.Timer;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QCodeLoader 
 *******************************************************************************/
class QCodeLoaderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetAdHoc()
   {
      QCodeLoader qCodeLoader = QCodeLoader.getAdHoc(QCodeLoader.class, new QCodeReference(QCodeLoader.class));
      assertThat(qCodeLoader).isInstanceOf(QCodeLoader.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   @Disabled("performance test, used during memoization change")
   void testBulkPerformance()
   {
      Timer timer = new Timer("start");
      for(int i = 0; i < 5; i++)
      {
         useCodeLoader(1_000_000);
         timer.mark("done with code loader");

         useNew(1_000_000);
         timer.mark("done with new");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void useNew(int count)
   {
      for(int i = 0; i < count; i++)
      {
         QCodeLoader qCodeLoader = new QCodeLoader();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void useCodeLoader(int count)
   {
      for(int i = 0; i < count; i++)
      {
         QCodeLoader qCodeLoader = QCodeLoader.getAdHoc(QCodeLoader.class, new QCodeReference(QCodeLoader.class));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCodeReferenceWithProperties()
   {
      assertNull(QCodeLoader.getAdHoc(SomeClass.class, new QCodeReference(SomeClass.class)));

      SomeClass someObject = QCodeLoader.getAdHoc(SomeClass.class, new QCodeReferenceWithProperties(SomeClass.class, Map.of("property", "someValue")));
      assertEquals("someValue", someObject.someProperty);

      SomeClass someOtherObject = QCodeLoader.getAdHoc(SomeClass.class, new QCodeReferenceWithProperties(SomeClass.class, Map.of("property", "someOtherValue")));
      assertEquals("someOtherValue", someOtherObject.someProperty);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class SomeClass implements InitializableViaCodeReference
   {
      private String someProperty;



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void initialize(QCodeReference codeReference)
      {
         if(codeReference instanceof QCodeReferenceWithProperties codeReferenceWithProperties)
         {
            someProperty = ValueUtils.getValueAsString(codeReferenceWithProperties.getProperties().get("property"));
         }

         if(!StringUtils.hasContent(someProperty))
         {
            throw new IllegalStateException("Missing property");
         }
      }
   }

}