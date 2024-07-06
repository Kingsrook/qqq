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

package com.kingsrook.qqq.backend.core.utils;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;


/*******************************************************************************
 ** Utility methods for working with QRecords (and the values they contain)
 *******************************************************************************/
public class QRecordUtils
{

   /*******************************************************************************
    ** given 2 records, and a collection of fields, identify any fields that are
    ** not equals between the records.
    *******************************************************************************/
   public static List<QFieldMetaData> getChangedFields(QRecord a, QRecord b, Collection<QFieldMetaData> fields)
   {
      List<QFieldMetaData> changedFields = new ArrayList<>();
      for(QFieldMetaData field : CollectionUtils.nonNullCollection(fields))
      {
         Serializable valueA = ValueUtils.getValueAsFieldType(field.getType(), a == null ? null : a.getValue(field.getName()));
         Serializable valueB = ValueUtils.getValueAsFieldType(field.getType(), b == null ? null : b.getValue(field.getName()));
         if(!Objects.equals(valueA, valueB))
         {
            changedFields.add(field);
         }
      }

      return (changedFields);
   }

}
