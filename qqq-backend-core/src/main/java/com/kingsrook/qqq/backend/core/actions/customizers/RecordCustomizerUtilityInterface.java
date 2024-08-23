/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Interface with utility methods that pre insert/update/delete customizers
 ** may want to use.
 *******************************************************************************/
public interface RecordCustomizerUtilityInterface
{
   QLogger LOG = QLogger.getLogger(RecordCustomizerUtilityInterface.class);



   /*******************************************************************************
    ** Container for an old value and a new value.
    *******************************************************************************/
   record Change(Serializable oldValue, Serializable newValue) {}


   /*******************************************************************************
    **
    *******************************************************************************/
   default Map<String, Change> getChanges(String tableName, QRecord oldRecord, QRecord newRecord)
   {
      Map<String, Change> rs = new HashMap<>();

      QTableMetaData table = QContext.getQInstance().getTable(tableName);
      for(Map.Entry<String, Serializable> entry : newRecord.getValues().entrySet())
      {
         String       fieldName = entry.getKey();
         Serializable newValue  = entry.getValue();
         Serializable oldValue  = oldRecord.getValue(fieldName);

         try
         {
            QFieldMetaData field         = table.getField(fieldName);
            Serializable   newTypedValue = ValueUtils.getValueAsFieldType(field.getType(), newValue);
            Serializable   oldTypedValue = ValueUtils.getValueAsFieldType(field.getType(), oldValue);

            if(!Objects.equals(oldTypedValue, newTypedValue))
            {
               rs.put(fieldName, new Change(oldTypedValue, newTypedValue));
            }
         }
         catch(Exception e)
         {
            LOG.info("Error getting a value as field's type", e, logPair("fieldName", fieldName), logPair("oldValue", oldValue), logPair("newValue", newValue));
         }
      }

      return (rs);
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default void errorIfNoValue(Serializable value, QRecord record, String errorMessage)
   {
      errorIf(!StringUtils.hasContent(ValueUtils.getValueAsString(value)), record, errorMessage);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default void errorIfEditedValue(QRecord oldRecord, QRecord newRecord, String fieldName, String errorMessage)
   {
      if(newRecord.getValues().containsKey(fieldName))
      {
         errorIf(isChangedValue(oldRecord.getValue(fieldName), newRecord.getValue(fieldName)), newRecord, errorMessage);
      }
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default boolean isChangedValue(Serializable oldValue, Serializable newValue)
   {
      //////////////////////////////////////////////
      // todo - probably ... some type "coercion" //
      //////////////////////////////////////////////
      return (!Objects.equals(oldValue, newValue));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default void errorIfAnyValue(Serializable value, QRecord record, String errorMessage)
   {
      if(StringUtils.hasContent(ValueUtils.getValueAsString(value)))
      {
         record.addError(new BadInputStatusMessage(errorMessage));
      }
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default void errorIf(boolean condition, QRecord record, String errorMessage)
   {
      if(condition)
      {
         record.addError(new BadInputStatusMessage(errorMessage));
      }
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default Map<Serializable, QRecord> getOldRecordMap(List<QRecord> oldRecordList, UpdateInput updateInput)
   {
      Map<Serializable, QRecord> oldRecordMap = new HashMap<>();
      for(QRecord qRecord : oldRecordList)
      {
         oldRecordMap.put(qRecord.getValue(updateInput.getTable().getPrimaryKeyField()), qRecord);
      }

      return (oldRecordMap);
   }

}
