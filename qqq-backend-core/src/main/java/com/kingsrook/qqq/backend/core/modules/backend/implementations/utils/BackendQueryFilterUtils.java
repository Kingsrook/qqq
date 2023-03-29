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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.utils;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 ** Utility class for backend modules that need to do filter operations.
 **
 ** e.g., like an in-memory module, or one that's working with files - basically
 ** one that doesn't have filtering provided by the backend (like a database or API).
 *******************************************************************************/
public class BackendQueryFilterUtils
{

   /*******************************************************************************
    ** Test if record matches filter.
    *******************************************************************************/
   public static boolean doesRecordMatch(QQueryFilter filter, QRecord qRecord)
   {
      if(filter == null || !filter.hasAnyCriteria())
      {
         return (true);
      }

      /////////////////////////////////////////////////////////////////////////////////////
      // for an AND query, default to a TRUE answer, and we'll &= each criteria's value. //
      // for an OR query, default to FALSE, and |= each criteria's value.                //
      /////////////////////////////////////////////////////////////////////////////////////
      AtomicBoolean recordMatches = new AtomicBoolean(filter.getBooleanOperator().equals(QQueryFilter.BooleanOperator.AND) ? true : false);

      ///////////////////////////////////////
      // if there are criteria, apply them //
      ///////////////////////////////////////
      for(QFilterCriteria criterion : CollectionUtils.nonNullList(filter.getCriteria()))
      {
         String       fieldName = criterion.getFieldName();
         Serializable value     = qRecord.getValue(fieldName);
         if(value == null)
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            // if the value isn't in the record - check, if it looks like a table.fieldName, but none of the //
            // field names in the record are fully qualified, then just use the field-name portion...        //
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            if(fieldName.contains("."))
            {
               Map<String, Serializable> values = qRecord.getValues();
               if(values.keySet().stream().noneMatch(n -> n.contains(".")))
               {
                  value = qRecord.getValue(fieldName.substring(fieldName.indexOf(".") + 1));
               }
            }
         }

         boolean criterionMatches = doesCriteriaMatch(criterion, fieldName, value);

         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // add this new value to the existing recordMatches value - and if we can short circuit the remaining checks, do so. //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         Boolean shortCircuitValue = applyBooleanOperator(recordMatches, criterionMatches, filter.getBooleanOperator());
         if(shortCircuitValue != null)
         {
            return (shortCircuitValue);
         }
      }

      ////////////////////////////////////////
      // apply sub-filters if there are any //
      ////////////////////////////////////////
      for(QQueryFilter subFilter : CollectionUtils.nonNullList(filter.getSubFilters()))
      {
         boolean subFilterMatches = doesRecordMatch(subFilter, qRecord);

         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // add this new value to the existing recordMatches value - and if we can short circuit the remaining checks, do so. //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         Boolean shortCircuitValue = applyBooleanOperator(recordMatches, subFilterMatches, filter.getBooleanOperator());
         if(shortCircuitValue != null)
         {
            return (shortCircuitValue);
         }
      }

