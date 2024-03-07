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


/*******************************************************************************
 ** Possible values for widget type
 *******************************************************************************/
public enum WidgetType
{
   BAR_CHART("barChart"),
   CHART("chart"),
   CHILD_RECORD_LIST("childRecordList"),
   DIVIDER("divider"),
   FIELD_VALUE_LIST("fieldValueList"),
   GENERIC("generic"),
   HORIZONTAL_BAR_CHART("horizontalBarChart"),
   HTML("html"),
   LINE_CHART("lineChart"),
   SMALL_LINE_CHART("smallLineChart"),
   LOCATION("location"),
   MULTI_STATISTICS("multiStatistics"),
   PARENT_WIDGET("parentWidget"),
   PIE_CHART("pieChart"),
   PROCESS("process"),
   QUICK_SIGHT_CHART("quickSightChart"),
   STATISTICS("statistics"),
   STACKED_BAR_CHART("stackedBarChart"),
   STEPPER("stepper"),
   TABLE("table"),
   USA_MAP("usaMap"),
   COMPOSITE("composite"),
   DATA_BAG_VIEWER("dataBagViewer"),
   SCRIPT_VIEWER("scriptViewer");


   private final String type;



   /*******************************************************************************
    **
    *******************************************************************************/
   WidgetType(String type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return type;
   }

}
