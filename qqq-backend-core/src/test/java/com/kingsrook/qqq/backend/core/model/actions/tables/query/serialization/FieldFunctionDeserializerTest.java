/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.actions.tables.query.serialization;


import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.SubStringFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.WeekdayOfDateFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit tests for {@link FieldFunctionDeserializer}
 *******************************************************************************/
class FieldFunctionDeserializerTest extends BaseTest
{
   private final ObjectMapper objectMapper = new ObjectMapper();



   /***************************************************************************
    ** Serialize a FieldFunction to JSON and deserialize it back.
    ** Verifies the @JsonProperty("functionTypeIdentifierName") and
    ** @JsonIgnore on getFunctionType() work correctly for round-tripping.
    ***************************************************************************/
   @Test
   void testRoundTrip() throws Exception
   {
      FieldFunction original = new FieldFunction()
         .withFunctionTypeIdentifier(WeekdayOfDateFunction.IDENTIFIER)
         .withFieldName("birthDate");

      String        json         = objectMapper.writeValueAsString(original);
      FieldFunction deserialized = objectMapper.readValue(json, FieldFunction.class);

      assertNotNull(deserialized);
      assertEquals("birthDate", deserialized.getFieldName());
      assertNotNull(deserialized.getFunctionTypeIdentifier());
      assertEquals("WeekdayOfDate", deserialized.getFunctionTypeIdentifier().getName());
   }



   /***************************************************************************
    ** Verify arguments survive round-trip
    ***************************************************************************/
   @Test
   void testRoundTripWithArguments() throws Exception
   {
      FieldFunction original = new FieldFunction()
         .withFunctionTypeIdentifier(SubStringFunction.IDENTIFIER)
         .withFieldName("firstName")
         .withArguments(Map.of(SubStringFunction.FROM_INDEX_PARAM, 2, SubStringFunction.LENGTH_PARAM, 3));

      String        json         = objectMapper.writeValueAsString(original);
      FieldFunction deserialized = objectMapper.readValue(json, FieldFunction.class);

      assertNotNull(deserialized);
      assertEquals("firstName", deserialized.getFieldName());
      assertEquals("SubString", deserialized.getFunctionTypeIdentifier().getName());
      assertNotNull(deserialized.getArguments());
      assertEquals(2, deserialized.getArguments().size());
   }



   /***************************************************************************
    ** Unknown function type identifier should throw IOException
    ***************************************************************************/
   @Test
   void testDeserializeUnknownFunctionType()
   {
      String json = """
         {"fieldName":"test","functionTypeIdentifierName":"BogusFunction","arguments":null}
         """;

      assertThrows(IOException.class, () -> objectMapper.readValue(json, FieldFunction.class));
   }



   /***************************************************************************
    ** Null arguments should not cause an error
    ***************************************************************************/
   @Test
   void testDeserializeWithNullArguments() throws Exception
   {
      String json = """
         {"fieldName":"birthDate","functionTypeIdentifierName":"WeekdayOfDate","arguments":null}
         """;

      FieldFunction deserialized = objectMapper.readValue(json, FieldFunction.class);
      assertNotNull(deserialized);
      assertEquals("birthDate", deserialized.getFieldName());
      assertEquals("WeekdayOfDate", deserialized.getFunctionTypeIdentifier().getName());
      assertNull(deserialized.getArguments());
   }

}
