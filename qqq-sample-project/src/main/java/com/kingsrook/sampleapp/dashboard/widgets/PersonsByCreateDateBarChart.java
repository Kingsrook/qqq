package com.kingsrook.sampleapp.dashboard.widgets;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.dashboard.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.BarChart;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;


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
         /*
         // todo - always do this as SQL... if we had database in CI...
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

         for(Map<String, Object> row : rows)
         {
            labels.add(ValueUtils.getValueAsString(row.get("month")));
            data.add(ValueUtils.getValueAsInteger(row.get("count")));
         }
          */

         List<String> labels = new ArrayList<>();
         List<Number> data   = new ArrayList<>();

         labels.add("Jan. 2022");
         data.add(17);

         labels.add("Feb. 2022");
         data.add(42);

         labels.add("Mar. 2022");
         data.add(47);

         labels.add("Apr. 2022");
         data.add(0);

         labels.add("May 2022");
         data.add(64);

         return (new BarChart("Persons created per Month", "Person records", labels, data));
      }
      catch(Exception e)
      {
         throw (new QException("Error rendering widget", e));
      }
   }

}
