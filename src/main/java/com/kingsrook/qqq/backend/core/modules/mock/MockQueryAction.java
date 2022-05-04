/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.mock;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QueryInterface;


/*******************************************************************************
 ** Mocked up version of query action.
 **
 *******************************************************************************/
public class MockQueryAction implements QueryInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryResult execute(QueryRequest queryRequest) throws QException
   {
      try
      {
         QTableMetaData table = queryRequest.getTable();

         QueryResult rs = new QueryResult();
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
         e.printStackTrace();
         throw new QException("Error executing query", e);
      }
   }



   /*******************************************************************************
    ** Get a mock value to use, based on its type.
    **
    *******************************************************************************/
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
