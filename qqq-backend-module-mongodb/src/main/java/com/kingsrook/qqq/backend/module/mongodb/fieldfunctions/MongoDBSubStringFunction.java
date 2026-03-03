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

package com.kingsrook.qqq.backend.module.mongodb.fieldfunctions;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.SubStringFunction;
import org.bson.Document;


/*******************************************************************************
 ** MongoDB adapter for SubStringFunction.
 ** Generates {$substrCP: ["$field", fromIndex, length]}.
 ** MongoDB $substrCP is 0-based; QQQ SubStringFunction uses 1-based (like SQL).
 ** When no length is specified, uses Integer.MAX_VALUE as a sentinel.
 *******************************************************************************/
public class MongoDBSubStringFunction implements MongoDBFieldFunctionAdapterInterface
{

   @Override
   public Object getExpression(String fieldReference, FieldFunction fieldFunction)
   {
      Integer fromIndex = fieldFunction.getArgumentValueOrDefault(Integer.class, SubStringFunction.FROM_INDEX_PARAM);
      Integer length    = fieldFunction.getArgumentValueOrDefault(Integer.class, SubStringFunction.LENGTH_PARAM);

      int mongoFromIndex = (fromIndex != null ? fromIndex : 1) - 1; // convert 1-based to 0-based
      int mongoLength    = (length != null ? length : Integer.MAX_VALUE);

      return new Document("$substrCP", List.of(fieldReference, mongoFromIndex, mongoLength));
   }

}
