package com.kingsrook.sampleapp.dashboard.widgets;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.BarChart;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.sampleapp.SampleMetaDataProvider;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for PersonsByCreateDateBarChart
 *******************************************************************************/
class PersonsByCreateDateBarChartTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      Object widgetData = new PersonsByCreateDateBarChart().render(SampleMetaDataProvider.defineInstance(), new QSession());
      assertThat(widgetData).isInstanceOf(BarChart.class);
      BarChart barChart = (BarChart) widgetData;
      assertEquals("barChart", barChart.getType());
      assertThat(barChart.getTitle()).isNotBlank();
      assertNotNull(barChart.getBarChartData());
   }

}