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

package com.kingsrook.qqq.api.wip;


import java.util.Map;
import com.kingsrook.qqq.openapi.model.Schema;
import com.kingsrook.qqq.openapi.model.Type;
import org.assertj.core.api.AbstractStringAssert;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestUtils
{
   /***************************************************************************
    **
    ***************************************************************************/
   private static void assertStringVsSchema(String string, Schema schema, String path)
   {
      String                        description = "At path " + path;
      final AbstractStringAssert<?> assertion   = assertThat(string).describedAs(description);

      Type type = Type.valueOf(schema.getType().toUpperCase());
      switch(type)
      {
         case OBJECT ->
         {
            assertion.startsWith("{");
            JSONObject object = new JSONObject(string);

            for(Map.Entry<String, Schema> entry : schema.getProperties().entrySet())
            {
               // todo deal with optional
               Object subObject = object.get(entry.getKey());
               assertStringVsSchema(subObject.toString(), entry.getValue(), path + "/" + entry.getKey());
            }
         }
         case ARRAY ->
         {
            assertion.startsWith("[");
            JSONArray array = new JSONArray(string);

            for(int i = 0; i < array.length(); i++)
            {
               Object subObject = array.get(i);
               assertStringVsSchema(subObject.toString(), schema.getItems(), path + "[" + i + "]");
            }
         }
         case BOOLEAN ->
         {
            assertion.matches("(true|false)");
         }
         case INTEGER ->
         {
            assertion.matches("-?\\d+");
         }
         case NUMBER ->
         {
            assertion.matches("-?\\d+(\\.\\d+)?");
         }
         case STRING ->
         {
            assertion.matches("\".*\"");
         }
      }
   }

}
