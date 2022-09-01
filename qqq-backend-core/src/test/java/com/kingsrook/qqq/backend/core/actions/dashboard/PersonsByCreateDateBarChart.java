package com.kingsrook.qqq.backend.core.actions.dashboard;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.BarChart;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Sample bar chart widget
 *******************************************************************************/
public class PersonsByCreateDateBarChart extends AbstractWidgetRenderer
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Object render(QInstance qInstance, QSession session, QWidgetMetaDataInterface metaData) throws QException
   {
      try
      {
         List<String> labels = new ArrayList<>();
         List<Number> data = new ArrayList<>();

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
