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

package com.kingsrook.qqq.backend.core.adapters;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Class to convert QRecords to CSV Strings.
 *******************************************************************************/
public class QRecordToCsvAdapter
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public String recordToCsv(QTableMetaData table, QRecord record)
   {
      return (recordToCsv(table, record, new ArrayList<>(table.getFields().values())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String recordToCsv(QTableMetaData table, QRecord record, List<QFieldMetaData> fields)
   {
      StringBuilder rs      = new StringBuilder();
      int           fieldNo = 0;

      for(QFieldMetaData field : fields)
      {
         if(fieldNo++ > 0)
         {
            rs.append(',');
         }
         rs.append('"');
         Serializable value         = record.getValue(field.getName());
         String       valueAsString = ValueUtils.getValueAsString(value);
         if(StringUtils.hasContent(valueAsString))
         {
            rs.append(sanitize(valueAsString));
         }
         rs.append('"');
      }
      rs.append('\n');
      return (rs.toString());
   }



   /*******************************************************************************
    ** todo - kinda weak... can we find this in a CSV lib??
    *******************************************************************************/
   static String sanitize(String value)
   {
      /////////////////////////////////////////////////////////////////////////////////////
      // especially in big exports, we see a TON of memory allocated and CPU spent here, //
      // if we just blindly replaceAll.  So, only do it if needed.                       //
      /////////////////////////////////////////////////////////////////////////////////////
      if(value.contains("\""))
      {
         value = value.replaceAll("\"", "\"\"");
      }

      if(value.contains("\n"))
      {
         value = value.replaceAll("\n", " ");
      }

      return (value);
   }

}
