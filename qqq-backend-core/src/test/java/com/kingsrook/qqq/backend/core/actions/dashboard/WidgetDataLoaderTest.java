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

package com.kingsrook.qqq.backend.core.actions.dashboard;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChartData;
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
      assertThat(widgetData).isInstanceOf(ChartData.class);
      ChartData chartData = (ChartData) widgetData;
      assertEquals("chartData", chartData.getType());
      assertThat(chartData.getTitle()).isNotBlank();
      assertNotNull(chartData.getChartData());
   }

}
