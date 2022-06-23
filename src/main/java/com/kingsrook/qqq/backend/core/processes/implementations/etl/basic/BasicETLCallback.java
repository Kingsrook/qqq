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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.basic;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.callbacks.QProcessCallback;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;


/*******************************************************************************
 ** Provide callback functionality for the BasicETL process
 *******************************************************************************/
public class BasicETLCallback implements QProcessCallback
{

   /*******************************************************************************
    ** Get the filter query for this callback.
    *******************************************************************************/
   @Override
   public QQueryFilter getQueryFilter()
   {
      // todo - possibly get something from params?  through state?  added as a method arg?
      return null;
   }



   /*******************************************************************************
    ** Get the field values for this callback.
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   @Override
   public Map<String, Serializable> getFieldValues(List<QFieldMetaData> fields)
   {
      Map<String, Serializable> rs = new HashMap<>();
      for(QFieldMetaData field : fields)
      {
         // TODO - replace this whole thing with our params mechanism
         // TODO - add default methods to the interface that throw, presumably?
         rs.put(field.getName(), switch(field.getName())
            {
               case BasicETLProcess.FIELD_SOURCE_TABLE -> "personFile";
               case BasicETLProcess.FIELD_DESTINATION_TABLE -> "person";
               default -> throw new IllegalArgumentException("Unhandled field: " + field.getName());
            });
      }
      return (rs);
   }
}
