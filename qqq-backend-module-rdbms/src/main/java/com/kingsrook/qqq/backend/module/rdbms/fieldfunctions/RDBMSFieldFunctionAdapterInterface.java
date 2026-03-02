/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.rdbms.fieldfunctions;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.BackendFieldFunctionAdapterInterface;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;


/*******************************************************************************
 * RDBMS-specific extension of {@link BackendFieldFunctionAdapterInterface}
 * for adapting a {@link FieldFunction} into SQL expressions.
 *
 * <p>Implementations wrap a raw escaped column name in the appropriate SQL
 * function call (e.g., {@code CHAR_LENGTH(`col`)}) for use in SELECT and WHERE
 * clauses, and optionally supply bind-parameter values (e.g., for {@code
 * SUBSTR(`col`, ?, ?)}</p>
 *
 * <p>By default, the same expression is used for ORDER-BY, but, an alternative
 * can be used instead if needed (e.g., to change weekday sorting)</p>
 *******************************************************************************/
public interface RDBMSFieldFunctionAdapterInterface extends BackendFieldFunctionAdapterInterface
{
   /***************************************************************************
    * Wraps the given escaped SQL column reference in the appropriate SQL
    * function expression for use in SELECT or WHERE clauses.
    ***************************************************************************/
   String wrapColumnName(String escapedColumnName, FieldFunction fieldFunction);


   /***************************************************************************
    * Returns any SQL bind-parameter values needed by the wrapped expression
    * (e.g., SUBSTRING start index). Returns an empty list by default.
    ***************************************************************************/
   default List<Serializable> getParams(FieldFunction fieldFunction)
   {
      return Collections.emptyList();
   }


   /***************************************************************************
    * Wraps the column reference for use specifically in an ORDER BY clause.
    * Defaults to calling {@link #wrapColumnName}; may be overridden when the
    * ORDER BY form differs (e.g., a modulo for Sunday-first sorting).
    ***************************************************************************/
   default String wrapColumnNameForOrderBy(String escapedColumnName, FieldFunction fieldFunction)
   {
      return wrapColumnName(escapedColumnName, fieldFunction);
   }
}
