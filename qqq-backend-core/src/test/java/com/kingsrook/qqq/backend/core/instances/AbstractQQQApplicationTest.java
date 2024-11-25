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

package com.kingsrook.qqq.backend.core.instances;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for AbstractQQQApplication 
 *******************************************************************************/
class AbstractQQQApplicationTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      TestApplication testApplication = new TestApplication();
      QInstance       qInstance       = testApplication.defineQInstance();
      assertEquals(1, qInstance.getTables().size());
      assertTrue(qInstance.getTables().containsKey("testTable"));

      assertThatThrownBy(() -> testApplication.defineValidatedQInstance())
         .hasMessageContaining("validation");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class TestApplication extends AbstractQQQApplication
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QInstance defineQInstance() throws QException
      {
         QInstance qInstance = new QInstance();
         qInstance.addTable(new QTableMetaData().withName("testTable"));
         return (qInstance);
      }
   }
}