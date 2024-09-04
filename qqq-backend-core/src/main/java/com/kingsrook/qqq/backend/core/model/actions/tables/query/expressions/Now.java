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

package com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions;


import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class Now extends AbstractFilterExpression<Serializable>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Serializable evaluate(QFieldMetaData field) throws QException
   {
      QFieldType type = field == null ? QFieldType.DATE_TIME : field.getType();

      if(type.equals(QFieldType.DATE_TIME))
      {
         return (Instant.now());
      }
      else if(type.equals(QFieldType.DATE))
      {
         ZoneId zoneId = ValueUtils.getSessionOrInstanceZoneId();
         return (Instant.now().atZone(zoneId).toLocalDate());
      }
      else
      {
         throw (new QException("Unsupported field type [" + type + "]"));
      }
   }

}
