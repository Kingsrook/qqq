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
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
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
    **
    ***************************************************************************/
   default void setValueOrDefault(QRecord record, String fieldName, String associationNameChain, BulkInsertMapping mapping, BulkLoadFileRow row, Integer index)
   {
      String fieldNameWithAssociationPrefix = StringUtils.hasContent(associationNameChain) ? associationNameChain + "." + fieldName : fieldName;

      Serializable value = null;
      if(index != null && row != null)
      {
         value = row.getValueElseNull(index);
      }
      else if(mapping.getFieldNameToDefaultValueMap().containsKey(fieldNameWithAssociationPrefix))
      {
         value = mapping.getFieldNameToDefaultValueMap().get(fieldNameWithAssociationPrefix);
      }

      if(value != null)
      {
         record.setValue(fieldName, value);
      }
   }

}
