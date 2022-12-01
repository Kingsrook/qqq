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
 ** Bean representing an element of a query order-by clause - ordering by an
 ** aggregate field.
 **
 *******************************************************************************/
public class QFilterOrderByAggregate extends QFilterOrderBy implements Cloneable
{
   private Aggregate aggregate;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QFilterOrderByAggregate clone()
   {
      return (QFilterOrderByAggregate) super.clone();
   }



   /*******************************************************************************
    ** Default no-arg constructor
    *******************************************************************************/
   public QFilterOrderByAggregate()
   {

   }



   /*******************************************************************************
    ** Constructor that sets field name, but leaves default for isAscending (true)
    *******************************************************************************/
   public QFilterOrderByAggregate(Aggregate aggregate)
   {
      this.aggregate = aggregate;
   }



   /*******************************************************************************
    ** Constructor that takes field name and isAscending.
    *******************************************************************************/
   public QFilterOrderByAggregate(Aggregate aggregate, boolean isAscending)
   {
      this.aggregate = aggregate;
      setIsAscending(isAscending);
   }



   /*******************************************************************************
    ** Getter for aggregate
    **
    *******************************************************************************/
   public Aggregate getAggregate()
   {
      return aggregate;
   }



   /*******************************************************************************
    ** Setter for aggregate
    **
    *******************************************************************************/
   public void setAggregate(Aggregate aggregate)
   {
      this.aggregate = aggregate;
   }



   /*******************************************************************************
    ** Fluent setter for aggregate
    **
    *******************************************************************************/
   public QFilterOrderByAggregate withAggregate(Aggregate aggregate)
   {
      this.aggregate = aggregate;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return (aggregate + " " + (getIsAscending() ? "ASC" : "DESC"));
   }
}
