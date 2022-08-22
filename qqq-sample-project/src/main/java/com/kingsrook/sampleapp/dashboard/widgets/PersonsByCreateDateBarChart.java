package com.kingsrook.sampleapp.dashboard.widgets;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.dashboard.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.BarChart;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.sampleapp.SampleMetaDataProvider;


/*******************************************************************************
 **
 *******************************************************************************/
public class PersonsByCreateDateBarChart extends AbstractWidgetRenderer
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Object render(QInstance qInstance, QSession session) throws QException
   {
      try
      {
         ConnectionManager connectionManager = new ConnectionManager();
         Connection        connection        = connectionManager.getConnection(SampleMetaDataProvider.defineRdbmsBackend());

         String sql = """
            SELECT
               COUNT(*) AS count,
               DATE_FORMAT(create_date, '%m-%Y') AS month
            FROM
               person
            GROUP BY
               2
            ORDER BY
               2
            """;

         List<Map<String, Object>> rows = QueryManager.executeStatementForRows(connection, sql);

         List<String> labels = new ArrayList<>();
         List<Number> data   = new ArrayList<>();

         for(Map<String, Object> row : rows)
         {
            labels.add(ValueUtils.getValueAsString(row.get("month")));
            data.add(ValueUtils.getValueAsInteger(row.get("count")));
         }

         return (new BarChart("Persons created per Month", "Person records", labels, data));
      }
      catch(Exception e)
      {
         throw (new QException("Error rendering widget", e));
      }
   }

}
