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

package com.kingsrook.qqq.backend.core.model.actions.tables.aggregate;


import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;


/*******************************************************************************
 ** Bean representing an element of a query order-by clause - ordering by a
 ** group by
 **
 *******************************************************************************/
public class QFilterOrderByGroupBy extends QFilterOrderBy implements Cloneable
{
   private GroupBy groupBy;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QFilterOrderByGroupBy clone()
   {
      return (QFilterOrderByGroupBy) super.clone();
   }



   /*******************************************************************************
    ** Default no-arg constructor
    *******************************************************************************/
   public QFilterOrderByGroupBy()
   {

   }



   /*******************************************************************************
    ** Constructor that sets groupBy, but leaves default for isAscending (true)
    *******************************************************************************/
   public QFilterOrderByGroupBy(GroupBy groupBy)
   {
      this.groupBy = groupBy;
   }



   /*******************************************************************************
    ** Constructor that takes groupBy and isAscending.
    *******************************************************************************/
   public QFilterOrderByGroupBy(GroupBy groupBy, boolean isAscending)
   {
      this.groupBy = groupBy;
      setIsAscending(isAscending);
   }



   /*******************************************************************************
    ** Getter for groupBy
    **
    *******************************************************************************/
   public GroupBy getGroupBy()
   {
      return groupBy;
   }



   /*******************************************************************************
    ** Setter for groupBy
    **
    *******************************************************************************/
   public void setGroupBy(GroupBy groupBy)
   {
      this.groupBy = groupBy;
   }



   /*******************************************************************************
    ** Fluent setter for groupBy
    **
    *******************************************************************************/
   public QFilterOrderByGroupBy withGroupBy(GroupBy groupBy)
   {
      this.groupBy = groupBy;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return (groupBy + " " + (getIsAscending() ? "ASC" : "DESC"));
   }
}
