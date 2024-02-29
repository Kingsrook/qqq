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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;


/*******************************************************************************
 ** Meta Data Producer for SampleWidgetsDashboard
 *******************************************************************************/
public class SampleWidgetsDashboardMetaDataProducer extends MetaDataProducer<QAppMetaData>
{
   public static final String NAME = "SampleWidgetsDashboard";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QAppMetaData produce(QInstance qInstance) throws QException
   {
      // Divider
      // Parent
      // Process...

      // USMap ??
      // QuickSightChart ??
      // ChildRecordList
      // FieldValueList

      ////////////////////////////////////
      // in java enum, but not frontend //
      ////////////////////////////////////
      // HORIZONTAL_BAR_CHART("horizontalBarChart"),
      // LOCATION("location"),

      return (new QAppMetaData()
         .withName(NAME)
         .withIcon(new QIcon("widgets"))
         .withWidgets(List.of(
            SampleBigNumberBlocksWidgetMetaDataProducer.NAME,
            SampleMultiStatisticsWidgetMetaDataProducer.NAME,
            SamplePieChartWidgetMetaDataProducer.NAME,
            SampleStatisticsWidgetMetaDataProducer.NAME,
            SampleTableWidgetMetaDataProducer.NAME,
            SampleStackedBarChartWidgetMetaDataProducer.NAME,
            SampleStepperWidgetMetaDataProducer.NAME,
            SampleHTMLWidgetMetaDataProducer.NAME,
            SampleSmallLineChartWidgetMetaDataProducer.NAME,
            SampleLineChartWidgetMetaDataProducer.NAME,
            SampleBarChartWidgetMetaDataProducer.NAME
         )));

   }

}
