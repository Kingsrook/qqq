/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.collections.TypeTolerantKeyMap;


/*******************************************************************************
 ** utility class to help table customizers working with the oldRecordList.
 ** Usage is just 2 lines:
 ** outside of loop-over-records:
 **   - OldRecordHelper oldRecordHelper = new OldRecordHelper(updateInput.getTableName(), oldRecordList);
 ** then inside the record loop:
 **   - Optional<QRecord> oldRecord = oldRecordHelper.getOldRecord(record);
 *******************************************************************************/
public class OldRecordHelper
{
   private String     primaryKeyField;
   private QFieldType primaryKeyType;

   private Optional<List<QRecord>>    oldRecordList;
   private Map<Serializable, QRecord> oldRecordMap;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OldRecordHelper(String tableName, Optional<List<QRecord>> oldRecordList)
   {
      this.primaryKeyField = QContext.getQInstance().getTable(tableName).getPrimaryKeyField();
      this.primaryKeyType = QContext.getQInstance().getTable(tableName).getField(primaryKeyField).getType();

      this.oldRecordList = oldRecordList;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public Optional<QRecord> getOldRecord(QRecord record)
   {
      if(oldRecordMap == null)
      {
         if(oldRecordList.isPresent())
         {
            oldRecordMap = new TypeTolerantKeyMap<>(primaryKeyType);
            oldRecordList.get().forEach(r -> oldRecordMap.put(r.getValue(primaryKeyField), r));
         }
         else
         {
            oldRecordMap = Collections.emptyMap();
         }
      }

      return (Optional.ofNullable(oldRecordMap.get(record.getValue(primaryKeyField))));
   }
}
