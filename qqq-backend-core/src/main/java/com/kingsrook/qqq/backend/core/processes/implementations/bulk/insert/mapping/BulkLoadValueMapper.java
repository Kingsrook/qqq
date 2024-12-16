/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.values.SearchPossibleValueSourceAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkInsertMapping;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkLoadValueMapper
{
   private static final QLogger LOG = QLogger.getLogger(BulkLoadValueMapper.class);



   /***************************************************************************
    **
    ***************************************************************************/
   public static void valueMapping(List<QRecord> records, BulkInsertMapping mapping, QTableMetaData table) throws QException
   {
      valueMapping(records, mapping, table, null);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void valueMapping(List<QRecord> records, BulkInsertMapping mapping, QTableMetaData table, String associationNameChain) throws QException
   {
      if(CollectionUtils.nullSafeIsEmpty(records))
      {
         return;
      }

      String associationNamePrefixForFields = StringUtils.hasContent(associationNameChain) ? associationNameChain + "." : "";
      String tableLabelPrefix               = StringUtils.hasContent(associationNameChain) ? table.getLabel() + ": " : "";

      Map<String, ListingHash<String, QRecord>> possibleValueToRecordMap = new HashMap<>();

      Map<String, Map<String, Serializable>> mappingForTable = mapping.getFieldNameToValueMappingForTable(associationNameChain);
      for(QRecord record : records)
      {
         for(Map.Entry<String, Serializable> valueEntry : record.getValues().entrySet())
         {
            QFieldMetaData field = table.getField(valueEntry.getKey());
            Serializable   value = valueEntry.getValue();

            ///////////////////
            // value mappin' //
            ///////////////////
            if(mappingForTable.containsKey(field.getName()) && value != null)
            {
               Serializable mappedValue = mappingForTable.get(field.getName()).get(ValueUtils.getValueAsString(value));
               if(mappedValue != null)
               {
                  value = mappedValue;
               }
            }

            /////////////////////
            // type convertin' //
            /////////////////////
            if(value != null && !"".equals(value))
            {
               if(StringUtils.hasContent(field.getPossibleValueSourceName()))
               {
                  ListingHash<String, QRecord> fieldPossibleValueToRecordMap = possibleValueToRecordMap.computeIfAbsent(field.getName(), k -> new ListingHash<>());
                  fieldPossibleValueToRecordMap.add(ValueUtils.getValueAsString(value), record);
               }
               else
               {
                  QFieldType type = field.getType();
                  try
                  {
                     value = ValueUtils.getValueAsFieldType(type, value);
                  }
                  catch(Exception e)
                  {
                     record.addError(new BulkLoadValueTypeError(associationNamePrefixForFields + field.getName(), value, type, tableLabelPrefix + field.getLabel()));
                  }
               }
            }

            record.setValue(field.getName(), value);
         }

         //////////////////////////////////////
         // recursively process associations //
         //////////////////////////////////////
         for(Map.Entry<String, List<QRecord>> entry : record.getAssociatedRecords().entrySet())
         {
            String                associationName = entry.getKey();
            Optional<Association> association     = table.getAssociations().stream().filter(a -> a.getName().equals(associationName)).findFirst();
            if(association.isPresent())
            {
               QTableMetaData associatedTable = QContext.getQInstance().getTable(association.get().getAssociatedTableName());
               valueMapping(entry.getValue(), mapping, associatedTable, StringUtils.hasContent(associationNameChain) ? associationNameChain + "." + associationName : associationName);
            }
            else
            {
               throw new QException("Missing association [" + associationName + "] on table [" + table.getName() + "]");
            }
         }
      }

      //////////////////////////////////////////
      // look up and validate possible values //
      //////////////////////////////////////////
      for(Map.Entry<String, ListingHash<String, QRecord>> entry : possibleValueToRecordMap.entrySet())
      {
         String                       fieldName                     = entry.getKey();
         QFieldMetaData               field                         = table.getField(fieldName);
         ListingHash<String, QRecord> fieldPossibleValueToRecordMap = possibleValueToRecordMap.get(fieldName);

         handlePossibleValues(field, fieldPossibleValueToRecordMap, associationNamePrefixForFields, tableLabelPrefix);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void handlePossibleValues(QFieldMetaData field, ListingHash<String, QRecord> fieldPossibleValueToRecordMap, String associationNamePrefixForFields, String tableLabelPrefix) throws QException
   {
      QPossibleValueSource possibleValueSource = QContext.getQInstance().getPossibleValueSource(field.getPossibleValueSourceName());

      Set<String>                    values                      = fieldPossibleValueToRecordMap.keySet();
      Map<String, Serializable>      valuesToValueInPvsIdTypeMap = new HashMap<>();
      Map<String, QPossibleValue<?>> valuesFound                 = new HashMap<>();
      Set<Serializable>              valuesNotFound              = new HashSet<>();

      ////////////////////////////////////////////////////////
      // do a search, trying to use all given values as ids //
      ////////////////////////////////////////////////////////
      SearchPossibleValueSourceInput searchPossibleValueSourceInput = new SearchPossibleValueSourceInput();
      searchPossibleValueSourceInput.setPossibleValueSourceName(field.getPossibleValueSourceName());

      ArrayList<Serializable> idList = new ArrayList<>();
      for(String value : values)
      {
         Serializable valueInPvsIdType = value;

         try
         {
            valueInPvsIdType = ValueUtils.getValueAsFieldType(possibleValueSource.getIdType(), value);
         }
         catch(Exception e)
         {
            ////////////////////////////
            // leave as original type //
            ////////////////////////////
         }

         valuesToValueInPvsIdTypeMap.put(value, valueInPvsIdType);
         idList.add(valueInPvsIdType);
         valuesNotFound.add(valueInPvsIdType);
      }

      searchPossibleValueSourceInput.setIdList(idList);
      searchPossibleValueSourceInput.setLimit(values.size());
      LOG.debug("Searching possible value source by ids during bulk load mapping", logPair("pvsName", field.getPossibleValueSourceName()), logPair("noOfIds", idList.size()), logPair("firstId", () -> idList.get(0)));
      SearchPossibleValueSourceOutput searchPossibleValueSourceOutput = idList.isEmpty() ? new SearchPossibleValueSourceOutput() : new SearchPossibleValueSourceAction().execute(searchPossibleValueSourceInput);

      ////////////////////////////////////////////////////////////////////////////////////////////////////
      // for each possible value found, remove it from the set of ones not-found, and store it as a hit //
      ////////////////////////////////////////////////////////////////////////////////////////////////////
      for(QPossibleValue<?> possibleValue : searchPossibleValueSourceOutput.getResults())
      {
         String valueAsString = ValueUtils.getValueAsString(possibleValue.getId());
         valuesFound.put(valueAsString, possibleValue);
         valuesNotFound.remove(valueAsString);
      }

      ///////////////////////////////////////////////////////////////////////////
      // if there are any that weren't found, try to look them up now by label //
      ///////////////////////////////////////////////////////////////////////////
      if(!valuesNotFound.isEmpty())
      {
         searchPossibleValueSourceInput = new SearchPossibleValueSourceInput();
         searchPossibleValueSourceInput.setPossibleValueSourceName(field.getPossibleValueSourceName());
         List<String> labelList = valuesNotFound.stream().map(ValueUtils::getValueAsString).toList();
         searchPossibleValueSourceInput.setLabelList(labelList);
         searchPossibleValueSourceInput.setLimit(valuesNotFound.size());

         LOG.debug("Searching possible value source by labels during bulk load mapping", logPair("pvsName", field.getPossibleValueSourceName()), logPair("noOfLabels", labelList.size()), logPair("firstLabel", () -> labelList.get(0)));
         searchPossibleValueSourceOutput = new SearchPossibleValueSourceAction().execute(searchPossibleValueSourceInput);
         for(QPossibleValue<?> possibleValue : searchPossibleValueSourceOutput.getResults())
         {
            valuesFound.put(possibleValue.getLabel(), possibleValue);
            valuesNotFound.remove(possibleValue.getLabel());
         }
      }

      ////////////////////////////////////////////////////////////////////////////////
      // for each record now, either set a usable value (e.g., a PV.id) or an error //
      ////////////////////////////////////////////////////////////////////////////////
      for(Map.Entry<String, List<QRecord>> entry : fieldPossibleValueToRecordMap.entrySet())
      {
         String       value            = entry.getKey();
         Serializable valueInPvsIdType = valuesToValueInPvsIdTypeMap.get(entry.getKey());
         String       pvsIdAsString    = ValueUtils.getValueAsString(valueInPvsIdType);

         for(QRecord record : entry.getValue())
         {
            if(valuesFound.containsKey(pvsIdAsString))
            {
               record.setValue(field.getName(), valuesFound.get(pvsIdAsString).getId());
            }
            else
            {
               record.addError(new BulkLoadPossibleValueError(associationNamePrefixForFields + field.getName(), value, tableLabelPrefix + field.getLabel()));
            }
         }
      }
   }

}
