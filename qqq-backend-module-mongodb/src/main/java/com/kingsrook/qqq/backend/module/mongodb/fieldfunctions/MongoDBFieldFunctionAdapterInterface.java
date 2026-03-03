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


import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.BackendFieldFunctionAdapterInterface;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;


/*******************************************************************************
 ** MongoDB-specific extension of BackendFieldFunctionAdapterInterface
 ** for adapting a FieldFunction into MongoDB aggregation expressions.
 **
 ** Implementations return BSON-compatible objects (typically Document instances)
 ** representing MongoDB aggregation expressions (e.g., {$strLenCP: "$fieldName"}).
 *******************************************************************************/
public interface MongoDBFieldFunctionAdapterInterface extends BackendFieldFunctionAdapterInterface
{

   /*******************************************************************************
    ** Returns a MongoDB aggregation expression for the given field reference.
    ** The fieldReference is a "$"-prefixed field name (e.g., "$firstName").
    *******************************************************************************/
   Object getExpression(String fieldReference, FieldFunction fieldFunction);


   /*******************************************************************************
    ** Returns a MongoDB aggregation expression for use specifically in $sort stages.
    ** Defaults to getExpression(); may be overridden when the sort form differs
    ** (e.g., a modulo for Sunday-first weekday sorting).
    *******************************************************************************/
   default Object getExpressionForOrderBy(String fieldReference, FieldFunction fieldFunction)
   {
      return getExpression(fieldReference, fieldFunction);
   }

}
