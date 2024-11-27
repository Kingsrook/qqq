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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkInsertMapping;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;


/*******************************************************************************
 ** use a flatter mapping object, where field names look like:
 ** associationChain.fieldName,index.subIndex
 *******************************************************************************/
public class WideRowsToRecordWithExplicitFieldNameSuffixIndexBasedMapping implements RowsToRecordInterface
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
         QRecord         record = makeRecordFromRow(mapping, table, "", row, fieldIndexes, headerRow, new ArrayList<>());
         rs.add(record);
      }

      ValueMapper.valueMapping(rs, mapping, table);

      return (rs);
   }



   /***************************************************************************
    ** may return null, if there were no values in the row for this (sub-wide) record.
    ***************************************************************************/
   private QRecord makeRecordFromRow(BulkInsertMapping mapping, QTableMetaData table, String associationNameChain, BulkLoadFileRow row, Map<String, Integer> fieldIndexes, BulkLoadFileRow headerRow, List<Integer> wideAssociationIndexes) throws QException
   {
      //////////////////////////////////////////////////////
      // start by building the record with its own fields //
      //////////////////////////////////////////////////////
      QRecord record = new QRecord();
      BulkLoadRecordUtils.addBackendDetailsAboutFileRows(record, row);

      boolean hadAnyValuesInRow = false;
      for(QFieldMetaData field : table.getFields().values())
      {
         hadAnyValuesInRow = setValueOrDefault(record, field, associationNameChain, mapping, row, fieldIndexes.get(field.getName()), wideAssociationIndexes) || hadAnyValuesInRow;
      }

      if(!hadAnyValuesInRow)
      {
         return (null);
      }

      /////////////////////////////
      // associations (children) //
      /////////////////////////////
      for(String associationName : CollectionUtils.nonNullList(mapping.getMappedAssociations()))
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

            List<QRecord> associatedRecords = processAssociation(associationNameMinusChain, associationNameChain, associatedTable, mapping, row, headerRow);
            record.withAssociatedRecords(associationNameMinusChain, associatedRecords);
         }
      }

      return record;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private List<QRecord> processAssociation(String associationName, String associationNameChain, QTableMetaData associatedTable, BulkInsertMapping mapping, BulkLoadFileRow row, BulkLoadFileRow headerRow) throws QException
   {
      List<QRecord> rs = new ArrayList<>();

      String associationNameChainForRecursiveCalls = "".equals(associationNameChain) ? associationName : associationNameChain + "." + associationName;

      for(int i = 0; true; i++)
      {
         // todo - doesn't support grand-children
         List<Integer>        wideAssociationIndexes = List.of(i);
         Map<String, Integer> fieldIndexes           = mapping.getFieldIndexes(associatedTable, associationNameChainForRecursiveCalls, headerRow, wideAssociationIndexes);
         if(fieldIndexes.isEmpty())
         {
            break;
         }

         QRecord record = makeRecordFromRow(mapping, associatedTable, associationNameChainForRecursiveCalls, row, fieldIndexes, headerRow, wideAssociationIndexes);
         if(record != null)
         {
            rs.add(record);
         }
      }

      return (rs);
   }



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