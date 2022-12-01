/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.values;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Utility class to apply value behaviors to records.  
 *******************************************************************************/
public class ValueBehaviorApplier
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static void applyFieldBehaviors(QInstance instance, QTableMetaData table, List<QRecord> recordList)
   {
      for(QFieldMetaData field : table.getFields().values())
      {
         String fieldName = field.getName();
         if(field.getType().equals(QFieldType.STRING) && field.getMaxLength() != null)
         {
            applyValueTooLongBehavior(instance, recordList, field, fieldName);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void applyValueTooLongBehavior(QInstance instance, List<QRecord> recordList, QFieldMetaData field, String fieldName)
   {
      ValueTooLongBehavior valueTooLongBehavior = field.getBehavior(instance, ValueTooLongBehavior.class);

      ////////////////////////////////////////////////////////////////////////////////////////////////////
      // don't process PASS_THROUGH - so we don't have to iterate over the whole record list to do noop //
      ////////////////////////////////////////////////////////////////////////////////////////////////////
      if(valueTooLongBehavior != null && !valueTooLongBehavior.equals(ValueTooLongBehavior.PASS_THROUGH))
      {
         for(QRecord record : recordList)
         {
            String value = record.getValueString(fieldName);
            if(value != null && value.length() > field.getMaxLength())
            {
               switch(valueTooLongBehavior)
               {
                  case TRUNCATE -> record.setValue(fieldName, StringUtils.safeTruncate(value, field.getMaxLength()));
                  case TRUNCATE_ELLIPSIS -> record.setValue(fieldName, StringUtils.safeTruncate(value, field.getMaxLength(), "..."));
                  case ERROR -> record.addError("The value for " + field.getLabel() + " is too long (max allowed length=" + field.getMaxLength() + ")");
                  case PASS_THROUGH ->
                  {
                  }
                  default -> throw new IllegalStateException("Unexpected valueTooLongBehavior: " + valueTooLongBehavior);
               }
            }
         }
      }
   }

}
