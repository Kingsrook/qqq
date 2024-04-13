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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Interface for (expected to be?) enums which define behaviors that get applied
 ** to fields.
 **
 ** Some of these behaviors get applied before a field is stored (insert
 ** or update), through the ValueBehaviorApplier class.  Others can be used to
 ** do more advanced display formatting than the displayFormat string alone can
 ** do (see QValueFormatter).
 **
 *******************************************************************************/
public interface FieldBehavior<T extends FieldBehavior<T>>
{

   /*******************************************************************************
    ** In case a behavior of this type wasn't set on the field, what should the
    ** default of this type be?
    *******************************************************************************/
   @JsonIgnore
   T getDefault();

   /*******************************************************************************
    ** Apply this behavior to a list of records
    *******************************************************************************/
   void apply(ValueBehaviorApplier.Action action, List<QRecord> recordList, QInstance instance, QTableMetaData table, QFieldMetaData field);

   /*******************************************************************************
    ** control if multiple behaviors of this type should be allowed together on a field.
    *******************************************************************************/
   default boolean allowMultipleBehaviorsOfThisType()
   {
      return (false);
   }

   /*******************************************************************************
    ** allow this behavior to be validated during QInstance validation.
    **
    ** return a list of validation errors, if there are any.
    *******************************************************************************/
   default List<String> validateBehaviorConfiguration(QTableMetaData tableMetaData, QFieldMetaData fieldMetaData)
   {
      return (Collections.emptyList());
   }

}
