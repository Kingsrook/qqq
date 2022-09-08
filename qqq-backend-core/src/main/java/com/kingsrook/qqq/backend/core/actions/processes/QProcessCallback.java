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

package com.kingsrook.qqq.backend.core.actions.processes;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;


/*******************************************************************************
 ** When a process/function can't run because it's missing data, this interface
 ** defines how the core framework goes back to a middleware (and possibly to a
 ** frontend) to get the data.
 *******************************************************************************/
public interface QProcessCallback
{
   /*******************************************************************************
    ** Get the filter query for this callback.
    *******************************************************************************/
   default QQueryFilter getQueryFilter()
   {
      return (null);
   }

   /*******************************************************************************
    ** Get the field values for this callback.
    *******************************************************************************/
   default Map<String, Serializable> getFieldValues(List<QFieldMetaData> fields)
   {
      return (null);
   }
}
