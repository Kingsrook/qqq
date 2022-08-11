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

package com.kingsrook.qqq.backend.core.actions.values;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QValueFormatter
 *******************************************************************************/
class QValueFormatterTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFormatValue()
   {
      assertNull(QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.COMMAS), null));

      assertEquals("1", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.COMMAS), 1));
      assertEquals("1,000", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.COMMAS), 1000));
      assertEquals("1000", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(null), 1000));
      assertEquals("$1,000.00", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.CURRENCY), 1000));
      assertEquals("1,000.00", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.DECIMAL2_COMMAS), 1000));
      assertEquals("1000.00", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.DECIMAL2), 1000));

      assertEquals("1", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.COMMAS), new BigDecimal("1")));
      assertEquals("1,000", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.COMMAS), new BigDecimal("1000")));
      assertEquals("1000", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.STRING), new BigDecimal("1000")));
      assertEquals("1000", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.STRING), 1000));

      //////////////////////////////////////////////////
      // this one flows through the exceptional cases //
      //////////////////////////////////////////////////
      assertEquals("1000.01", QValueFormatter.formatValue(new QFieldMetaData().withDisplayFormat(DisplayFormat.COMMAS), new BigDecimal("1000.01")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFormatRecordLabel()
   {
      QTableMetaData table = new QTableMetaData().withRecordLabelFormat("%s %s").withRecordLabelFields(List.of("firstName", "lastName"));
      assertEquals("Darin Kelkhoff", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")));
      assertEquals("Darin ", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("firstName", "Darin")));
      assertEquals("Darin ", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("firstName", "Darin").withValue("lastName", null)));

      table = new QTableMetaData().withRecordLabelFormat("%s " + DisplayFormat.CURRENCY).withRecordLabelFields(List.of("firstName", "price"));
      assertEquals("Darin $10,000.00", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("firstName", "Darin").withValue("price", new BigDecimal(10000))));

      table = new QTableMetaData().withRecordLabelFormat(DisplayFormat.DEFAULT).withRecordLabelFields(List.of("id"));
      assertEquals("123456", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("id", "123456")));

      ///////////////////////////////////////////////////////
      // exceptional flow:  no recordLabelFormat specified //
      ///////////////////////////////////////////////////////
      table = new QTableMetaData().withPrimaryKeyField("id");
      assertEquals("42", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("id", 42)));

      /////////////////////////////////////////////////
      // exceptional flow:  no fields for the format //
      /////////////////////////////////////////////////
      table = new QTableMetaData().withRecordLabelFormat("%s %s").withPrimaryKeyField("id");
      assertEquals("128", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("id", 128)));

      /////////////////////////////////////////////////////////
      // exceptional flow:  not enough fields for the format //
      /////////////////////////////////////////////////////////
      table = new QTableMetaData().withRecordLabelFormat("%s %s").withRecordLabelFields(List.of("a")).withPrimaryKeyField("id");
      assertEquals("256", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("a", 47).withValue("id", 256)));

      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      // exceptional flow (kinda):  too many fields for the format (just get the ones that are in the format) //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      table = new QTableMetaData().withRecordLabelFormat("%s %s").withRecordLabelFields(List.of("a", "b", "c")).withPrimaryKeyField("id");
      assertEquals("47 48", QValueFormatter.formatRecordLabel(table, new QRecord().withValue("a", 47).withValue("b", 48).withValue("c", 49).withValue("id", 256)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSetDisplayValuesInRecords()
   {
      QTableMetaData table = new QTableMetaData()
         .withRecordLabelFormat("%s %s")
         .withRecordLabelFields(List.of("firstName", "lastName"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("price", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY))
         .withField(new QFieldMetaData("quantity", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS));

      /////////////////////////////////////////////////////////////////
      // first, make sure it doesn't crash with null or empty inputs //
      /////////////////////////////////////////////////////////////////
      QValueFormatter.setDisplayValuesInRecords(table, null);
      QValueFormatter.setDisplayValuesInRecords(table, Collections.emptyList());

      List<QRecord> records = List.of(
         new QRecord()
            .withValue("firstName", "Tim")
            .withValue("lastName", "Chamberlain")
            .withValue("price", new BigDecimal("3.50"))
            .withValue("quantity", 1701),
         new QRecord()
            .withValue("firstName", "Tyler")
            .withValue("lastName", "Samples")
            .withValue("price", new BigDecimal("174999.99"))
            .withValue("quantity", 47)
      );

      QValueFormatter.setDisplayValuesInRecords(table, records);

      assertEquals("Tim Chamberlain", records.get(0).getRecordLabel());
      assertEquals("$3.50", records.get(0).getDisplayValue("price"));
      assertEquals("1,701", records.get(0).getDisplayValue("quantity"));

      assertEquals("Tyler Samples", records.get(1).getRecordLabel());
      assertEquals("$174,999.99", records.get(1).getDisplayValue("price"));
      assertEquals("47", records.get(1).getDisplayValue("quantity"));
   }

}