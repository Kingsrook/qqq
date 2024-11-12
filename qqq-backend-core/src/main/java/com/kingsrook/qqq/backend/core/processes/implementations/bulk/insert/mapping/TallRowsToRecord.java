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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;


/*******************************************************************************
 **
 *******************************************************************************/
public class TallRowsToRecord implements RowsToRecordInterface
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

      List<BulkLoadFileRow> rowsForCurrentRecord = new ArrayList<>();
      List<Serializable>    recordGroupByValues  = null;

      String associationNameChain = "";

      while(fileToRowsInterface.hasNext() && rs.size() < limit)
      {
         BulkLoadFileRow row = fileToRowsInterface.next();

         List<Integer>      groupByIndexes   = mapping.getTallLayoutGroupByIndexMap().get(table.getName());
         List<Serializable> rowGroupByValues = getGroupByValues(row, groupByIndexes);
         if(rowGroupByValues == null)
         {
            continue;
         }

         if(rowsForCurrentRecord.isEmpty())
         {
            ///////////////////////////////////
            // this is first - so it's a yes //
            ///////////////////////////////////
            recordGroupByValues = rowGroupByValues;
            rowsForCurrentRecord.add(row);
         }
         else if(Objects.equals(recordGroupByValues, rowGroupByValues))
         {
            /////////////////////////////
            // a match - so keep going //
            /////////////////////////////
            rowsForCurrentRecord.add(row);
         }
         else
         {
            //////////////////////////////////////////////////////////////
            // not first, and not a match, so we can finish this record //
            //////////////////////////////////////////////////////////////
            rs.add(makeRecordFromRows(table, associationNameChain, mapping, headerRow, rowsForCurrentRecord));

            ////////////////////////////////////////
            // reset these record-specific values //
            ////////////////////////////////////////
            rowsForCurrentRecord = new ArrayList<>();
            recordGroupByValues = null;

            //////////////////////////////////////////////////////////////////////////////////////////////////////
            // we need to push this row back onto the fileToRows object, so it'll be handled in the next record //
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            fileToRowsInterface.unNext();
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // i wrote this condition in here: && rs.size() < limit                                        //
      // but IJ is saying it's always true... I can't quite see it, but, trusting static analysis... //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      if(!rowsForCurrentRecord.isEmpty())
      {
         rs.add(makeRecordFromRows(table, associationNameChain, mapping, headerRow, rowsForCurrentRecord));
      }

      ValueMapper.valueMapping(rs, mapping);

      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private QRecord makeRecordFromRows(QTableMetaData table, String associationNameChain, BulkInsertMapping mapping, BulkLoadFileRow headerRow, List<BulkLoadFileRow> rows) throws QException
   {
      QRecord record = new QRecord();

      Map<String, Integer> fieldIndexes = mapping.getFieldIndexes(table, associationNameChain, headerRow);

      //////////////////////////////////////////////////////
      // get all rows for the main table from the 0th row //
      //////////////////////////////////////////////////////
      BulkLoadFileRow row = rows.get(0);
      for(QFieldMetaData field : table.getFields().values())
      {
         setValueOrDefault(record, field.getName(), associationNameChain, mapping, row, fieldIndexes.get(field.getName()));
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

            List<QRecord> associatedRecords = processAssociation(associationNameMinusChain, associationNameChain, associatedTable, mapping, headerRow, rows);
            record.withAssociatedRecords(associationNameMinusChain, associatedRecords);
         }
      }

      return record;
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



   /***************************************************************************
    **
    ***************************************************************************/
   private List<QRecord> processAssociation(String associationName, String associationNameChain, QTableMetaData associatedTable, BulkInsertMapping mapping, BulkLoadFileRow headerRow, List<BulkLoadFileRow> rows) throws QException
   {
      List<QRecord> rs = new ArrayList<>();

      QTableMetaData table                                 = QContext.getQInstance().getTable(associatedTable.getName());
      String         associationNameChainForRecursiveCalls = "".equals(associationNameChain) ? associationName : associationNameChain + "." + associationName;

      List<BulkLoadFileRow> rowsForCurrentRecord = new ArrayList<>();
      List<Serializable>    recordGroupByValues  = null;
      for(BulkLoadFileRow row : rows)
      {
         List<Integer> groupByIndexes = mapping.getTallLayoutGroupByIndexMap().get(associationNameChainForRecursiveCalls);
         if(CollectionUtils.nullSafeIsEmpty(groupByIndexes))
         {
            throw (new QException("Missing group-by-index(es) for association: " + associationNameChainForRecursiveCalls));
         }

         List<Serializable> rowGroupByValues = getGroupByValues(row, groupByIndexes);
         if(rowGroupByValues == null)
         {
            continue;
         }

         if(rowsForCurrentRecord.isEmpty())
         {
            ///////////////////////////////////
            // this is first - so it's a yes //
            ///////////////////////////////////
            recordGroupByValues = rowGroupByValues;
            rowsForCurrentRecord.add(row);
         }
         else if(Objects.equals(recordGroupByValues, rowGroupByValues))
         {
            /////////////////////////////
            // a match - so keep going //
            /////////////////////////////
            rowsForCurrentRecord.add(row);
         }
         else
         {
            //////////////////////////////////////////////////////////////
            // not first, and not a match, so we can finish this record //
            //////////////////////////////////////////////////////////////
            rs.add(makeRecordFromRows(table, associationNameChainForRecursiveCalls, mapping, headerRow, rowsForCurrentRecord));

            ////////////////////////////////////////
            // reset these record-specific values //
            ////////////////////////////////////////
            rowsForCurrentRecord = new ArrayList<>();

            //////////////////////////////////////////////////
            // use the current row to start the next record //
            //////////////////////////////////////////////////
            rowsForCurrentRecord.add(row);
            recordGroupByValues = rowGroupByValues;
         }
      }

      ///////////
      // final //
      ///////////
      if(!rowsForCurrentRecord.isEmpty())
      {
         rs.add(makeRecordFromRows(table, associationNameChainForRecursiveCalls, mapping, headerRow, rowsForCurrentRecord));
      }

      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private List<Serializable> getGroupByValues(BulkLoadFileRow row, List<Integer> indexes)
   {
      List<Serializable> rowGroupByValues     = new ArrayList<>();
      boolean            haveAnyGroupByValues = false;
      for(Integer index : indexes)
      {
         Serializable value = row.getValueElseNull(index);
         rowGroupByValues.add(value);

         if(value != null && !"".equals(value))
         {
            haveAnyGroupByValues = true;
         }
      }

      if(!haveAnyGroupByValues)
      {
         return (null);
      }

      return (rowGroupByValues);
   }

}
