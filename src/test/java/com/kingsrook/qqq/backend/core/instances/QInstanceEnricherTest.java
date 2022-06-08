/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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


import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QInstanceEnricher
 **
 *******************************************************************************/
class QInstanceEnricherTest
{

   /*******************************************************************************
    ** Test that a table missing a label gets the default label applied (name w/ UC-first).
    **
    *******************************************************************************/
   @Test
   public void test_nullTableLabelComesFromName()
   {
      QInstance qInstance = TestUtils.defineInstance();
      QTableMetaData personTable = qInstance.getTable("person");
      personTable.setLabel(null);
      assertNull(personTable.getLabel());
      new QInstanceEnricher().enrich(qInstance);
      assertEquals("Person", personTable.getLabel());
   }


   /*******************************************************************************
    ** Test that a table missing a label and a name doesn't NPE, but just keeps
    ** the name & label both null.
    **
    *******************************************************************************/
   @Test
   public void test_nullNameGivesNullLabel()
   {
      QInstance qInstance = TestUtils.defineInstance();
      QTableMetaData personTable = qInstance.getTable("person");
      personTable.setLabel(null);
      personTable.setName(null);
      assertNull(personTable.getLabel());
      assertNull(personTable.getName());
      new QInstanceEnricher().enrich(qInstance);
      assertNull(personTable.getLabel());
      assertNull(personTable.getName());
   }


   /*******************************************************************************
    ** Test that a field missing a label gets the default label applied (name w/ UC-first)
    **
    *******************************************************************************/
   @Test
   public void test_nullFieldLabelComesFromName()
   {
      QInstance qInstance = TestUtils.defineInstance();
      QFieldMetaData idField = qInstance.getTable("person").getField("id");
      idField.setLabel(null);
      assertNull(idField.getLabel());
      new QInstanceEnricher().enrich(qInstance);
      assertEquals("Id", idField.getLabel());
   }

}
