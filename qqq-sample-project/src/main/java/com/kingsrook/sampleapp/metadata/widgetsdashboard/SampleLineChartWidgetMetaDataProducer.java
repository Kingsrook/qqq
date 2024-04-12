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


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChartData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;


/*******************************************************************************
 ** Meta Data Producer for SampleLineChartWidget
 *******************************************************************************/
public class SampleLineChartWidgetMetaDataProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "SampleLineChartWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      return new QWidgetMetaData()
         .withName(NAME)
         .withType(WidgetType.LINE_CHART.getType())
         .withGridColumns(4)
         .withIsCard(true)
         .withLabel("Line Chart")
         .withTooltip("This is a sample of a  Line Chart widget")
         .withShowReloadButton(false)
         .withCodeReference(new QCodeReference(SampleLineChartWidgetRenderer.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SampleLineChartWidgetRenderer extends AbstractWidgetRenderer
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public RenderWidgetOutput render(RenderWidgetInput input) throws QException
      {
         List<String> labels = List.of("January", "February", "March", "April", "May");
         List<Number> data   = List.of(1753, 1830, 920, 1543, 1804);

         String description = "Total units have been <strong>increasing</strong> over the last five months.";
         return (new RenderWidgetOutput(new ChartData("Line Chart", description, "Units", labels, data)));
      }
   }

}
