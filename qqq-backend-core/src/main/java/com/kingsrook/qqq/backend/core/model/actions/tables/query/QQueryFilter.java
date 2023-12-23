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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 * Full "filter" for a query - a list of criteria and order-bys
 *
 *******************************************************************************/
public class QQueryFilter implements Serializable, Cloneable
{
   private static final QLogger LOG = QLogger.getLogger(QQueryFilter.class);

   private List<QFilterCriteria> criteria = new ArrayList<>();
   private List<QFilterOrderBy>  orderBys = new ArrayList<>();

   private BooleanOperator    booleanOperator = BooleanOperator.AND;
   private List<QQueryFilter> subFilters      = new ArrayList<>();

   ////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // skip & limit are meant to only apply to QueryAction (at least at the initial time they are added here) //
   // e.g., they are ignored in CountAction, AggregateAction, etc, where their meanings may be less obvious  //
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////
   private Integer skip;
   private Integer limit;



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum BooleanOperator
   {
      AND,
      OR
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QQueryFilter()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QQueryFilter(QFilterCriteria... criteria)
   {
      this.criteria = new ArrayList<>(Arrays.stream(criteria).toList());
   }



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

         if(subFilters != null)
         {
            clone.subFilters = new ArrayList<>();
            for(QQueryFilter subFilter : subFilters)
            {
               clone.subFilters.add(subFilter.clone());
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
    **
    *******************************************************************************/
   public boolean hasAnyCriteria()
   {
      if(CollectionUtils.nullSafeHasContents(criteria))
      {
         return (true);
      }

      if(CollectionUtils.nullSafeHasContents(subFilters))
      {
         for(QQueryFilter subFilter : subFilters)
         {
            if(subFilter.hasAnyCriteria())
            {
               return (true);
            }
         }
      }

      return (false);
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



   /*******************************************************************************
    ** Getter for booleanOperator
    **
    *******************************************************************************/
   public BooleanOperator getBooleanOperator()
   {
      return booleanOperator;
   }



   /*******************************************************************************
    ** Setter for booleanOperator
    **
    *******************************************************************************/
   public void setBooleanOperator(BooleanOperator booleanOperator)
   {
      this.booleanOperator = booleanOperator;
   }



   /*******************************************************************************
    ** Fluent setter for booleanOperator
    **
    *******************************************************************************/
   public QQueryFilter withBooleanOperator(BooleanOperator booleanOperator)
   {
      this.booleanOperator = booleanOperator;
      return (this);
   }



   /*******************************************************************************
    ** Getter for subFilters
    **
    *******************************************************************************/
   public List<QQueryFilter> getSubFilters()
   {
      return subFilters;
   }



   /*******************************************************************************
    ** Setter for subFilters
    **
    *******************************************************************************/
   public void setSubFilters(List<QQueryFilter> subFilters)
   {
      this.subFilters = subFilters;
   }



   /*******************************************************************************
    ** Fluent setter for subFilters
    **
    *******************************************************************************/
   public QQueryFilter withSubFilters(List<QQueryFilter> subFilters)
   {
      this.subFilters = subFilters;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addSubFilter(QQueryFilter subFilter)
   {
      if(this.subFilters == null)
      {
         subFilters = new ArrayList<>();
      }

      subFilters.add(subFilter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      StringBuilder rs = new StringBuilder("(");
      try
      {
         for(QFilterCriteria criterion : CollectionUtils.nonNullList(criteria))
         {
            rs.append(criterion).append(" ").append(getBooleanOperator()).append(" ");
         }

         for(QQueryFilter subFilter : CollectionUtils.nonNullList(subFilters))
         {
            rs.append(subFilter);
         }
         rs.append(")");

         rs.append("OrderBy[");
         for(QFilterOrderBy orderBy : CollectionUtils.nonNullList(orderBys))
         {
            rs.append(orderBy).append(",");
         }
         rs.append("]");
      }
      catch(Exception e)
      {
         LOG.warn("Error in toString", e);
         rs.append("Error generating toString...");
      }

      return (rs.toString());
   }



   /*******************************************************************************
    ** Replace any criteria values that look like ${input.XXX} with the value of XXX
    ** from the supplied inputValues map.
    **
    ** Note - it may be very important that you call this method on a clone of a
    ** QQueryFilter - e.g., if it's one that defined in metaData, and that we don't
    ** want to be (permanently) changed!!
    *******************************************************************************/
   public void interpretValues(Map<String, Serializable> inputValues)
   {
      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", inputValues);
      for(QFilterCriteria criterion : getCriteria())
      {
         if(criterion.getValues() != null)
         {
            List<Serializable> newValues = new ArrayList<>();

            for(Serializable value : criterion.getValues())
            {
               String       valueAsString    = ValueUtils.getValueAsString(value);
               Serializable interpretedValue = variableInterpreter.interpretForObject(valueAsString);
               newValues.add(interpretedValue);
            }
            criterion.setValues(newValues);
         }
      }
   }



   /*******************************************************************************
    ** Getter for skip
    *******************************************************************************/
   public Integer getSkip()
   {
      return (this.skip);
   }



   /*******************************************************************************
    ** Setter for skip
    *******************************************************************************/
   public void setSkip(Integer skip)
   {
      this.skip = skip;
   }



   /*******************************************************************************
    ** Fluent setter for skip
    *******************************************************************************/
   public QQueryFilter withSkip(Integer skip)
   {
      this.skip = skip;
      return (this);
   }



   /*******************************************************************************
    ** Getter for limit
    *******************************************************************************/
   public Integer getLimit()
   {
      return (this.limit);
   }



   /*******************************************************************************
    ** Setter for limit
    *******************************************************************************/
   public void setLimit(Integer limit)
   {
      this.limit = limit;
   }



   /*******************************************************************************
    ** Fluent setter for limit
    *******************************************************************************/
   public QQueryFilter withLimit(Integer limit)
   {
      this.limit = limit;
      return (this);
   }

}
