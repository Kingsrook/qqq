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
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;


/*******************************************************************************
 ** Meta Data Producer for SampleStatisticsWidget
 *******************************************************************************/
public class SampleTableWidgetMetaDataProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "SampleTableWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      return new QWidgetMetaData()
         .withName(NAME)
         .withType(WidgetType.TABLE.getType())
         .withGridColumns(4)
         .withIsCard(true)
         .withLabel("Table")
         .withTooltip("This is a sample of a table widget")
         .withShowReloadButton(false)
         .withCodeReference(new QCodeReference(SampleTableWidgetRenderer.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SampleTableWidgetRenderer extends AbstractWidgetRenderer
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public RenderWidgetOutput render(RenderWidgetInput input) throws QException
      {
         ////////////////////////////////////
         // setup datastructures for table //
         ////////////////////////////////////
         List<Map<String, Object>> tableRows = new ArrayList<>();
         List<TableData.Column> columns = List.of(
            new TableData.Column("html", "Name", "name", "2fr", null),
            new TableData.Column("html", "Age", "age", "1fr", "right"),
            new TableData.Column("html", "Hometown", "hometown", "3fr", null)
         );

         TableData tableData = new TableData(null, columns, tableRows)
            .withRowsPerPage(100)
            .withFixedStickyLastRow(true)
            .withHidePaginationDropdown(true);

         tableRows.add(MapBuilder.of(
            "name", "Darin",
            "age", "43",
            "hometown", "Chesterfield, MO"
         ));

         tableRows.add(MapBuilder.of(
            "name", "James",
            "age", "43",
            "hometown", "Chester, IL"
         ));

         tableRows.add(MapBuilder.of(
            "name", "Tim",
            "age", "47",
            "hometown", "Maryville, IL"
         ));

         ///////////////////////////////////////////////////
         // totals row - just the last row in our table!! //
         ///////////////////////////////////////////////////
         tableRows.add(MapBuilder.of(
            "name", "Total",
            "age", "43",
            "hometown", "U.S.A."
         ));

         return (new RenderWidgetOutput(tableData));
      }
   }

}
