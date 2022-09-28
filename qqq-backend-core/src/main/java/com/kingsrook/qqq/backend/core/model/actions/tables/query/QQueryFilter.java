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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 * Full "filter" for a query - a list of criteria and order-bys
 *
 *******************************************************************************/
public class QQueryFilter implements Serializable, Cloneable
{
   private List<QFilterCriteria> criteria = new ArrayList<>();
   private List<QFilterOrderBy>  orderBys = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QQueryFilter clone()
   {
      try
      {
         QQueryFilter clone = (QQueryFilter) super.clone();

         if(criteria != null)
         {
            clone.criteria = new ArrayList<>();
            for(QFilterCriteria criterion : criteria)
            {
               clone.criteria.add(criterion.clone());
            }
         }

         if(orderBys != null)
         {
            clone.orderBys = new ArrayList<>();
            for(QFilterOrderBy orderBy : orderBys)
            {
               clone.orderBys.add(orderBy.clone());
            }
         }

         return clone;
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }



   /*******************************************************************************
    ** Getter for criteria
    **
    *******************************************************************************/
   public List<QFilterCriteria> getCriteria()
   {
      return criteria;
   }



   /*******************************************************************************
    ** Setter for criteria
    **
    *******************************************************************************/
   public void setCriteria(List<QFilterCriteria> criteria)
   {
      this.criteria = criteria;
   }



   /*******************************************************************************
    ** Getter for order
    **
    *******************************************************************************/
   public List<QFilterOrderBy> getOrderBys()
   {
      return orderBys;
   }



   /*******************************************************************************
    ** Setter for order
    **
    *******************************************************************************/
   public void setOrderBys(List<QFilterOrderBy> orderBys)
   {
      this.orderBys = orderBys;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addCriteria(QFilterCriteria qFilterCriteria)
   {
      if(criteria == null)
      {
         criteria = new ArrayList<>();
      }
      criteria.add(qFilterCriteria);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QQueryFilter withCriteria(QFilterCriteria qFilterCriteria)
   {
      addCriteria(qFilterCriteria);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addOrderBy(QFilterOrderBy qFilterOrderBy)
   {
      if(orderBys == null)
      {
         orderBys = new ArrayList<>();
      }
      orderBys.add(qFilterOrderBy);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QQueryFilter withOrderBy(QFilterOrderBy qFilterOrderBy)
   {
      addOrderBy(qFilterOrderBy);
      return (this);
   }

}
