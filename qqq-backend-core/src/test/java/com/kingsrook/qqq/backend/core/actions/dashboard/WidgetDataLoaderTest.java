package com.kingsrook.qqq.backend.core.actions.dashboard;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.BarChart;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for WidgetDataLoader
 *******************************************************************************/
class WidgetDataLoaderTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      Object widgetData = new WidgetDataLoader().execute(TestUtils.defineInstance(), TestUtils.getMockSession(), PersonsByCreateDateBarChart.class.getSimpleName());
      assertThat(widgetData).isInstanceOf(BarChart.class);
      BarChart barChart = (BarChart) widgetData;
      assertEquals("barChart", barChart.getType());
      assertThat(barChart.getTitle()).isNotBlank();
      assertNotNull(barChart.getBarChartData());
   }

}