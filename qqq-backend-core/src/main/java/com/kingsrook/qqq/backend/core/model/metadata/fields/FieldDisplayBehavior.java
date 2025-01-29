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


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Interface to mark a field behavior as one to be used during generating
 ** display values.
 *******************************************************************************/
public interface FieldDisplayBehavior<T extends FieldDisplayBehavior<T>> extends FieldBehavior<T>
{
   NoopFieldDisplayBehavior NOOP = new NoopFieldDisplayBehavior();

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   @SuppressWarnings("unchecked")
   default T getDefault()
   {
      return (T) NOOP;
   }


   /***************************************************************************
    ** a default implementation for this behavior type, which does nothing.
    ***************************************************************************/
   class NoopFieldDisplayBehavior implements FieldDisplayBehavior<NoopFieldDisplayBehavior>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void apply(ValueBehaviorApplier.Action action, List<QRecord> recordList, QInstance instance, QTableMetaData table, QFieldMetaData field)
      {

      }
   }
}
