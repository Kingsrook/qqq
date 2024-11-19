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


import java.util.ArrayList;
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
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;


/*******************************************************************************
 **
 *******************************************************************************/
public class WideRowsToRecordWithExplicitMapping implements RowsToRecordInterface
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
            setValueOrDefault(record, field, null, mapping, row, fieldIndexes.get(field.getName()));
         }

         processAssociations(mapping.getWideLayoutMapping(), "", headerRow, mapping, table, row, record);

         rs.add(record);
      }

      ValueMapper.valueMapping(rs, mapping, table);

      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void processAssociations(Map<String, BulkInsertWideLayoutMapping> mappingMap, String associationNameChain, BulkLoadFileRow headerRow, BulkInsertMapping mapping, QTableMetaData table, BulkLoadFileRow row, QRecord record) throws QException
   {
      for(Map.Entry<String, BulkInsertWideLayoutMapping> entry : CollectionUtils.nonNullMap(mappingMap).entrySet())
      {
         String                      associationName             = entry.getKey();
         BulkInsertWideLayoutMapping bulkInsertWideLayoutMapping = entry.getValue();

         Optional<Association> association = table.getAssociations().stream().filter(a -> a.getName().equals(associationName)).findFirst();
         if(association.isEmpty())
         {
            throw (new QException("Couldn't find association: " + associationName + " under table: " + table.getName()));
         }

         QTableMetaData associatedTable = QContext.getQInstance().getTable(association.get().getAssociatedTableName());

         String subChain = StringUtils.hasContent(associationNameChain) ? associationNameChain + "." + associationName: associationName;

         for(BulkInsertWideLayoutMapping.ChildRecordMapping childRecordMapping : bulkInsertWideLayoutMapping.getChildRecordMappings())
         {
            QRecord associatedRecord = processAssociation(associatedTable, subChain, childRecordMapping, mapping, row, headerRow);
            if(associatedRecord != null)
            {
               record.withAssociatedRecord(associationName, associatedRecord);
            }
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private QRecord processAssociation(QTableMetaData table, String associationNameChain, BulkInsertWideLayoutMapping.ChildRecordMapping childRecordMapping, BulkInsertMapping mapping, BulkLoadFileRow row, BulkLoadFileRow headerRow) throws QException
   {
      Map<String, Integer> fieldIndexes = childRecordMapping.getFieldIndexes(headerRow);

      QRecord associatedRecord     = new QRecord();
      boolean usedAnyValuesFromRow = false;

      for(QFieldMetaData field : table.getFields().values())
      {
         boolean valueFromRowWasUsed = setValueOrDefault(associatedRecord, field, associationNameChain, mapping, row, fieldIndexes.get(field.getName()));
         usedAnyValuesFromRow |= valueFromRowWasUsed;
      }

      if(usedAnyValuesFromRow)
      {
         processAssociations(childRecordMapping.getAssociationNameToChildRecordMappingMap(), associationNameChain, headerRow, mapping, table, row, associatedRecord);
         return (associatedRecord);
      }
      else
      {
         return (null);
      }
   }

   // /***************************************************************************
   //  **
   //  ***************************************************************************/
   // private List<QRecord> processAssociationV2(String associationName, String associationNameChain, QTableMetaData table, BulkInsertMapping mapping, BulkLoadFileRow row, BulkLoadFileRow headerRow, QRecord record, int startIndex, int endIndex) throws QException
   // {
   //    List<QRecord> rs = new ArrayList<>();

   //    Map<String, String> fieldNameToHeaderNameMapForThisAssociation = new HashMap<>();
   //    for(Map.Entry<String, String> entry : mapping.getFieldNameToHeaderNameMap().entrySet())
   //    {
   //       if(entry.getKey().startsWith(associationName + "."))
   //       {
   //          String fieldName = entry.getKey().substring(associationName.length() + 1);

   //          //////////////////////////////////////////////////////////////////////////
   //          // make sure the name here is for this table - not a sub-table under it //
   //          //////////////////////////////////////////////////////////////////////////
   //          if(!fieldName.contains("."))
   //          {
   //             fieldNameToHeaderNameMapForThisAssociation.put(fieldName, entry.getValue());
   //          }
   //       }
   //    }

   //    /////////////////////////////////////////////////////////////////////
   //    // loop over the length of the record, building associated records //
   //    /////////////////////////////////////////////////////////////////////
   //    QRecord     associatedRecord    = new QRecord();
   //    Set<String> processedFieldNames = new HashSet<>();
   //    boolean     gotAnyValues        = false;
   //    int         subStartIndex       = -1;

   //    for(int i = startIndex; i < endIndex; i++)
   //    {
   //       String headerValue = ValueUtils.getValueAsString(headerRow.getValue(i));

   //       for(Map.Entry<String, String> entry : fieldNameToHeaderNameMapForThisAssociation.entrySet())
   //       {
   //          if(headerValue.equals(entry.getValue()) || headerValue.matches(entry.getValue() + " ?\\d+"))
   //          {
   //             ///////////////////////////////////////////////
   //             // ok - this is a value for this association //
   //             ///////////////////////////////////////////////
   //             if(subStartIndex == -1)
   //             {
   //                subStartIndex = i;
   //             }

   //             String fieldName = entry.getKey();
   //             if(processedFieldNames.contains(fieldName))
   //             {
   //                /////////////////////////////////////////////////
   //                // this means we're starting a new sub-record! //
   //                /////////////////////////////////////////////////
   //                if(gotAnyValues)
   //                {
   //                   addDefaultValuesToAssociatedRecord(processedFieldNames, table, associatedRecord, mapping, associationName);
   //                   processAssociations(associationName, headerRow, mapping, table, row, associatedRecord, subStartIndex, i);
   //                   rs.add(associatedRecord);
   //                }

   //                associatedRecord = new QRecord();
   //                processedFieldNames = new HashSet<>();
   //                gotAnyValues = false;
   //                subStartIndex = i + 1;
   //             }

   //             processedFieldNames.add(fieldName);

   //             Serializable value = row.getValueElseNull(i);
   //             if(value != null && !"".equals(value))
   //             {
   //                gotAnyValues = true;
   //             }

   //             setValueOrDefault(associatedRecord, fieldName, associationName, mapping, row, i);
   //          }
   //       }
   //    }

   //    ////////////////////////
   //    // handle final value //
   //    ////////////////////////
   //    if(gotAnyValues)
   //    {
   //       addDefaultValuesToAssociatedRecord(processedFieldNames, table, associatedRecord, mapping, associationName);
   //       processAssociations(associationName, headerRow, mapping, table, row, associatedRecord, subStartIndex, endIndex);
   //       rs.add(associatedRecord);
   //    }

   //    return (rs);
   // }



   /***************************************************************************
    **
    ***************************************************************************/
   private void addDefaultValuesToAssociatedRecord(Set<String> processedFieldNames, QTableMetaData table, QRecord associatedRecord, BulkInsertMapping mapping, String associationNameChain)
   {
      for(QFieldMetaData field : table.getFields().values())
      {
         if(!processedFieldNames.contains(field.getName()))
         {
            setValueOrDefault(associatedRecord, field, associationNameChain, mapping, null, null);
         }
      }
   }

}
