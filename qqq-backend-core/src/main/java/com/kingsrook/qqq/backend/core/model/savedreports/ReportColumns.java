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

package com.kingsrook.qqq.backend.core.model.savedreports;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** type of object expected to be in the SavedReport columnsJSON field
 *******************************************************************************/
public class ReportColumns implements Serializable
{
   private List<ReportColumn> columns;



   /*******************************************************************************
    ** Getter for columns
    *******************************************************************************/
   public List<ReportColumn> getColumns()
   {
      return (this.columns);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<ReportColumn> extractVisibleColumns()
   {
      return CollectionUtils.nonNullList(getColumns()).stream()
         //////////////////////////////////////////////////////
         // if isVisible is missing, we assume it to be true //
         //////////////////////////////////////////////////////
         .filter(rc -> rc.getIsVisible() == null || rc.getIsVisible())
         .filter(rc -> StringUtils.hasContent(rc.getName()))
         .filter(rc -> !rc.getName().startsWith("__check"))
         .toList();
   }



   /*******************************************************************************
    ** Setter for columns
    *******************************************************************************/
   public void setColumns(List<ReportColumn> columns)
   {
      this.columns = columns;
   }



   /*******************************************************************************
    ** Fluent setter for columns
    *******************************************************************************/
   public ReportColumns withColumns(List<ReportColumn> columns)
   {
      this.columns = columns;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter to add 1 column
    *******************************************************************************/
   public ReportColumns withColumn(ReportColumn column)
   {
      if(this.columns == null)
      {
         this.columns = new ArrayList<>();
      }
      this.columns.add(column);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter to add 1 column w/ just a name
    *******************************************************************************/
   public ReportColumns withColumn(String name)
   {
      if(this.columns == null)
      {
         this.columns = new ArrayList<>();
      }
      this.columns.add(new ReportColumn().withName(name));
      return (this);
   }

}
