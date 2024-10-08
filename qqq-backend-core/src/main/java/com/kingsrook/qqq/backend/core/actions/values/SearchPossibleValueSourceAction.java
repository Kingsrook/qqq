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

package com.kingsrook.qqq.backend.core.actions.values;


import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 ** Class responsible for looking up possible-values for fields/records and
 ** make them into display values.
 *******************************************************************************/
public class SearchPossibleValueSourceAction
{
   private static final QLogger LOG = QLogger.getLogger(SearchPossibleValueSourceAction.class);

   private QPossibleValueTranslator possibleValueTranslator;



   /*******************************************************************************
    **
    *******************************************************************************/
   public SearchPossibleValueSourceOutput execute(SearchPossibleValueSourceInput input) throws QException
   {
      QInstance            qInstance           = QContext.getQInstance();
      QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(input.getPossibleValueSourceName());
      if(possibleValueSource == null)
      {
         throw new QException("Missing possible value source named [" + input.getPossibleValueSourceName() + "]");
      }

      possibleValueTranslator = new QPossibleValueTranslator(QContext.getQInstance(), QContext.getQSession());
      SearchPossibleValueSourceOutput output = null;
      if(possibleValueSource.getType().equals(QPossibleValueSourceType.ENUM))
      {
         output = searchPossibleValueEnum(input, possibleValueSource);
      }
      else if(possibleValueSource.getType().equals(QPossibleValueSourceType.TABLE))
      {
         output = searchPossibleValueTable(input, possibleValueSource);
      }
      else if(possibleValueSource.getType().equals(QPossibleValueSourceType.CUSTOM))
      {
         output = searchPossibleValueCustom(input, possibleValueSource);
      }
      else
      {
         LOG.error("Unrecognized possibleValueSourceType [" + possibleValueSource.getType() + "] in PVS named [" + possibleValueSource.getName() + "]");
      }

      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private SearchPossibleValueSourceOutput searchPossibleValueEnum(SearchPossibleValueSourceInput input, QPossibleValueSource possibleValueSource)
   {
      SearchPossibleValueSourceOutput output      = new SearchPossibleValueSourceOutput();
      List<Serializable>              matchingIds = new ArrayList<>();

      List<?> inputIdsAsCorrectType = convertInputIdsToEnumIdType(possibleValueSource, input.getIdList());

      for(QPossibleValue<?> possibleValue : possibleValueSource.getEnumValues())
      {
         boolean match = false;

         if(input.getIdList() != null)
         {
            if(inputIdsAsCorrectType.contains(possibleValue.getId()))
            {
               match = true;
            }
         }
         else
         {
            if(StringUtils.hasContent(input.getSearchTerm()))
            {
               match = (Objects.equals(ValueUtils.getValueAsString(possibleValue.getId()).toLowerCase(), input.getSearchTerm().toLowerCase())
                  || possibleValue.getLabel().toLowerCase().startsWith(input.getSearchTerm().toLowerCase()));
            }
            else
            {
               match = true;
            }
         }

         if(match)
         {
            matchingIds.add((Serializable) possibleValue.getId());
         }

         // todo - skip & limit?
         // todo - default filter
      }

      List<QPossibleValue<?>> qPossibleValues = possibleValueTranslator.buildTranslatedPossibleValueList(possibleValueSource, matchingIds);
      output.setResults(qPossibleValues);

      return (output);
   }



   /*******************************************************************************
    ** The input list of ids might come through as a type that isn't the same as
    ** the type of the ids in the enum (e.g., strings from a frontend, integers
    ** in an enum).  So, this method looks at the first id in the enum, and then
    ** maps all the inputIds to be of the same type.
    *******************************************************************************/
   private List<Object> convertInputIdsToEnumIdType(QPossibleValueSource possibleValueSource, List<Serializable> inputIdList)
   {
      List<Object> rs = new ArrayList<>();
      if(CollectionUtils.nullSafeIsEmpty(inputIdList))
      {
         return (rs);
      }

      Object anIdFromTheEnum = possibleValueSource.getEnumValues().get(0).getId();

      if(anIdFromTheEnum instanceof Integer)
      {
         inputIdList.forEach(id -> rs.add(ValueUtils.getValueAsInteger(id)));
      }
      else if(anIdFromTheEnum instanceof String)
      {
         inputIdList.forEach(id -> rs.add(ValueUtils.getValueAsString(id)));
      }
      else if(anIdFromTheEnum instanceof Boolean)
      {
         inputIdList.forEach(id -> rs.add(ValueUtils.getValueAsBoolean(id)));
      }
      else
      {
         LOG.warn("Unexpected type [" + anIdFromTheEnum.getClass().getSimpleName() + "] for ids in enum: " + possibleValueSource.getName());
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private SearchPossibleValueSourceOutput searchPossibleValueTable(SearchPossibleValueSourceInput input, QPossibleValueSource possibleValueSource) throws QException
   {
      SearchPossibleValueSourceOutput output = new SearchPossibleValueSourceOutput();

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(possibleValueSource.getTableName());

      QTableMetaData table = QContext.getQInstance().getTable(possibleValueSource.getTableName());

      QQueryFilter queryFilter = new QQueryFilter();
      queryFilter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);

      if(input.getIdList() != null)
      {
         queryFilter.addCriteria(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, input.getIdList()));
      }
      else
      {
         String searchTerm = input.getSearchTerm();
         if(StringUtils.hasContent(searchTerm))
         {
            for(String valueField : possibleValueSource.getSearchFields())
            {
               try
               {
                  QFieldMetaData field = table.getField(valueField);
                  if(field.getType().equals(QFieldType.STRING))
                  {
                     queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.STARTS_WITH, List.of(searchTerm)));
                  }
                  else if(field.getType().equals(QFieldType.DATE))
                  {
                     LocalDate searchDate = ValueUtils.getValueAsLocalDate(searchTerm);
                     if(searchDate != null)
                     {
                        queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.EQUALS, searchDate));
                     }
                  }
                  else if(field.getType().equals(QFieldType.DATE_TIME))
                  {
                     Instant searchDate = ValueUtils.getValueAsInstant(searchTerm);
                     if(searchDate != null)
                     {
                        queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.EQUALS, searchDate));
                     }
                  }
                  else
                  {
                     Integer valueAsInteger = ValueUtils.getValueAsInteger(searchTerm);
                     if(valueAsInteger != null)
                     {
                        queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.EQUALS, List.of(valueAsInteger)));
                     }
                  }
               }
               catch(Exception e)
               {
                  //////////////////////////////////////////////////////////////////////////////////////////
                  // write a FALSE criteria upon exceptions (e.g., type conversion fails)                 //
                  // Why are we doing this?  so a single-field query finds nothing instead of everything. //
                  //////////////////////////////////////////////////////////////////////////////////////////
                  queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.IN, List.of()));
               }
            }
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // if given a default filter, make it the 'top level' filter and the one we just created a subfilter //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      if(input.getDefaultQueryFilter() != null)
      {
         input.getDefaultQueryFilter().addSubFilter(queryFilter);
         queryFilter = input.getDefaultQueryFilter();
      }

      // todo - skip & limit as params
      queryFilter.setLimit(250);

      queryFilter.setOrderBys(possibleValueSource.getOrderByFields());

      queryInput.setFilter(queryFilter);

      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      String fieldName;
      if(StringUtils.hasContent(possibleValueSource.getOverrideIdField()))
      {
         fieldName = possibleValueSource.getOverrideIdField();
      }
      else
      {
         fieldName = table.getPrimaryKeyField();
      }

      List<Serializable> ids = queryOutput.getRecords().stream().map(r -> r.getValue(fieldName)).toList();
      List<QPossibleValue<?>> qPossibleValues = possibleValueTranslator.buildTranslatedPossibleValueList(possibleValueSource, ids);
      output.setResults(qPossibleValues);

      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings({ "rawtypes", "unchecked" })
   private SearchPossibleValueSourceOutput searchPossibleValueCustom(SearchPossibleValueSourceInput input, QPossibleValueSource possibleValueSource)
   {
      try
      {
         QCustomPossibleValueProvider customPossibleValueProvider = QCodeLoader.getCustomPossibleValueProvider(possibleValueSource);
         List<QPossibleValue<?>>      possibleValues              = customPossibleValueProvider.search(input);

         SearchPossibleValueSourceOutput output = new SearchPossibleValueSourceOutput();
         output.setResults(possibleValues);
         return (output);
      }
      catch(Exception e)
      {
         // LOG.warn("Error sending [" + value + "] for field [" + field + "] through custom code for PVS [" + field.getPossibleValueSourceName() + "]", e);
      }

      throw new NotImplementedException("Not impleemnted");
      // return (null);
   }

}
