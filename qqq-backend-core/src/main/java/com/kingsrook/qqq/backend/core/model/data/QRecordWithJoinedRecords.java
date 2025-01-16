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

package com.kingsrook.qqq.backend.core.model.data;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;


/*******************************************************************************
 ** Extension on QRecord, intended to be used where you've got records from
 ** multiple tables, and you want to combine them into a single "wide" joined
 ** record - but to do so without copying or modifying any of the individual
 ** records.
 **
 ** e.g., given:
 ** - Order (id, orderNo, orderDate) (main table)
 ** - LineItem (id, sku, quantity)
 ** - Extrinsic (id, key, value)
 **
 ** If set up in here as:
 ** - new QRecordWithJoinedRecords(order)
 **      .withJoinedRecordValues(lineItem)
 **      .withJoinedRecordValues(extrinsic)
 **
 ** Then we'd have the appearance of values in the object like:
 ** - id, orderNo, orderDate, lineItem.id, lineItem.sku, lineItem.quantity, extrinsic.id, extrinsic.key, extrinsic.value
 **
 ** Which, by the by, is how a query that returns joined records looks, and, is
 ** what BackendQueryFilterUtils can use to do filter.
 **
 ** This is done without copying or mutating any of the records (which, if you just use
 ** QRecord.withJoinedRecordValues, then those values are copied into the main record)
 ** - because this object is just storing references to the input records.
 **
 ** Note that this implies that, values changed in this record (e.g, calls to setValue)
 ** WILL impact the underlying records!
 *******************************************************************************/
public class QRecordWithJoinedRecords extends QRecord
{
   private QRecord              mainRecord;
   private Map<String, QRecord> components = new LinkedHashMap<>();


   /***************************************************************************
    **
    ***************************************************************************/
   public QRecordWithJoinedRecords(QRecord mainRecord)
   {
      this.mainRecord = mainRecord;
   }



   /*************************************************************************
    **
    ***************************************************************************/
   @Override
   public void addJoinedRecordValues(String joinTableName, QRecord joinedRecord)
   {
      components.put(joinTableName, joinedRecord);
   }



   /*************************************************************************
    **
    ***************************************************************************/
   public QRecordWithJoinedRecords withJoinedRecordValues(QRecord record, String joinTableName)
   {
      addJoinedRecordValues(joinTableName, record);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Serializable getValue(String fieldName)
   {
      return performFunctionOnRecordBasedOnFieldName(fieldName, ((record, f) -> record.getValue(f)));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void setValue(String fieldName, Object value)
   {
      performFunctionOnRecordBasedOnFieldName(fieldName, ((record, f) ->
      {
         record.setValue(f, value);
         return (null);
      }));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void setValue(String fieldName, Serializable value)
   {
      performFunctionOnRecordBasedOnFieldName(fieldName, ((record, f) ->
      {
         record.setValue(f, value);
         return (null);
      }));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void removeValue(String fieldName)
   {
      performFunctionOnRecordBasedOnFieldName(fieldName, ((record, f) ->
      {
         record.removeValue(f);
         return (null);
      }));
   }



   /***************************************************************************
    ** avoid having this same block in all the functions that call it...
    ** given a fieldName, which may be a joinTable.fieldName, apply the function
    ** to the right entity.
    ***************************************************************************/
   private Serializable performFunctionOnRecordBasedOnFieldName(String fieldName, BiFunction<QRecord, String, Serializable> functionToPerform)
   {
      if(fieldName.contains("."))
      {
         String[] parts     = fieldName.split("\\.");
         QRecord  component = components.get(parts[0]);
         if(component != null)
         {
            return functionToPerform.apply(component, parts[1]);
         }
         else
         {
            return null;
         }
      }
      else
      {
         return functionToPerform.apply(mainRecord, fieldName);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Map<String, Serializable> getValues()
   {
      Map<String, Serializable> rs = new LinkedHashMap<>(mainRecord.getValues());
      for(Map.Entry<String, QRecord> componentEntry : components.entrySet())
      {
         String  joinTableName   = componentEntry.getKey();
         QRecord componentRecord = componentEntry.getValue();
         for(Map.Entry<String, Serializable> entry : componentRecord.getValues().entrySet())
         {
            rs.put(joinTableName + "." + entry.getKey(), entry.getValue());
         }
      }
      return (rs);
   }

}
