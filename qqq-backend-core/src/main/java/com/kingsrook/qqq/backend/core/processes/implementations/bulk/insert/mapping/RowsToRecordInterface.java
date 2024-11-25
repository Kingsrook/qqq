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
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkInsertMapping;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public interface RowsToRecordInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   List<QRecord> nextPage(FileToRowsInterface fileToRowsInterface, BulkLoadFileRow headerRow, BulkInsertMapping mapping, Integer limit) throws QException;


   /***************************************************************************
    ** returns true if value from row was used, else false.
    ***************************************************************************/
   default boolean setValueOrDefault(QRecord record, QFieldMetaData field, String associationNameChain, BulkInsertMapping mapping, BulkLoadFileRow row, Integer columnIndex)
   {
      return setValueOrDefault(record, field, associationNameChain, mapping, row, columnIndex, null);
   }

   /***************************************************************************
    ** returns true if value from row was used, else false.
    ***************************************************************************/
   default boolean setValueOrDefault(QRecord record, QFieldMetaData field, String associationNameChain, BulkInsertMapping mapping, BulkLoadFileRow row, Integer columnIndex, List<Integer> wideAssociationIndexes)
   {
      boolean valueFromRowWasUsed = false;

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // build full field-name -- possibly associations, then field name, then possibly index-suffix //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      String fieldName = field.getName();
      String fieldNameWithAssociationPrefix = StringUtils.hasContent(associationNameChain) ? associationNameChain + "." + fieldName : fieldName;

      String wideAssociationSuffix = "";
      if(CollectionUtils.nullSafeHasContents(wideAssociationIndexes))
      {
         wideAssociationSuffix = "," + StringUtils.join(".", wideAssociationIndexes);
      }

      String fullFieldName = fieldNameWithAssociationPrefix + wideAssociationSuffix;

      //////////////////////////////////////////////
      // ok - look in the row - then the defaults //
      //////////////////////////////////////////////
      Serializable value = null;
      if(columnIndex != null && row != null)
      {
         value = row.getValueElseNull(columnIndex);
         if(value != null && !"".equals(value))
         {
            valueFromRowWasUsed = true;
         }
      }
      else if(mapping.getFieldNameToDefaultValueMap().containsKey(fullFieldName))
      {
         value = mapping.getFieldNameToDefaultValueMap().get(fullFieldName);
      }

      if(value != null)
      {
         record.setValue(fieldName, value);
      }

      return (valueFromRowWasUsed);
   }

}
