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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;


/*******************************************************************************
 ** Input data for the Count action
 **
 *******************************************************************************/
public class AggregateInput extends AbstractTableActionInput
{
   private QQueryFilter    filter;
   private List<Aggregate> aggregates;
   private List<GroupBy>   groupBys = new ArrayList<>();
   private Integer         limit;

   private List<QueryJoin> queryJoins = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   public AggregateInput()
   {
   }



   /*******************************************************************************
    ** Getter for filter
    **
    *******************************************************************************/
   public QQueryFilter getFilter()
   {
      return filter;
   }



   /*******************************************************************************
    ** Setter for filter
    **
    *******************************************************************************/
   public void setFilter(QQueryFilter filter)
   {
      this.filter = filter;
   }



   /*******************************************************************************
    ** Fluent setter for filter
    **
    *******************************************************************************/
   public AggregateInput withFilter(QQueryFilter filter)
   {
      setFilter(filter);
      return (this);
   }



   /*******************************************************************************
    ** Getter for aggregates
    **
    *******************************************************************************/
   public List<Aggregate> getAggregates()
   {
      return aggregates;
   }



   /*******************************************************************************
    ** Setter for aggregates
    **
    *******************************************************************************/
   public void setAggregates(List<Aggregate> aggregates)
   {
      this.aggregates = aggregates;
   }



   /*******************************************************************************
    ** Fluent setter for aggregates
    **
    *******************************************************************************/
   public AggregateInput withAggregates(List<Aggregate> aggregates)
   {
      this.aggregates = aggregates;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for aggregates
    **
    *******************************************************************************/
   public AggregateInput withAggregate(Aggregate aggregate)
   {
      if(this.aggregates == null)
      {
         this.aggregates = new ArrayList<>();
      }
      this.aggregates.add(aggregate);
      return (this);
   }



   /*******************************************************************************
    ** Getter for groupBys
    **
    *******************************************************************************/
   public List<GroupBy> getGroupBys()
   {
      return groupBys;
   }



   /*******************************************************************************
    ** Setter for groupBys
    **
    *******************************************************************************/
   public void setGroupBys(List<GroupBy> groupBys)
   {
      this.groupBys = groupBys;
   }



   /*******************************************************************************
    ** Fluent setter for groupBys
    **
    *******************************************************************************/
   public AggregateInput withGroupBys(List<GroupBy> groupBys)
   {
      this.groupBys = groupBys;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for groupBys
    **
    *******************************************************************************/
   public AggregateInput withGroupBy(GroupBy groupBy)
   {
      if(this.groupBys == null)
      {
         this.groupBys = new ArrayList<>();
      }
      this.groupBys.add(groupBy);
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryJoins
    **
    *******************************************************************************/
   public List<QueryJoin> getQueryJoins()
   {
      return queryJoins;
   }



   /*******************************************************************************
    ** Setter for queryJoins
    **
    *******************************************************************************/
   public void setQueryJoins(List<QueryJoin> queryJoins)
   {
      this.queryJoins = queryJoins;
   }



   /*******************************************************************************
    ** Fluent setter for queryJoins
    **
    *******************************************************************************/
   public AggregateInput withQueryJoins(List<QueryJoin> queryJoins)
   {
      this.queryJoins = queryJoins;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for queryJoins
    **
    *******************************************************************************/
   public AggregateInput withQueryJoin(QueryJoin queryJoin)
   {
      if(this.queryJoins == null)
      {
         this.queryJoins = new ArrayList<>();
      }
      this.queryJoins.add(queryJoin);
      return (this);
   }



   /*******************************************************************************
    ** Getter for limit
    **
    *******************************************************************************/
   public Integer getLimit()
   {
      return limit;
   }



   /*******************************************************************************
    ** Setter for limit
    **
    *******************************************************************************/
   public void setLimit(Integer limit)
   {
      this.limit = limit;
   }



   /*******************************************************************************
    ** Fluent setter for limit
    **
    *******************************************************************************/
   public AggregateInput withLimit(Integer limit)
   {
      this.limit = limit;
      return (this);
   }

}
