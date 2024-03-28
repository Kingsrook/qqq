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

package com.kingsrook.qqq.backend.core.actions.reporting.pivottable;


/*******************************************************************************
 ** Either a row or column grouping in a pivot table.  e.g., a field plus
 ** sorting details, plus showTotals boolean.
 *******************************************************************************/
public class PivotTableGroupBy
{
   private String            fieldName;
   private PivotTableOrderBy orderBy;
   private boolean           showTotals;



   /*******************************************************************************
    ** Getter for fieldName
    *******************************************************************************/
   public String getFieldName()
   {
      return (this.fieldName);
   }



   /*******************************************************************************
    ** Setter for fieldName
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fieldName
    *******************************************************************************/
   public PivotTableGroupBy withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for orderBy
    *******************************************************************************/
   public PivotTableOrderBy getOrderBy()
   {
      return (this.orderBy);
   }



   /*******************************************************************************
    ** Setter for orderBy
    *******************************************************************************/
   public void setOrderBy(PivotTableOrderBy orderBy)
   {
      this.orderBy = orderBy;
   }



   /*******************************************************************************
    ** Fluent setter for orderBy
    *******************************************************************************/
   public PivotTableGroupBy withOrderBy(PivotTableOrderBy orderBy)
   {
      this.orderBy = orderBy;
      return (this);
   }



   /*******************************************************************************
    ** Getter for showTotals
    *******************************************************************************/
   public boolean getShowTotals()
   {
      return (this.showTotals);
   }



   /*******************************************************************************
    ** Setter for showTotals
    *******************************************************************************/
   public void setShowTotals(boolean showTotals)
   {
      this.showTotals = showTotals;
   }



   /*******************************************************************************
    ** Fluent setter for showTotals
    *******************************************************************************/
   public PivotTableGroupBy withShowTotals(boolean showTotals)
   {
      this.showTotals = showTotals;
      return (this);
   }

}
