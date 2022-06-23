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

package com.kingsrook.qqq.backend.core.callbacks;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;


/*******************************************************************************
 ** Simple implementation of a callback, that does no-op (returns empty objects).
 ** Useful for scaffolding, perhaps tests.
 *******************************************************************************/
public class NoopCallback implements QProcessCallback
{
   /*******************************************************************************
    ** Get the filter query for this callback.
    *******************************************************************************/
   @Override
   public QQueryFilter getQueryFilter()
   {
      return (new QQueryFilter());
   }



   /*******************************************************************************
    ** Get the field values for this callback.
    *******************************************************************************/
   @Override
   public Map<String, Serializable> getFieldValues(List<QFieldMetaData> fields)
   {
      return (Collections.emptyMap());
   }
}
