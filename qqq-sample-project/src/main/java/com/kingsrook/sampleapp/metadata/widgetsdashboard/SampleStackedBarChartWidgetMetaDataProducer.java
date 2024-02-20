/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.sampleapp.metadata.widgetsdashboard;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChartData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChartSubheaderData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.MaterialDashboardIconRoleNames;


/*******************************************************************************
 ** Meta Data Producer for SamplePieChart
 *******************************************************************************/
public class SampleStackedBarChartWidgetMetaDataProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "SampleStackedBarChartWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      return new QWidgetMetaData()
         .withName(NAME)
         .withType(WidgetType.STACKED_BAR_CHART.getType())
         .withGridColumns(4)
         .withIsCard(true)
         .withLabel("Stacked Bar Chart")
         .withTooltip("This is a sample of a stacked bar chart")
         .withShowReloadButton(true)
         .withIcon(MaterialDashboardIconRoleNames.TOP_RIGHT_INSIDE_CARD, new QIcon("new_releases").withColor("#6BA47D"))
         .withCodeReference(new QCodeReference(SampleStackedBarChartRenderer.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SampleStackedBarChartRenderer extends AbstractWidgetRenderer
   {
      private List<String> labels = new ArrayList<>();
      private List<String> colors = new ArrayList<>();
      private List<Number> data   = new ArrayList<>();
      private List<String> urls   = new ArrayList<>();



      /*******************************************************************************
       **
       *******************************************************************************/
      private void addSlice(String label, String color, Number datum, String url)
      {
         labels.add(label);
         colors.add(color);
         data.add(datum);
         urls.add(url);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public RenderWidgetOutput render(RenderWidgetInput input) throws QException
      {
         addSlice("Apple", "#FF0000", 100, null);
         addSlice("Orange", "#FF8000", 150, null);
         addSlice("Banana", "#FFFF00", 75, null);
         addSlice("Lime", "#00FF00", 100, null);
         addSlice("Blueberry", "#0000FF", 200, null);

         ChartData.Data.Dataset one = new ChartData.Data.Dataset()
            .withLabel("One")
            .withData(data)
            .withBackgroundColors(colors)
            .withUrls(urls);

         labels = new ArrayList<>();
         colors = new ArrayList<>();
         data = new ArrayList<>();
         urls = new ArrayList<>();

         addSlice("Apple", "#FF0000", 50, null);
         addSlice("Orange", "#FF8000", 100, null);
         addSlice("Banana", "#FFFF00", 75, null);
         addSlice("Lime", "#00FF00", 150, null);
         addSlice("Blueberry", "#0000FF", 75, null);

         ChartData.Data.Dataset two = new ChartData.Data.Dataset()
            .withLabel("Two")
            .withData(data)
            .withBackgroundColors(colors)
            .withUrls(urls);

         ChartData chartData = new ChartData()
            .withChartData(new ChartData.Data()
               .withLabels(labels)
               .withDatasets(List.of(one, two)));

         ChartSubheaderData chartSubheaderData = new ChartSubheaderData()
            .withMainNumber(1000)
            .withVsPreviousNumber(100);
         // .withMainNumberUrl(mainUrl)
         // .withPreviousNumberUrl(previousUrl);

         chartSubheaderData.calculatePercentsEtc(true);

         chartData.setChartSubheaderData(chartSubheaderData);

         return (new RenderWidgetOutput(chartData));
      }
   }

}
