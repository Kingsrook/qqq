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

package com.kingsrook.qqq.backend.core.actions.processes;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Constructor for commonly used QProcessCallback's
 *******************************************************************************/
public class QProcessCallbackFactory
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessCallback forFilter(QQueryFilter filter)
   {
      return new QProcessCallback()
      {
         /*******************************************************************************
          **
          *******************************************************************************/
         @Override
         public QQueryFilter getQueryFilter()
         {
            return (filter);
         }



         /*******************************************************************************
          **
          *******************************************************************************/
         @Override
         public Map<String, Serializable> getFieldValues(List<QFieldMetaData> fields)
         {
            return (Collections.emptyMap());
         }
      };
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessCallback forRecordEntity(QRecordEntity entity)
   {
      return forRecord(entity.toQRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessCallback forRecord(QRecord record)
   {
      String primaryKeyField = "id";
      if(StringUtils.hasContent(record.getTableName()))
      {
         primaryKeyField = QContext.getQInstance().getTable(record.getTableName()).getPrimaryKeyField();
      }

      Serializable primaryKeyValue = record.getValue(primaryKeyField);
      if(primaryKeyValue == null)
      {
         throw (new QRuntimeException("Record did not have value in its primary key field [" + primaryKeyField + "]"));
      }

      return (forPrimaryKey(primaryKeyField, primaryKeyValue));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessCallback forPrimaryKey(String fieldName, Serializable value)
   {
      return (forFilter(new QQueryFilter().withCriteria(new QFilterCriteria(fieldName, QCriteriaOperator.EQUALS, value))));
   }

}
