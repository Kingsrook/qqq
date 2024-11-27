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
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.FileToRowsInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkInsertMapping;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;


/*******************************************************************************
 **
 *******************************************************************************/
public class FlatRowsToRecord implements RowsToRecordInterface
{
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
         BulkLoadRecordUtils.addBackendDetailsAboutFileRows(record, row);

         for(QFieldMetaData field : table.getFields().values())
         {
            setValueOrDefault(record, field, null, mapping, row, fieldIndexes.get(field.getName()));
         }

         rs.add(record);
      }

      ValueMapper.valueMapping(rs, mapping, table);

      return (rs);
   }
}
