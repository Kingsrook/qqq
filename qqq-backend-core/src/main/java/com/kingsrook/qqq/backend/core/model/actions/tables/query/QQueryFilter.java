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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.AbstractFilterExpression;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.FilterVariableExpression;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
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
    ** recursively look at both this filter, and any sub-filters it may have.
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
            if(subFilter != null && subFilter.hasAnyCriteria())
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
    ** Fluent setter for adding a single subFilter
    **
    *******************************************************************************/
   public QQueryFilter withSubFilter(QQueryFilter subFilter)
   {
      addSubFilter(subFilter);
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
         int criteriaIndex = 0;
         for(QFilterCriteria criterion : CollectionUtils.nonNullList(criteria))
         {
            if(criteriaIndex > 0)
            {
               rs.append(" ").append(getBooleanOperator()).append(" ");
            }
            rs.append(criterion);
            criteriaIndex++;
         }

         if(CollectionUtils.nullSafeHasContents(subFilters))
         {
            rs.append("Sub:{");
            int subIndex = 0;
            for(QQueryFilter subFilter : CollectionUtils.nonNullList(subFilters))
            {
               if(subIndex > 0)
               {
                  rs.append(" ").append(getBooleanOperator()).append(" ");
               }
               rs.append(subFilter);
               subIndex++;
            }
            rs.append("}");
         }

         rs.append(")");

         if(CollectionUtils.nullSafeHasContents(orderBys))
         {
            rs.append("OrderBy[");
            for(QFilterOrderBy orderBy : CollectionUtils.nonNullList(orderBys))
            {
               rs.append(orderBy).append(",");
            }
            rs.append("]");
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error in toString", e);
         rs.append("Error generating toString...");
      }

      return (rs.toString());
   }



   /*******************************************************************************
    ** Replaces any FilterVariables' variableNames with one constructed from the field
    ** name, criteria, and index, camel style
    **
    *******************************************************************************/
   public void prepForBackend()
   {
      Map<String, Integer> fieldOperatorMap = new HashMap<>();
      for(QFilterCriteria criterion : getCriteria())
      {
         if(criterion.getValues() != null)
         {
            int criteriaIndex = 1;
            int valueIndex    = 0;
            for(Serializable value : criterion.getValues())
            {
               ///////////////////////////////////////////////////////////////////////////////
               // keep track of what the index is for this criterion, this way if there are //
               // more than one with the same id/operator values, we can differentiate      //
               ///////////////////////////////////////////////////////////////////////////////
               String backendName = getBackendName(criterion, valueIndex);
               if(!fieldOperatorMap.containsKey(backendName))
               {
                  fieldOperatorMap.put(backendName, criteriaIndex);
               }
               else
               {
                  criteriaIndex = fieldOperatorMap.get(backendName) + 1;
                  fieldOperatorMap.put(backendName, criteriaIndex);
               }

               if(value instanceof FilterVariableExpression fve)
               {
                  if(criteriaIndex > 1)
                  {
                     backendName += criteriaIndex;
                  }
                  fve.setVariableName(backendName);
               }

               valueIndex++;
            }
         }
      }
   }



   /*******************************************************************************
    ** builds up a backend name for a field variable expression
    **
    *******************************************************************************/
   private String getBackendName(QFilterCriteria criterion, int valueIndex)
   {
      StringBuilder backendName = new StringBuilder();
      for(String fieldNameParts : criterion.getFieldName().split("\\."))
      {
         backendName.append(StringUtils.ucFirst(fieldNameParts));
      }

      for(String operatorParts : criterion.getOperator().name().split("_"))
      {
         backendName.append(StringUtils.ucFirst(operatorParts.toLowerCase()));
      }

      if(criterion.getOperator().equals(QCriteriaOperator.BETWEEN) || criterion.getOperator().equals(QCriteriaOperator.NOT_BETWEEN))
      {
         if(valueIndex == 0)
         {
            backendName.append("From");
         }
         else
         {
            backendName.append("To");
         }
      }

      return (StringUtils.lcFirst(backendName.toString()));
   }



   /*******************************************************************************
    ** Replace any criteria values that look like ${input.XXX} with the value of XXX
    ** from the supplied inputValues map.
    **
    ** Note - it may be very important that you call this method on a clone of a
    ** QQueryFilter - e.g., if it's one that defined in metaData, and that we don't
    ** want to be (permanently) changed!!
    *******************************************************************************/
   public void interpretValues(Map<String, Serializable> inputValues) throws QException
   {
      List<Exception> caughtExceptions = new ArrayList<>();

      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", inputValues);
      for(QFilterCriteria criterion : getCriteria())
      {
         if(criterion.getValues() != null)
         {
            List<Serializable> newValues = new ArrayList<>();

            for(Serializable value : criterion.getValues())
            {
               try
               {
                  if(value instanceof AbstractFilterExpression<?>)
                  {
                     ///////////////////////////////////////////////////////////////////////
                     // if a filter variable expression, evaluate the input values, which //
                     // will replace the variables with the corresponding actual values   //
                     ///////////////////////////////////////////////////////////////////////
                     if(value instanceof FilterVariableExpression filterVariableExpression)
                     {
                        newValues.add(filterVariableExpression.evaluateInputValues(inputValues));
                     }
                     else
                     {
                        newValues.add(value);
                     }
                  }
                  else
                  {
                     String       valueAsString    = ValueUtils.getValueAsString(value);
                     Serializable interpretedValue = variableInterpreter.interpretForObject(valueAsString);
                     newValues.add(interpretedValue);
                  }
               }
               catch(Exception e)
               {
                  caughtExceptions.add(e);
               }
            }
            criterion.setValues(newValues);
         }
      }

      if(!caughtExceptions.isEmpty())
      {
         String  message       = "Error interpreting filter values: " + StringUtils.joinWithCommasAndAnd(caughtExceptions.stream().map(e -> e.getMessage()).toList());
         boolean allUserFacing = caughtExceptions.stream().allMatch(QUserFacingException.class::isInstance);
         throw (allUserFacing ? new QUserFacingException(message) : new QException(message));
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



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }

      if(o == null || getClass() != o.getClass())
      {
         return false;
      }

      QQueryFilter that = (QQueryFilter) o;
      return Objects.equals(criteria, that.criteria) && Objects.equals(orderBys, that.orderBys) && booleanOperator == that.booleanOperator && Objects.equals(subFilters, that.subFilters) && Objects.equals(skip, that.skip) && Objects.equals(limit, that.limit);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(criteria, orderBys, booleanOperator, subFilters, skip, limit);
   }
}
