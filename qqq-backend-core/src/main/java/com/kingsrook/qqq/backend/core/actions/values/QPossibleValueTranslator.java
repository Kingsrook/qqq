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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Class responsible for looking up possible-values for fields/records and
 ** make them into display values.
 *******************************************************************************/
public class QPossibleValueTranslator
{
   private static final Logger LOG = LogManager.getLogger(QPossibleValueTranslator.class);

   private final QInstance qInstance;
   private final QSession  session;

   ///////////////////////////////////////////////////////
   // top-level keys are pvsNames (not table names)     //
   // 2nd-level keys are pkey values from the PVS table //
   ///////////////////////////////////////////////////////
   private Map<String, Map<Serializable, String>> possibleValueCache;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueTranslator(QInstance qInstance, QSession session)
   {
      this.qInstance = qInstance;
      this.session = session;

      this.possibleValueCache = new HashMap<>();
   }



   /*******************************************************************************
    ** For a list of records, translate their possible values (populating their display values)
    *******************************************************************************/
   public void translatePossibleValuesInRecords(QTableMetaData table, List<QRecord> records)
   {
      if(records == null || table == null)
      {
         return;
      }

      primePvsCache(table, records);

      for(QRecord record : records)
      {
         for(QFieldMetaData field : table.getFields().values())
         {
            if(field.getPossibleValueSourceName() != null)
            {
               record.setDisplayValue(field.getName(), translatePossibleValue(field, record.getValue(field.getName())));
            }
         }
      }
   }



   /*******************************************************************************
    ** Translate a list of ids to a list of possible values (e.g., w/ rendered values)
    *******************************************************************************/
   public List<QPossibleValue<?>> buildTranslatedPossibleValueList(QPossibleValueSource possibleValueSource, List<Serializable> ids)
   {
      if(ids == null)
      {
         return (null);
      }

      if(ids.isEmpty())
      {
         return (new ArrayList<>());
      }

      List<QPossibleValue<?>> rs = new ArrayList<>();
      if(possibleValueSource.getType().equals(QPossibleValueSourceType.TABLE))
      {
         primePvsCache(possibleValueSource.getTableName(), List.of(possibleValueSource), ids);
      }

      for(Serializable id : ids)
      {
         String translated = translatePossibleValue(possibleValueSource, id);
         rs.add(new QPossibleValue<>(id, translated));
      }

      return (rs);
   }



   /*******************************************************************************
    ** For a given field and (raw/id) value, get the translated (string) value.
    *******************************************************************************/
   public String translatePossibleValue(QFieldMetaData field, Serializable value)
   {
      QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(field.getPossibleValueSourceName());
      if(possibleValueSource == null)
      {
         LOG.error("Missing possible value source named [" + field.getPossibleValueSourceName() + "] when formatting value for field [" + field.getName() + "]");
         return (null);
      }

      try
      {
         if(field.getType().equals(QFieldType.INTEGER) && !(value instanceof Integer))
         {
            value = ValueUtils.getValueAsInteger(value);
         }
      }
      catch(QValueException e)
      {
         LOG.info("Error translating possible value raw value...");
         ///////////////////////////
         // leave value as it was //
         ///////////////////////////
      }

      return translatePossibleValue(possibleValueSource, value);
   }



   /*******************************************************************************
    ** For a given PossibleValueSource and (raw/id) value, get the translated (string) value.
    *******************************************************************************/
   String translatePossibleValue(QPossibleValueSource possibleValueSource, Serializable value)
   {
      String resultValue = null;
      if(possibleValueSource.getType().equals(QPossibleValueSourceType.ENUM))
      {
         resultValue = translatePossibleValueEnum(value, possibleValueSource);
      }
      else if(possibleValueSource.getType().equals(QPossibleValueSourceType.TABLE))
      {
         resultValue = translatePossibleValueTable(value, possibleValueSource);
      }
      else if(possibleValueSource.getType().equals(QPossibleValueSourceType.CUSTOM))
      {
         resultValue = translatePossibleValueCustom(value, possibleValueSource);
      }
      else
      {
         LOG.error("Unrecognized possibleValueSourceType [" + possibleValueSource.getType() + "] in PVS named [" + possibleValueSource.getName() + "]");
      }

      if(resultValue == null)
      {
         resultValue = getDefaultForPossibleValue(possibleValueSource, value);
      }

      return (resultValue);
   }



