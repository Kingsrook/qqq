package com.kingsrook.qqq.backend.core.modules.mock;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.QueryResult;
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
            record.setValue(field, "1");
         }

         return rs;
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new QException("Error executing query", e);
      }
   }

}
