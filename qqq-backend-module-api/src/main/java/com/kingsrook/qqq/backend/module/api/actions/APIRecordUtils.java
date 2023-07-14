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

package com.kingsrook.qqq.backend.module.api.actions;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class APIRecordUtils
{
   private static final QLogger LOG = QLogger.getLogger(APIRecordUtils.class);



   /*******************************************************************************
    ** Take a QRecord whose field names are formatted in JSONQuery-style
    ** (e.g., 'key' or 'key.subKey' or 'key[index].subKey')
    ** and convert it to a JSONObject.
    *******************************************************************************/
   public static JSONObject jsonQueryStyleQRecordToJSONObject(QTableMetaData table, QRecord record, boolean includeNonTableFields)
   {
      try
      {
         JSONObject body = new JSONObject();
         for(Map.Entry<String, Serializable> entry : record.getValues().entrySet())
         {
            String       fieldName = entry.getKey();
            Serializable value     = entry.getValue();

            if(fieldName.contains("."))
            {
               JSONObject tmp   = body;
               String[]   parts = fieldName.split("\\.");
               for(int i = 0; i < parts.length - 1; i++)
               {
                  String thisPart = parts[i];
                  if(thisPart.contains("["))
                  {
                     String arrayName = thisPart.replaceFirst("\\[.*", "");
                     if(!tmp.has(arrayName))
                     {
                        tmp.put(arrayName, new JSONArray());
                     }

                     JSONArray array      = tmp.getJSONArray(arrayName);
                     Integer   arrayIndex = Integer.parseInt(thisPart.replaceFirst(".*\\[", "").replaceFirst("].*", ""));
                     if(array.opt(arrayIndex) == null)
                     {
                        array.put(arrayIndex, new JSONObject());
                     }
                     tmp = array.getJSONObject(arrayIndex);
                  }
                  else
                  {
                     if(!tmp.has(thisPart))
                     {
                        tmp.put(thisPart, new JSONObject());
                     }
                     tmp = tmp.getJSONObject(thisPart);
                  }
               }
               tmp.put(parts[parts.length - 1], value);
            }
            else
            {
               try
               {
                  QFieldMetaData field = table.getField(fieldName);
                  body.put(getFieldBackendName(field), value);
               }
               catch(Exception e)
               {
                  if(includeNonTableFields)
                  {
                     LOG.debug("Putting non-table-field in record", logPair("name", fieldName));
                     body.put(fieldName, value);
                  }
               }
            }
         }
         return body;
      }
      catch(Exception e)
      {
         throw (new QRuntimeException("Error converting record to JSON Object", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String getFieldBackendName(QFieldMetaData field)
   {
      String backendName = field.getBackendName();
      if(!StringUtils.hasContent(backendName))
      {
         backendName = field.getName();
      }
      return (backendName);
   }

}