   /*******************************************************************************
    ** do translation for an enum-type PVS
    *******************************************************************************/
   private String translatePossibleValueEnum(Serializable value, QPossibleValueSource possibleValueSource)
   {
      for(QPossibleValue<?> possibleValue : possibleValueSource.getEnumValues())
      {
         if(possibleValue.getId().equals(value))
         {
            return (formatPossibleValue(possibleValueSource, possibleValue));
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** do translation for a table-type PVS
    *******************************************************************************/
   String translatePossibleValueTable(Serializable value, QPossibleValueSource possibleValueSource)
   {
      /////////////////////////////////
      // null input gets null output //
      /////////////////////////////////
      if(value == null)
      {
         return (null);
      }

      //////////////////////////////////////////////////////////////
      // look for cached value - if it's missing, call the primer //
      //////////////////////////////////////////////////////////////
      possibleValueCache.putIfAbsent(possibleValueSource.getName(), new HashMap<>());
      Map<Serializable, String> cacheForPvs = possibleValueCache.get(possibleValueSource.getName());
      if(!cacheForPvs.containsKey(value))
      {
         primePvsCache(possibleValueSource.getTableName(), List.of(possibleValueSource), List.of(value));
      }

      return (cacheForPvs.get(value));
   }



   /*******************************************************************************
    ** do translation for a custom-type PVS
    *******************************************************************************/
   private String translatePossibleValueCustom(Serializable value, QPossibleValueSource possibleValueSource)
   {
      try
      {
         QCustomPossibleValueProvider customPossibleValueProvider = QCodeLoader.getCustomPossibleValueProvider(possibleValueSource);
         return (formatPossibleValue(possibleValueSource, customPossibleValueProvider.getPossibleValue(value)));
      }
      catch(Exception e)
      {
         LOG.warn("Error sending [" + value + "] for through custom code for PVS [" + possibleValueSource.getName() + "]", e);
      }

      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String formatPossibleValue(QPossibleValueSource possibleValueSource, QPossibleValue<?> possibleValue)
   {
      return (doFormatPossibleValue(possibleValueSource.getValueFormat(), possibleValueSource.getValueFields(), possibleValue.getId(), possibleValue.getLabel()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getDefaultForPossibleValue(QPossibleValueSource possibleValueSource, Serializable value)
   {
      if(possibleValueSource.getValueFormatIfNotFound() == null)
      {
         return (null);
      }

      return (doFormatPossibleValue(possibleValueSource.getValueFormatIfNotFound(), possibleValueSource.getValueFieldsIfNotFound(), value, null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   private String doFormatPossibleValue(String formatString, List<String> valueFields, Object id, String label)
   {
      List<Object> values = new ArrayList<>();
      if(valueFields != null)
      {
         for(String valueField : valueFields)
         {
            Object value = switch(valueField)
               {
                  case "id" -> id;
                  case "label" -> label;
                  default -> throw new IllegalArgumentException("Unexpected value field: " + valueField);
               };
            values.add(Objects.requireNonNullElse(value, ""));
         }
      }

      return (formatString.formatted(values.toArray()));
   }



   /*******************************************************************************
    ** prime the cache (e.g., by doing bulk-queries) for table-based PVS's
    **
    ** @param table the table that the records are from
    ** @param records the records that have the possible value id's (e.g., foreign keys)
    *******************************************************************************/
   void primePvsCache(QTableMetaData table, List<QRecord> records)
   {
      ListingHash<String, QFieldMetaData>       fieldsByPvsTable = new ListingHash<>();
      ListingHash<String, QPossibleValueSource> pvsesByTable     = new ListingHash<>();
      for(QFieldMetaData field : table.getFields().values())
      {
         QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(field.getPossibleValueSourceName());
         if(possibleValueSource != null && possibleValueSource.getType().equals(QPossibleValueSourceType.TABLE))
         {
            fieldsByPvsTable.add(possibleValueSource.getTableName(), field);
            pvsesByTable.add(possibleValueSource.getTableName(), possibleValueSource);
         }
      }

      for(String tableName : fieldsByPvsTable.keySet())
      {
         Set<Serializable> values = new HashSet<>();
         for(QRecord record : records)
         {
            for(QFieldMetaData field : fieldsByPvsTable.get(tableName))
            {
               Serializable fieldValue = record.getValue(field.getName());

               //////////////////////////////////////
               // check if value is already cached //
               //////////////////////////////////////
               QPossibleValueSource possibleValueSource = pvsesByTable.get(tableName).get(0);
               possibleValueCache.putIfAbsent(possibleValueSource.getName(), new HashMap<>());
               Map<Serializable, String> cacheForPvs = possibleValueCache.get(possibleValueSource.getName());

               if(!cacheForPvs.containsKey(fieldValue))
               {
                  values.add(fieldValue);
               }
            }
         }

         if(!values.isEmpty())
         {
            primePvsCache(tableName, pvsesByTable.get(tableName), values);
         }
      }
   }



   /*******************************************************************************
    ** For a given table, and a list of pkey-values in that table, AND a list of
    ** possible value sources based on that table (maybe usually 1, but could be more,
    ** e.g., if they had different formatting, or different filters (todo, would that work?)
    ** - query for the values in the table, and populate the possibleValueCache.
    *******************************************************************************/
   private void primePvsCache(String tableName, List<QPossibleValueSource> possibleValueSources, Collection<Serializable> values)
   {
      for(QPossibleValueSource possibleValueSource : possibleValueSources)
      {
         possibleValueCache.putIfAbsent(possibleValueSource.getName(), new HashMap<>());
      }

      try
      {
         String primaryKeyField = qInstance.getTable(tableName).getPrimaryKeyField();

         for(List<Serializable> page : CollectionUtils.getPages(values, 1000))
         {
            QueryInput queryInput = new QueryInput(qInstance);
            queryInput.setSession(session);
            queryInput.setTableName(tableName);
            queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria(primaryKeyField, QCriteriaOperator.IN, page)));

            /////////////////////////////////////////////////////////////////////////////////////////
            // this is needed to get record labels, which are what we use here... unclear if best! //
            /////////////////////////////////////////////////////////////////////////////////////////
            if(notTooDeep())
            {
               queryInput.setShouldTranslatePossibleValues(true);
               queryInput.setShouldGenerateDisplayValues(true);
            }

            QueryOutput queryOutput = new QueryAction().execute(queryInput);

            ///////////////////////////////////////////////////////////////////////////////////
            // for all records that were found, put a formatted value into cache foreach PVS //
            ///////////////////////////////////////////////////////////////////////////////////
            for(QRecord record : queryOutput.getRecords())
            {
               Serializable pkeyValue = record.getValue(primaryKeyField);
               for(QPossibleValueSource possibleValueSource : possibleValueSources)
               {
                  QPossibleValue<?> possibleValue = new QPossibleValue<>(pkeyValue, record.getRecordLabel());
                  possibleValueCache.get(possibleValueSource.getName()).put(pkeyValue, formatPossibleValue(possibleValueSource, possibleValue));
               }
            }

            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            // for all pkeys that were NOT found, put a null value into cache foreach PVS (to avoid re-looking up) //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            for(Serializable pkey : page)
            {
               for(QPossibleValueSource possibleValueSource : possibleValueSources)
               {
                  if(!possibleValueCache.get(possibleValueSource.getName()).containsKey(pkey))
                  {
                     possibleValueCache.get(possibleValueSource.getName()).put(pkey, null);
                  }
               }
            }
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error looking up possible values for table [" + tableName + "]", e);
      }
   }



   /*******************************************************************************
    ** Avoid infinite recursion, for where one field's PVS depends on another's...
    ** not too smart, just breaks at 5...
    *******************************************************************************/
   private boolean notTooDeep()
   {
      int count = 0;
      for(StackTraceElement stackTraceElement : new Throwable().getStackTrace())
      {
         if(stackTraceElement.getMethodName().equals("translatePossibleValuesInRecords"))
         {
            count++;
         }
      }

      return (count < 5);
   }

}
