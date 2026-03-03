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

package com.kingsrook.qqq.backend.module.postgres.fieldfunctions;


import java.util.function.Function;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.WeekdayOfDateTimeFunction;
import com.kingsrook.qqq.backend.module.rdbms.fieldfunctions.RDBMSFieldFunctionAdapterInterface;
import org.apache.commons.lang3.BooleanUtils;


/*******************************************************************************
 * PostgreSQL adapter for the {@link com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.WeekdayOfDateFunction}
 * that generates {@code EXTRACT(ISODOW FROM col)} SQL expressions.
 *
 * <p>PostgreSQL's {@code ISODOW} function naturally matches QQQ's ISO-8601
 * weekday standard (Monday=1 … Sunday=7), so no offset is needed.</p>
 *******************************************************************************/
public class PostgreSQLRDBMSWeekdayOfDateFunction implements RDBMSFieldFunctionAdapterInterface
{

   /***************************************************************************
    * Returns an {@code EXTRACT(ISODOW FROM col)} expression — PostgreSQL's ISODOW directly matches ISO-8601 (Monday=1 … Sunday=7).
    ***************************************************************************/
   @Override
   public String wrapColumnName(String escapedColumnName, FieldFunction fieldFunction, Function<String, String> fieldNameToColumnReference)
   {
      ///////////////////////////////////////////////////////////////////
      // QQQ attempts to standardize on ISO-8601 (Monday=1, Sunday=7). //
      // Postgres ISODOW matches this.                                 //
      ///////////////////////////////////////////////////////////////////
      return "EXTRACT(ISODOW FROM " + escapedColumnName + ")";
   }



   /***************************************************************************
    * Returns the ORDER BY expression; when sortSundayFirst is true, appends {@code % 7} so Sunday (value 7) sorts as 0 before Monday.
    ***************************************************************************/
   @Override
   public String wrapColumnNameForOrderBy(String escapedColumnName, FieldFunction fieldFunction, Function<String, String> fieldNameToColumnReference)
   {
      //////////////////////////////////////////////////////////////////////////////////////////////
      // to sort Sunday first, do a % 7, which puts Sunday(7) = 0, and leaves Monday(1) = 1, etc. //
      //////////////////////////////////////////////////////////////////////////////////////////////
      Boolean sundayFirst = fieldFunction.getArgumentValueOrDefault(Boolean.class, WeekdayOfDateTimeFunction.PARAM_SORT_SUNDAY_FIRST);
      return "EXTRACT(ISODOW FROM " + escapedColumnName + ")" + (BooleanUtils.isTrue(sundayFirst) ? " % 7" : "");
   }
}