      return (recordMatches.getPlain());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:indentation")
   public static boolean doesCriteriaMatch(QFilterCriteria criterion, String fieldName, Serializable value)
   {
      boolean criterionMatches = switch(criterion.getOperator())
         {
            case EQUALS -> testEquals(criterion, value);
            case NOT_EQUALS -> !testEquals(criterion, value);
            case IN -> testIn(criterion, value);
            case NOT_IN -> !testIn(criterion, value);
            case IS_BLANK -> testBlank(criterion, value);
            case IS_NOT_BLANK -> !testBlank(criterion, value);
            case CONTAINS -> testContains(criterion, fieldName, value);
            case NOT_CONTAINS -> !testContains(criterion, fieldName, value);
            case IS_NULL_OR_IN -> testBlank(criterion, value) || testIn(criterion, value);
            case LIKE -> testLike(criterion, fieldName, value);
            case NOT_LIKE -> !testLike(criterion, fieldName, value);
            case STARTS_WITH -> testStartsWith(criterion, fieldName, value);
            case NOT_STARTS_WITH -> !testStartsWith(criterion, fieldName, value);
            case ENDS_WITH -> testEndsWith(criterion, fieldName, value);
            case NOT_ENDS_WITH -> !testEndsWith(criterion, fieldName, value);
            case GREATER_THAN -> testGreaterThan(criterion, value);
            case GREATER_THAN_OR_EQUALS -> testGreaterThan(criterion, value) || testEquals(criterion, value);
            case LESS_THAN -> !testGreaterThan(criterion, value) && !testEquals(criterion, value);
            case LESS_THAN_OR_EQUALS -> !testGreaterThan(criterion, value);
            case BETWEEN ->
            {
               QFilterCriteria criteria0 = new QFilterCriteria().withValues(criterion.getValues());
               QFilterCriteria criteria1 = new QFilterCriteria().withValues(new ArrayList<>(criterion.getValues()));
               criteria1.getValues().remove(0);
               yield (testGreaterThan(criteria0, value) || testEquals(criteria0, value)) && (!testGreaterThan(criteria1, value) || testEquals(criteria1, value));
            }
            case NOT_BETWEEN ->
            {
               QFilterCriteria criteria0 = new QFilterCriteria().withValues(criterion.getValues());
               QFilterCriteria criteria1 = new QFilterCriteria().withValues(new ArrayList<>(criterion.getValues()));
               criteria1.getValues().remove(0);
               boolean between = (testGreaterThan(criteria0, value) || testEquals(criteria0, value)) && (!testGreaterThan(criteria1, value) || testEquals(criteria1, value));
               yield !between;
            }
         };
      return criterionMatches;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean testLike(QFilterCriteria criterion, String fieldName, Serializable value)
   {
      String stringValue    = getStringFieldValue(value, fieldName, criterion);
      String criterionValue = getFirstStringCriterionValue(criterion);

      String regex = sqlLikeToRegex(criterionValue);

      return (stringValue.matches(regex));
   }



   /*******************************************************************************
    ** Based on an incoming boolean value (accumulator), a new value, and a boolean
    ** operator, update the accumulator, and if we can then short-circuit remaining
    ** operations, return a true or false.  Returning null means to keep going.
    *******************************************************************************/
   private static Boolean applyBooleanOperator(AtomicBoolean accumulator, boolean newValue, QQueryFilter.BooleanOperator booleanOperator)
   {
      boolean accumulatorValue = accumulator.getPlain();
      if(booleanOperator.equals(QQueryFilter.BooleanOperator.AND))
      {
         accumulatorValue &= newValue;
         if(!accumulatorValue)
         {
            return (false);
         }
      }
      else
      {
         accumulatorValue |= newValue;
         if(accumulatorValue)
         {
            return (true);
         }
      }

      accumulator.set(accumulatorValue);
      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean testBlank(QFilterCriteria criterion, Serializable value)
   {
      if(value == null)
      {
         return (true);
      }

      if("".equals(ValueUtils.getValueAsString(value)))
      {
         return (true);
      }

      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean testGreaterThan(QFilterCriteria criterion, Serializable value)
   {
      Serializable criterionValue = criterion.getValues().get(0);
      if(criterionValue == null)
      {
         throw (new IllegalArgumentException("Missing criterion value in query"));
      }

      if(value == null)
      {
         /////////////////////////////////////////////////////////////////////////////////////
         // a database would say 'false' for if a null column is > a value, so do the same. //
         /////////////////////////////////////////////////////////////////////////////////////
         return (false);
      }

      return isGreaterThan(criterionValue, value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean isGreaterThan(Serializable a, Serializable b)
   {
      if(Objects.equals(a, b))
      {
         return false;
      }

      if(b instanceof LocalDate valueDate && a instanceof LocalDate criterionValueDate)
      {
         return (valueDate.isAfter(criterionValueDate));
      }

      if(b instanceof Number valueNumber && a instanceof Number criterionValueNumber)
      {
         return (valueNumber.doubleValue() > criterionValueNumber.doubleValue());
      }

      if(b instanceof String valueString && a instanceof String criterionValueString)
      {
         return (valueString.compareTo(criterionValueString) > 0);
      }

      if(b instanceof LocalDate || a instanceof LocalDate)
      {
         LocalDate valueDate;
         if(b instanceof LocalDate ld)
         {
            valueDate = ld;
         }
         else
         {
            valueDate = ValueUtils.getValueAsLocalDate(b);
         }

         LocalDate criterionDate;
         if(a instanceof LocalDate ld)
         {
            criterionDate = ld;
         }
         else
         {
            criterionDate = ValueUtils.getValueAsLocalDate(a);
         }

         return (valueDate.isAfter(criterionDate));
      }

      try
      {
         if(a instanceof Number numberA && b instanceof String stringB)
         {
            BigDecimal bdA = ValueUtils.getValueAsBigDecimal(numberA);
            BigDecimal bdB = ValueUtils.getValueAsBigDecimal(stringB);
            return (bdA.doubleValue() < bdB.doubleValue());
         }
         else if(a instanceof String stringA && b instanceof Number numberB)
         {
            BigDecimal bdA = ValueUtils.getValueAsBigDecimal(stringA);
            BigDecimal bdB = ValueUtils.getValueAsBigDecimal(numberB);
            return (bdA.doubleValue() < bdB.doubleValue());
         }
      }
      catch(Exception e)
      {
         // ignore...
      }

      throw (new NotImplementedException("Greater/Less Than comparisons are not (yet?) implemented for the supplied types [" + b.getClass().getSimpleName() + "][" + a.getClass().getSimpleName() + "]"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean testIn(QFilterCriteria criterion, Serializable value)
   {
      if(CollectionUtils.nullSafeHasContents(criterion.getValues()))
      {
         if(criterion.getValues().get(0) instanceof String && value instanceof Number)
         {
            value = String.valueOf(value);
         }
      }

      if(!criterion.getValues().contains(value))
      {
         return (false);
      }
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean testEquals(QFilterCriteria criterion, Serializable value)
   {
      if(value == null)
      {
         return (false);
      }

      Serializable criteriaValue = criterion.getValues().get(0);
      if(value instanceof String && criteriaValue instanceof Number)
      {
         criteriaValue = String.valueOf(criteriaValue);
      }
      else if(criteriaValue instanceof String && value instanceof Number)
      {
         value = String.valueOf(value);
      }

      if(!value.equals(criteriaValue))
      {
         return (false);
      }
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean testContains(QFilterCriteria criterion, String fieldName, Serializable value)
   {
      String stringValue    = getStringFieldValue(value, fieldName, criterion);
      String criterionValue = getFirstStringCriterionValue(criterion);

      if(!stringValue.contains(criterionValue))
      {
         return (false);
      }

      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean testStartsWith(QFilterCriteria criterion, String fieldName, Serializable value)
   {
      String stringValue    = getStringFieldValue(value, fieldName, criterion);
      String criterionValue = getFirstStringCriterionValue(criterion);

      if(!stringValue.startsWith(criterionValue))
      {
         return (false);
      }

      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean testEndsWith(QFilterCriteria criterion, String fieldName, Serializable value)
   {
      String stringValue    = getStringFieldValue(value, fieldName, criterion);
      String criterionValue = getFirstStringCriterionValue(criterion);

      if(!stringValue.endsWith(criterionValue))
      {
         return (false);
      }

      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String getFirstStringCriterionValue(QFilterCriteria criteria)
   {
      if(CollectionUtils.nullSafeIsEmpty(criteria.getValues()))
      {
         throw new IllegalArgumentException("Missing value for [" + criteria.getOperator() + "] criteria on field [" + criteria.getFieldName() + "]");
      }
      Serializable value = criteria.getValues().get(0);
      if(value == null)
      {
         return "";
      }

      if(!(value instanceof String stringValue))
      {
         throw new ClassCastException("Value [" + value + "] for criteria [" + criteria.getFieldName() + "] is not a String, which is required for the [" + criteria.getOperator() + "] operator.");
      }

      return (stringValue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String getStringFieldValue(Serializable value, String fieldName, QFilterCriteria criterion)
   {
      if(value == null)
      {
         return "";
      }

      if(!(value instanceof String stringValue))
      {
         throw new ClassCastException("Value [" + value + "] in field [" + fieldName + "] is not a String, which is required for the [" + criterion.getOperator() + "] operator.");
      }

      return (stringValue);
   }



   /*******************************************************************************
    ** Sort list of records based on filter.
    *******************************************************************************/
   public static void sortRecordList(QQueryFilter filter, List<QRecord> recordList)
   {
      if(filter == null || CollectionUtils.nullSafeIsEmpty(filter.getOrderBys()))
      {
         return;
      }

      recordList.sort((a, b) ->
      {
         for(QFilterOrderBy orderBy : filter.getOrderBys())
         {
            Serializable valueA = a.getValue(orderBy.getFieldName());
            Serializable valueB = b.getValue(orderBy.getFieldName());
            if(Objects.equals(valueA, valueB))
            {
               continue;
            }
            else if(isGreaterThan(valueA, valueB))
            {
               return (orderBy.getIsAscending() ? -1 : 1);
            }
            else // Less Than
            {
               return (orderBy.getIsAscending() ? 1 : -1);
            }
         }

         return (0);
      });
   }



   /*******************************************************************************
    ** Apply skip & limit attributes from queryInput to a list of records.
    *******************************************************************************/
   public static List<QRecord> applySkipAndLimit(QueryInput queryInput, List<QRecord> recordList)
   {
      Integer skip = queryInput.getSkip();
      if(skip != null && skip > 0)
      {
         if(skip < recordList.size())
         {
            recordList = recordList.subList(skip, recordList.size());
         }
         else
         {
            recordList.clear();
         }
      }

      Integer limit = queryInput.getLimit();
      if(limit != null && limit >= 0 && limit < recordList.size())
      {
         recordList = recordList.subList(0, limit);
      }
      return recordList;
   }



   /*******************************************************************************
    ** ... written by ChatGPT
    *******************************************************************************/
   static String sqlLikeToRegex(String sqlLikeExpression)
   {
      StringBuilder regex = new StringBuilder("^");

      for(int i = 0; i < sqlLikeExpression.length(); i++)
      {
         char c = sqlLikeExpression.charAt(i);

         if(c == '%')
         {
            regex.append(".*");
         }
         else if(c == '_')
         {
            regex.append(".");
         }
         else if("[]^$|\\(){}.*+?".indexOf(c) >= 0)
         {
            regex.append("\\").append(c);
         }
         else
         {
            regex.append(c);
         }
      }

      regex.append("$");
      return regex.toString();
   }
}
