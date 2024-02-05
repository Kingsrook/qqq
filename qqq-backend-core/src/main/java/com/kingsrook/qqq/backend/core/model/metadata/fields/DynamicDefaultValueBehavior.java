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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Field behavior that sets a default value for a field dynamically.
 ** e.g., create-date fields get set to 'now' on insert.
 ** e.g., modify-date fields get set to 'now' on insert and on update.
 *******************************************************************************/
public enum DynamicDefaultValueBehavior implements FieldBehavior<DynamicDefaultValueBehavior>
{
   CREATE_DATE,
   MODIFY_DATE,
   NONE;

   private static final QLogger LOG = QLogger.getLogger(ValueTooLongBehavior.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DynamicDefaultValueBehavior getDefault()
   {
      return (NONE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void apply(ValueBehaviorApplier.Action action, List<QRecord> recordList, QInstance instance, QTableMetaData table, QFieldMetaData field)
   {
      if(this.equals(NONE))
      {
         return;
      }

      switch(this)
      {
         case CREATE_DATE -> applyCreateDate(action, recordList, table, field);
         case MODIFY_DATE -> applyModifyDate(action, recordList, table, field);
         default -> throw new IllegalStateException("Unexpected enum value: " + this);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void applyCreateDate(ValueBehaviorApplier.Action action, List<QRecord> recordList, QTableMetaData table, QFieldMetaData field)
   {
      if(!ValueBehaviorApplier.Action.INSERT.equals(action))
      {
         return;
      }

      setCreateDateOrModifyDateOnList(recordList, table, field);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void applyModifyDate(ValueBehaviorApplier.Action action, List<QRecord> recordList, QTableMetaData table, QFieldMetaData field)
   {
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // check both of these (even though they're the only 2 values at the time of this writing), just in case more enum values are added in the future //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(!ValueBehaviorApplier.Action.INSERT.equals(action) && !ValueBehaviorApplier.Action.UPDATE.equals(action))
      {
         return;
      }

      setCreateDateOrModifyDateOnList(recordList, table, field);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setCreateDateOrModifyDateOnList(List<QRecord> recordList, QTableMetaData table, QFieldMetaData field)
   {
      String       fieldName = field.getName();
      Serializable value     = getNow(table, field);

      for(QRecord record : CollectionUtils.nonNullList(recordList))
      {
         record.setValue(fieldName, value);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Serializable getNow(QTableMetaData table, QFieldMetaData field)
   {
      if(QFieldType.DATE_TIME.equals(field.getType()))
      {
         return (Instant.now());
      }
      else if(QFieldType.DATE.equals(field.getType()))
      {
         return (LocalDate.now());
      }
      else
      {
         LOG.debug("Request to apply a " + this.name() + " DynamicDefaultValueBehavior to a non-date or date-time field", logPair("table", table.getName()), logPair("field", field.getName()));
         return (null);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void noop()
   {

   }

}
