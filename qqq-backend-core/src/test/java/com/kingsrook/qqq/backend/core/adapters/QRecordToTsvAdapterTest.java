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

package com.kingsrook.qqq.backend.core.adapters;


import java.util.List;
import java.util.stream.Stream;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QRecordToTsvAdapter
 *******************************************************************************/
class QRecordToTsvAdapterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRecordToTsv()
   {
      QTableMetaData table  = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON);
      QRecord        record = new QRecord().withValue("firstName", "John").withValue("lastName", "Doe");
      String         tsv    = new QRecordToTsvAdapter().recordToTsv(table, record, Stream.of("id", "firstName", "lastName").map(f -> table.getField(f)).toList());
      assertEquals("\tJohn\tDoe\n", tsv);

      record.setValue("id", 47);
      tsv = new QRecordToTsvAdapter().recordToTsv(table, record, Stream.of("id", "firstName", "lastName").map(f -> table.getField(f)).toList());
      assertEquals("47\tJohn\tDoe\n", tsv);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSanitationTypes()
   {
      QTableMetaData       table     = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON);
      QRecord              record    = new QRecord().withValue("firstName", "H\no\tm\re\\r");
      List<QFieldMetaData> fieldList = List.of(table.getField("firstName"));

      assertEquals("H\no\tm\re\\r\n", new QRecordToTsvAdapter().withSanitizationType(QRecordToTsvAdapter.SanitizationType.NONE).recordToTsv(table, record, fieldList));
      assertEquals("H\\no\\tm\\re\\\\r\n", new QRecordToTsvAdapter().withSanitizationType(QRecordToTsvAdapter.SanitizationType.ESCAPE).recordToTsv(table, record, fieldList));
      assertEquals("H o m e\\r\n", new QRecordToTsvAdapter().withSanitizationType(QRecordToTsvAdapter.SanitizationType.STRIP).recordToTsv(table, record, fieldList));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEscape()
   {
      assertEquals("foo", QRecordToTsvAdapter.escape("foo"));

      assertArrayEquals(new char[] { 'n', 'e', 'w', '\\', 'n', 'l', 'i', 'n', 'e' }, QRecordToTsvAdapter.escape("""
         new
         line""").toCharArray());

      assertArrayEquals(new char[] { 'n', 'e', 'w', '\\', 'n', 'l', 'i', 'n', 'e' }, QRecordToTsvAdapter.escape("""
         new\nline""").toCharArray());

      assertArrayEquals(new char[] { 'i', 'n', 'n', 'e', 'r', '\\', 't', 't', 'a', 'b' }, QRecordToTsvAdapter.escape("""
         inner\ttab""").toCharArray());

      assertArrayEquals(new char[] { 'b', 'a', 'c', 'k', '\\', '\\', 's', 'l', 'a', 's', 'h' }, QRecordToTsvAdapter.escape("""
         back\\slash""").toCharArray());

      assertEquals("""
         Homer "Jay" Simpson""", QRecordToTsvAdapter.escape("""
         Homer "Jay" Simpson"""));

      assertEquals("""
         end "quote" new\\nline""", QRecordToTsvAdapter.escape("""
         end "quote" new
         line"""));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStrip()
   {
      assertEquals("foo", QRecordToTsvAdapter.strip("foo"));

      assertArrayEquals(new char[] { 'n', 'e', 'w', ' ', 'l', 'i', 'n', 'e' }, QRecordToTsvAdapter.strip("""
         new
         line""").toCharArray());

      assertArrayEquals(new char[] { 'n', 'e', 'w', ' ', 'l', 'i', 'n', 'e' }, QRecordToTsvAdapter.strip("""
         new\nline""").toCharArray());

      assertArrayEquals(new char[] { 'i', 'n', 'n', 'e', 'r', ' ', 't', 'a', 'b' }, QRecordToTsvAdapter.strip("""
         inner\ttab""").toCharArray());

      assertArrayEquals(new char[] { 'b', 'a', 'c', 'k', '\\', 's', 'l', 'a', 's', 'h' }, QRecordToTsvAdapter.strip("""
         back\\slash""").toCharArray());

      assertEquals("""
         Homer "Jay" Simpson""", QRecordToTsvAdapter.strip("""
         Homer "Jay" Simpson"""));

      assertEquals("""
         end "quote" new line""", QRecordToTsvAdapter.strip("""
         end "quote" new
         line"""));
   }

}