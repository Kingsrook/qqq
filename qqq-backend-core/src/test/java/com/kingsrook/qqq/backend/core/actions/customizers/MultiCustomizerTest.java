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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReferenceWithProperties;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for MultiCustomizer 
 *******************************************************************************/
class MultiCustomizerTest extends BaseTest
{
   private static List<String> events = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      events.clear();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withCustomizer(TableCustomizers.PRE_INSERT_RECORD, MultiCustomizer.of(
            new QCodeReference(CustomizerA.class),
            new QCodeReference(CustomizerB.class)
         ));
      reInitInstanceInContext(qInstance);

      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withRecord(new QRecord()));
      assertThat(events).hasSize(2)
         .contains("CustomizerA.preInsert")
         .contains("CustomizerB.preInsert");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAddingMore() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      QCodeReferenceWithProperties multiCustomizer = MultiCustomizer.of(new QCodeReference(CustomizerA.class));
      MultiCustomizer.addTableCustomizer(multiCustomizer, new QCodeReference(CustomizerB.class));

      qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).withCustomizer(TableCustomizers.PRE_INSERT_RECORD, multiCustomizer);
      reInitInstanceInContext(qInstance);

      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withRecord(new QRecord()));
      assertThat(events).hasSize(2)
         .contains("CustomizerA.preInsert")
         .contains("CustomizerB.preInsert");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class CustomizerA implements TableCustomizerInterface
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public List<QRecord> preInsert(InsertInput insertInput, List<QRecord> records, boolean isPreview) throws QException
      {
         events.add("CustomizerA.preInsert");
         return (records);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class CustomizerB implements TableCustomizerInterface
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public List<QRecord> preInsert(InsertInput insertInput, List<QRecord> records, boolean isPreview) throws QException
      {
         events.add("CustomizerB.preInsert");
         return (records);
      }
   }

}