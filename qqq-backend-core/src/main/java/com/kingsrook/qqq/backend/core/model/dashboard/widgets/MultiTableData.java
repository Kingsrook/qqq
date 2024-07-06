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
 ** Model containing datastructure expected by frontend bar chart widget
 **
 *******************************************************************************/
public class MultiTableData extends QWidgetData
{
   List<TableData> tableDataList;



   /*******************************************************************************
    **
    *******************************************************************************/
   public MultiTableData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public MultiTableData(List<TableData> tableDataList)
   {
      setTableDataList(tableDataList);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.MULTI_TABLE.getType();
   }



   /*******************************************************************************
    ** Getter for tableDataList
    *******************************************************************************/
   public List<TableData> getTableDataList()
   {
      return (this.tableDataList);
   }



   /*******************************************************************************
    ** Setter for tableDataList
    *******************************************************************************/
   public void setTableDataList(List<TableData> tableDataList)
   {
      this.tableDataList = tableDataList;
   }



   /*******************************************************************************
    ** Fluent setter for tableDataList
    *******************************************************************************/
   public MultiTableData withTableDataList(List<TableData> tableDataList)
   {
      this.tableDataList = tableDataList;
      return (this);
   }

}
