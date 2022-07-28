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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.basic;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for BasicETLTransformFunction
 *******************************************************************************/
class BasicETLTransformFunctionTest
{

   /*******************************************************************************
    ** Test the removeNonNumericValuesFromMappedRecords function
    *******************************************************************************/
   @Test
   void testRemoveNonNumericValuesFromMappedRecords()
   {
      assertNull(doRemoveNonNumericValuesFromMappedRecords(QFieldType.INTEGER, null));
      assertNull(doRemoveNonNumericValuesFromMappedRecords(QFieldType.INTEGER, "foo"));
      assertNull(doRemoveNonNumericValuesFromMappedRecords(QFieldType.INTEGER, "1foo"));
      assertEquals("1", doRemoveNonNumericValuesFromMappedRecords(QFieldType.INTEGER, "1"));
      assertEquals("1000", doRemoveNonNumericValuesFromMappedRecords(QFieldType.INTEGER, "1,000"));
      assertEquals("1000000", doRemoveNonNumericValuesFromMappedRecords(QFieldType.INTEGER, "1,000,000"));

      assertNull(doRemoveNonNumericValuesFromMappedRecords(QFieldType.DECIMAL, null));
      assertNull(doRemoveNonNumericValuesFromMappedRecords(QFieldType.DECIMAL, "foo"));
      assertNull(doRemoveNonNumericValuesFromMappedRecords(QFieldType.DECIMAL, "1foo"));
      assertEquals("1", doRemoveNonNumericValuesFromMappedRecords(QFieldType.DECIMAL, "1"));
      assertEquals("1000", doRemoveNonNumericValuesFromMappedRecords(QFieldType.DECIMAL, "1,000"));
      assertEquals("1.0", doRemoveNonNumericValuesFromMappedRecords(QFieldType.DECIMAL, "1.0"));
      assertEquals("1000.00", doRemoveNonNumericValuesFromMappedRecords(QFieldType.DECIMAL, "1,000.00"));
      assertEquals("1000000", doRemoveNonNumericValuesFromMappedRecords(QFieldType.DECIMAL, "1,000,000"));
   }



   private String doRemoveNonNumericValuesFromMappedRecords(QFieldType fieldType, String inputValue)
   {
      String field = "field";
      QTableMetaData table = new QTableMetaData()
         .withField(new QFieldMetaData(field, fieldType));

      List<QRecord> records = List.of(new QRecord().withValue(field, inputValue));
      new BasicETLTransformFunction().removeNonNumericValuesFromMappedRecords(table, records);
      return (records.get(0).getValueString(field));
   }
}