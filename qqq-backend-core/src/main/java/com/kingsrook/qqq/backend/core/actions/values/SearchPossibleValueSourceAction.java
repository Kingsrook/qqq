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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
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
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Class responsible for looking up possible-values for fields/records and
 ** make them into display values.
 *******************************************************************************/
public class SearchPossibleValueSourceAction
{
   private static final Logger LOG = LogManager.getLogger(SearchPossibleValueSourceAction.class);

   private QPossibleValueTranslator possibleValueTranslator;



   /*******************************************************************************
    **
    *******************************************************************************/
   public SearchPossibleValueSourceOutput execute(SearchPossibleValueSourceInput input) throws QException
   {
      QInstance            qInstance           = input.getInstance();
      QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(input.getPossibleValueSourceName());
      if(possibleValueSource == null)
      {
         throw new QException("Missing possible value source named [" + input.getPossibleValueSourceName() + "]");
      }

      possibleValueTranslator = new QPossibleValueTranslator(input.getInstance(), input.getSession());
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

      for(QPossibleValue<?> possibleValue : possibleValueSource.getEnumValues())
      {
         boolean match = false;

         if(input.getIdList() != null)
         {
            if(input.getIdList().contains(possibleValue.getId()))
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
    **
    *******************************************************************************/
   private SearchPossibleValueSourceOutput searchPossibleValueTable(SearchPossibleValueSourceInput input, QPossibleValueSource possibleValueSource) throws QException
   {
      SearchPossibleValueSourceOutput output = new SearchPossibleValueSourceOutput();

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(possibleValueSource.getTableName());

      QTableMetaData table = input.getInstance().getTable(possibleValueSource.getTableName());

      QQueryFilter queryFilter = new QQueryFilter();
      queryFilter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);

      if(input.getIdList() != null)
      {
         queryFilter.addCriteria(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, input.getIdList()));
      }
      else
      {
         if(StringUtils.hasContent(input.getSearchTerm()))
         {
            for(String valueField : possibleValueSource.getSearchFields())
            {
               QFieldMetaData field = table.getField(valueField);
               if(field.getType().equals(QFieldType.STRING))
               {
                  queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.STARTS_WITH, List.of(input.getSearchTerm())));
               }
               else if(field.getType().equals(QFieldType.DATE) || field.getType().equals(QFieldType.DATE_TIME))
               {
                  LOG.debug("Not querying PVS [" + possibleValueSource.getName() + "] on date field [" + field.getName() + "]");
                  // todo - what? queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.STARTS_WITH, List.of(input.getSearchTerm())));
               }
               else
               {
                  try
                  {
                     Integer valueAsInteger = ValueUtils.getValueAsInteger(input.getSearchTerm());
                     if(valueAsInteger != null)
                     {
                        queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.EQUALS, List.of(valueAsInteger)));
                     }
                  }
                  catch(Exception e)
                  {
                     ////////////////////////////////////////////////////////
                     // write a FALSE criteria if the value isn't a number //
                     ////////////////////////////////////////////////////////
                     queryFilter.addCriteria(new QFilterCriteria(valueField, QCriteriaOperator.IN, List.of()));
                  }
               }
            }
         }
      }

      queryFilter.setOrderBys(possibleValueSource.getOrderByFields());

      // todo - skip & limit as params
      queryInput.setLimit(250);

      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // if given a default filter, make it the 'top level' filter and the one we just created a subfilter //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      if(input.getDefaultQueryFilter() != null)
      {
         input.getDefaultQueryFilter().addSubFilter(queryFilter);
         queryFilter = input.getDefaultQueryFilter();
      }
      queryInput.setFilter(queryFilter);

      QueryOutput             queryOutput     = new QueryAction().execute(queryInput);
      List<Serializable>      ids             = queryOutput.getRecords().stream().map(r -> r.getValue(table.getPrimaryKeyField())).toList();
      List<QPossibleValue<?>> qPossibleValues = possibleValueTranslator.buildTranslatedPossibleValueList(possibleValueSource, ids);
      output.setResults(qPossibleValues);

      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private SearchPossibleValueSourceOutput searchPossibleValueCustom(SearchPossibleValueSourceInput input, QPossibleValueSource possibleValueSource)
   {
      try
      {
         // QCustomPossibleValueProvider customPossibleValueProvider = QCodeLoader.getCustomPossibleValueProvider(possibleValueSource);
         // return (formatPossibleValue(possibleValueSource, customPossibleValueProvider.getPossibleValue(value)));
      }
      catch(Exception e)
      {
         // LOG.warn("Error sending [" + value + "] for field [" + field + "] through custom code for PVS [" + field.getPossibleValueSourceName() + "]", e);
      }

      throw new NotImplementedException("Not impleemnted");
      // return (null);
   }

}
