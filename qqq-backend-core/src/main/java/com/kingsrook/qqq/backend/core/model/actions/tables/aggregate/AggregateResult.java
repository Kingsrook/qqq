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


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class AggregateResult
{
   private Map<Aggregate, Serializable> aggregateValues = new LinkedHashMap<>();
   private Map<String, Serializable>    groupByValues   = new LinkedHashMap<>();



   /*******************************************************************************
    ** Getter for aggregateValues
    **
    *******************************************************************************/
   public Map<Aggregate, Serializable> getAggregateValues()
   {
      return aggregateValues;
   }



   /*******************************************************************************
    ** Setter for aggregateValues
    **
    *******************************************************************************/
   public void setAggregateValues(Map<Aggregate, Serializable> aggregateValues)
   {
      this.aggregateValues = aggregateValues;
   }



   /*******************************************************************************
    ** Fluent setter for aggregateValues
    **
    *******************************************************************************/
   public AggregateResult withAggregateValues(Map<Aggregate, Serializable> aggregateValues)
   {
      this.aggregateValues = aggregateValues;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for groupByValues
    **
    *******************************************************************************/
   public AggregateResult withAggregateValue(Aggregate aggregate, Serializable value)
   {
      if(this.aggregateValues == null)
      {
         this.aggregateValues = new LinkedHashMap<>();
      }
      this.aggregateValues.put(aggregate, value);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Serializable getAggregateValue(Aggregate aggregate)
   {
      return (this.aggregateValues.get(aggregate));
   }



   /*******************************************************************************
    ** Getter for groupByValues
    **
    *******************************************************************************/
   public Map<String, Serializable> getGroupByValues()
   {
      return groupByValues;
   }



   /*******************************************************************************
    ** Setter for groupByValues
    **
    *******************************************************************************/
   public void setGroupByValues(Map<String, Serializable> groupByValues)
   {
      this.groupByValues = groupByValues;
   }



   /*******************************************************************************
    ** Fluent setter for groupByValues
    **
    *******************************************************************************/
   public AggregateResult withGroupByValues(Map<String, Serializable> groupByValues)
   {
      this.groupByValues = groupByValues;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for groupByValues
    **
    *******************************************************************************/
   public AggregateResult withGroupByValue(String fieldName, Serializable value)
   {
      if(this.groupByValues == null)
      {
         this.groupByValues = new LinkedHashMap<>();
      }
      this.groupByValues.put(fieldName, value);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Serializable getGroupByValue(String fieldName)
   {
      return (this.groupByValues.get(fieldName));
   }

}
