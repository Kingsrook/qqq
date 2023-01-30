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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets;


import java.util.List;


/*******************************************************************************
 ** Model containing datastructure expected by frontend pie chart widget
 **
 *******************************************************************************/
public class PieChartData extends ChartData
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public PieChartData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public PieChartData(String description, String seriesLabel, List<String> labels, List<Number> data)
   {
      setDescription(description);
      setChartData(new ChartData.Data()
         .withLabels(labels)
         .withDatasets(List.of(
            new ChartData.Data.Dataset()
               .withLabel(seriesLabel)
               .withData(data)
         ))
      );

   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.PIE_CHART.getType();
   }
}
