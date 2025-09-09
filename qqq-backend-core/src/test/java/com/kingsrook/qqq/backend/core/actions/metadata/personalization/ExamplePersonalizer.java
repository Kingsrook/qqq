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

package com.kingsrook.qqq.backend.core.actions.metadata.personalization;


import java.util.HashSet;
import java.util.Set;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.personalization.TableMetaDataPersonalizerInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.Pair;


/*******************************************************************************
 * For tests, an example of a table personalizer.
 *
 * Uses static config methods to define business rules.  Use reset method to clear.
 *******************************************************************************/
public class ExamplePersonalizer implements TableMetaDataPersonalizerInterface
{
   private static Set<String>                                       customizableTables     = new HashSet<>();
   private static ListingHash<String, Pair<String, String>>         fieldsToRemoveByUserId = new ListingHash<>();
   private static ListingHash<String, Pair<String, QFieldMetaData>> fieldsToAddByUserId    = new ListingHash<>();



   /***************************************************************************
    *
    ***************************************************************************/
   public static void registerInQInstance()
   {
      QContext.getQInstance().addSupplementalCustomizer(TableMetaDataPersonalizerInterface.CUSTOMIZER_TYPE, new QCodeReference(ExamplePersonalizer.class));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public QTableMetaData execute(TableMetaDataPersonalizerInput input) throws QException
   {
      if(!customizableTables.contains(input.getTableName()))
      {
         return (input.getTable());
      }

      if(!QInputSource.USER.equals(input.getInputSource()))
      {
         return (input.getTable());
      }

      QTableMetaData clone = input.getTable().clone();

      String userId = QContext.getQSession().getUser().getIdReference();
      for(Pair<String, String> pair : CollectionUtils.nonNullList(fieldsToRemoveByUserId.get(userId)))
      {
         String tableName = pair.getA();
         String fieldName = pair.getB();
         if(input.getTableName().equals(tableName))
         {
            clone.getFields().remove(fieldName);
         }
      }

      for(Pair<String, QFieldMetaData> pair : CollectionUtils.nonNullList(fieldsToAddByUserId.get(userId)))
      {
         String         tableName = pair.getA();
         QFieldMetaData field     = pair.getB();
         if(input.getTableName().equals(tableName))
         {
            clone.getFields().put(field.getName(), field);
         }
      }

      return (clone);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static void addCustomizableTable(String tableName)
   {
      customizableTables.add(tableName);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static void addFieldToRemoveForUserId(String tableName, String fieldName, String userId)
   {
      fieldsToRemoveByUserId.add(userId, Pair.of(tableName, fieldName));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static void addFieldToAddForUserId(String tableName, QFieldMetaData field, String userId)
   {
      fieldsToAddByUserId.add(userId, Pair.of(tableName, field));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static void reset()
   {
      customizableTables.clear();
      fieldsToRemoveByUserId.clear();
      fieldsToAddByUserId.clear();
   }
}
