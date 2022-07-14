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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.mock;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Mocked up version of query action.
 **
 *******************************************************************************/
public class MockQueryAction implements QueryInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutput execute(QueryInput queryInput) throws QException
   {
      try
      {
         QTableMetaData table = queryInput.getTable();

         QueryOutput   rs      = new QueryOutput();
         List<QRecord> records = new ArrayList<>();
         rs.setRecords(records);

         QRecord record = new QRecord();
         records.add(record);
         record.setTableName(table.getName());

         for(String field : table.getFields().keySet())
         {
            Serializable value = getValue(table, field);
            record.setValue(field, value);
         }

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing query", e);
      }
   }



   /*******************************************************************************
    ** Get a mock value to use, based on its type.
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:MagicNumber")
   private Serializable getValue(QTableMetaData table, String field)
   {
      // @formatter:off // IJ can't do new-style switch correctly yet...
      return switch(table.getField(field).getType())
      {
         case STRING -> "Foo";
         case INTEGER -> 42;
         case DECIMAL -> new BigDecimal("3.14159");
         case DATE -> LocalDate.of(1970, Month.JANUARY, 1);
         case DATE_TIME -> LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0);
         case TEXT -> "Four score and seven years ago...";
         case HTML -> "<b>BOLD</b>";
         case PASSWORD -> "abc***234";
         default -> throw new IllegalStateException("Unexpected value: " + table.getField(field).getType());
      };
      // @formatter:on
   }

}
