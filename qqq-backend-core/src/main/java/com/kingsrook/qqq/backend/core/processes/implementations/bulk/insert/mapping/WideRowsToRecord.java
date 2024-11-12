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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;


/*******************************************************************************
 **
 *******************************************************************************/
public class WideRowsToRecord implements RowsToRecordInterface
{
   private Memoization<Pair<String, String>, Boolean> shouldProcesssAssociationMemoization = new Memoization<>();



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> nextPage(FileToRowsInterface fileToRowsInterface, BulkLoadFileRow headerRow, BulkInsertMapping mapping, Integer limit) throws QException
   {
      QTableMetaData table = QContext.getQInstance().getTable(mapping.getTableName());
      if(table == null)
      {
         throw (new QException("Table [" + mapping.getTableName() + "] was not found in the Instance"));
      }

      List<QRecord> rs = new ArrayList<>();

      Map<String, Integer> fieldIndexes = mapping.getFieldIndexes(table, null, headerRow);

      while(fileToRowsInterface.hasNext() && rs.size() < limit)
      {
         BulkLoadFileRow row    = fileToRowsInterface.next();
         QRecord         record = new QRecord();

         for(QFieldMetaData field : table.getFields().values())
         {
            setValueOrDefault(record, field.getName(), null, mapping, row, fieldIndexes.get(field.getName()));
         }

         processAssociations("", headerRow, mapping, table, row, record, 0, headerRow.size());

         rs.add(record);
      }

      ValueMapper.valueMapping(rs, mapping);

      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void processAssociations(String associationNameChain, BulkLoadFileRow headerRow, BulkInsertMapping mapping, QTableMetaData table, BulkLoadFileRow row, QRecord record, int startIndex, int endIndex) throws QException
   {
      for(String associationName : mapping.getMappedAssociations())
      {
         boolean processAssociation = shouldProcessAssociation(associationNameChain, associationName);

         if(processAssociation)
         {
            String associationNameMinusChain = StringUtils.hasContent(associationNameChain)
               ? associationName.substring(associationNameChain.length() + 1)
               : associationName;

            Optional<Association> association = table.getAssociations().stream().filter(a -> a.getName().equals(associationNameMinusChain)).findFirst();
            if(association.isEmpty())
            {
               throw (new QException("Couldn't find association: " + associationNameMinusChain + " under table: " + table.getName()));
            }

            QTableMetaData associatedTable = QContext.getQInstance().getTable(association.get().getAssociatedTableName());

            // List<QRecord> associatedRecords = processAssociation(associationName, associationNameChain, associatedTable, mapping, row, headerRow, record);
            List<QRecord> associatedRecords = processAssociationV2(associationName, associationNameChain, associatedTable, mapping, row, headerRow, record, startIndex, endIndex);
            record.withAssociatedRecords(associationNameMinusChain, associatedRecords);
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private List<QRecord> processAssociationV2(String associationName, String associationNameChain, QTableMetaData table, BulkInsertMapping mapping, BulkLoadFileRow row, BulkLoadFileRow headerRow, QRecord record, int startIndex, int endIndex) throws QException
   {
      List<QRecord> rs = new ArrayList<>();

      Map<String, String> fieldNameToHeaderNameMapForThisAssociation = new HashMap<>();
      for(Map.Entry<String, String> entry : mapping.getFieldNameToHeaderNameMap().entrySet())
      {
         if(entry.getKey().startsWith(associationName + "."))
         {
            String fieldName = entry.getKey().substring(associationName.length() + 1);

            //////////////////////////////////////////////////////////////////////////
            // make sure the name here is for this table - not a sub-table under it //
            //////////////////////////////////////////////////////////////////////////
            if(!fieldName.contains("."))
            {
               fieldNameToHeaderNameMapForThisAssociation.put(fieldName, entry.getValue());
            }
         }
      }

      /////////////////////////////////////////////////////////////////////
      // loop over the length of the record, building associated records //
      /////////////////////////////////////////////////////////////////////
      QRecord     associatedRecord    = new QRecord();
      Set<String> processedFieldNames = new HashSet<>();
      boolean     gotAnyValues        = false;
      int         subStartIndex       = -1;

      for(int i = startIndex; i < endIndex; i++)
      {
         String headerValue = ValueUtils.getValueAsString(headerRow.getValue(i));

         for(Map.Entry<String, String> entry : fieldNameToHeaderNameMapForThisAssociation.entrySet())
         {
            if(headerValue.equals(entry.getValue()) || headerValue.matches(entry.getValue() + " ?\\d+"))
            {
               ///////////////////////////////////////////////
               // ok - this is a value for this association //
               ///////////////////////////////////////////////
               if(subStartIndex == -1)
               {
                  subStartIndex = i;
               }

               String fieldName = entry.getKey();
               if(processedFieldNames.contains(fieldName))
               {
                  /////////////////////////////////////////////////
                  // this means we're starting a new sub-record! //
                  /////////////////////////////////////////////////
                  if(gotAnyValues)
                  {
                     addDefaultValuesToAssociatedRecord(processedFieldNames, table, associatedRecord, mapping, associationName);
                     processAssociations(associationName, headerRow, mapping, table, row, associatedRecord, subStartIndex, i);
                     rs.add(associatedRecord);
                  }

                  associatedRecord = new QRecord();
                  processedFieldNames = new HashSet<>();
                  gotAnyValues = false;
                  subStartIndex = i + 1;
               }

               processedFieldNames.add(fieldName);

               Serializable value = row.getValueElseNull(i);
               if(value != null && !"".equals(value))
               {
                  gotAnyValues = true;
               }

               setValueOrDefault(associatedRecord, fieldName, associationName, mapping, row, i);
            }
         }
      }

      ////////////////////////
      // handle final value //
      ////////////////////////
      if(gotAnyValues)
      {
         addDefaultValuesToAssociatedRecord(processedFieldNames, table, associatedRecord, mapping, associationName);
         processAssociations(associationName, headerRow, mapping, table, row, associatedRecord, subStartIndex, endIndex);
         rs.add(associatedRecord);
      }

      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void addDefaultValuesToAssociatedRecord(Set<String> processedFieldNames, QTableMetaData table, QRecord associatedRecord, BulkInsertMapping mapping, String associationNameChain)
   {
      for(QFieldMetaData field : table.getFields().values())
      {
         if(!processedFieldNames.contains(field.getName()))
         {
            setValueOrDefault(associatedRecord, field.getName(), associationNameChain, mapping, null, null);
         }
      }
   }

   /***************************************************************************
    **
    ***************************************************************************/
   // private List<QRecord> processAssociation(String associationName, String associationNameChain, QTableMetaData table, BulkInsertMapping mapping, Row row, Row headerRow, QRecord record) throws QException
   // {
   //    List<QRecord> rs                                    = new ArrayList<>();
   //    String        associationNameChainForRecursiveCalls = associationName;

   //    Map<String, String> fieldNameToHeaderNameMapForThisAssociation = new HashMap<>();
   //    for(Map.Entry<String, String> entry : mapping.getFieldNameToHeaderNameMap().entrySet())
   //    {
   //       if(entry.getKey().startsWith(associationNameChainForRecursiveCalls + "."))
   //       {
   //          fieldNameToHeaderNameMapForThisAssociation.put(entry.getKey().substring(associationNameChainForRecursiveCalls.length() + 1), entry.getValue());
   //       }
   //    }

   //    Map<String, List<Integer>> indexes = new HashMap<>();
   //    for(int i = 0; i < headerRow.size(); i++)
   //    {
   //       String headerValue = ValueUtils.getValueAsString(headerRow.getValue(i));
   //       for(Map.Entry<String, String> entry : fieldNameToHeaderNameMapForThisAssociation.entrySet())
   //       {
   //          if(headerValue.equals(entry.getValue()) || headerValue.matches(entry.getValue() + " ?\\d+"))
   //          {
   //             indexes.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(i);
   //          }
   //       }
   //    }

   //    int maxIndex = indexes.values().stream().map(l -> l.size()).max(Integer::compareTo).orElse(0);

   //    //////////////////////////////////////////////////////
   //    // figure out how many sub-rows we'll be processing //
   //    //////////////////////////////////////////////////////
   //    for(int i = 0; i < maxIndex; i++)
   //    {
   //       QRecord associatedRecord = new QRecord();
   //       boolean gotAnyValues     = false;

   //       for(Map.Entry<String, String> entry : fieldNameToHeaderNameMapForThisAssociation.entrySet())
   //       {
   //          String fieldName = entry.getKey();
   //          if(indexes.containsKey(fieldName) && indexes.get(fieldName).size() > i)
   //          {
   //             Integer      index = indexes.get(fieldName).get(i);
   //             Serializable value = row.getValueElseNull(index);
   //             if(value != null && !"".equals(value))
   //             {
   //                gotAnyValues = true;
   //             }

   //             setValueOrDefault(associatedRecord, fieldName, mapping, row, index);
   //          }
   //       }

   //       if(gotAnyValues)
   //       {
   //          processAssociations(associationNameChainForRecursiveCalls, headerRow, mapping, table, row, associatedRecord, 0, headerRow.size());
   //          rs.add(associatedRecord);
   //       }
   //    }

   //    return (rs);
   // }



   /***************************************************************************
    **
    ***************************************************************************/
   boolean shouldProcessAssociation(String associationNameChain, String associationName)
   {
      return shouldProcesssAssociationMemoization.getResult(Pair.of(associationNameChain, associationName), p ->
      {
         List<String> chainParts = new ArrayList<>();
         List<String> nameParts  = new ArrayList<>();

         if(StringUtils.hasContent(associationNameChain))
         {
            chainParts.addAll(Arrays.asList(associationNameChain.split("\\.")));
         }

         if(StringUtils.hasContent(associationName))
         {
            nameParts.addAll(Arrays.asList(associationName.split("\\.")));
         }

         if(!nameParts.isEmpty())
         {
            nameParts.remove(nameParts.size() - 1);
         }

         return (chainParts.equals(nameParts));
      }).orElse(false);
   }

}
