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


import java.time.ZoneId;
import java.util.List;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.WeekdayOfDateTimeFunction;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.bson.Document;


/*******************************************************************************
 ** MongoDB adapter for WeekdayOfDateTimeFunction.
 ** Same ISO conversion as WeekdayOfDate, but uses timezone-aware
 ** form of $dayOfWeek: {$dayOfWeek: {date: "$field", timezone: "tz"}}.
 **
 ** Timezone resolution follows the same priority as the RDBMS version:
 **   1. Explicit timeZoneId argument
 **   2. Session zone ID (if useSessionZoneId is true)
 **   3. Instance default time zone
 *******************************************************************************/
public class MongoDBWeekdayOfDateTimeFunction implements MongoDBFieldFunctionAdapterInterface
{

   @Override
   public Object getExpression(String fieldReference, FieldFunction fieldFunction)
   {
      return buildIsoExpression(fieldReference, fieldFunction);
   }



   @Override
   public Object getExpressionForOrderBy(String fieldReference, FieldFunction fieldFunction)
   {
      Boolean sundayFirst = fieldFunction.getArgumentValueOrDefault(Boolean.class, WeekdayOfDateTimeFunction.PARAM_SORT_SUNDAY_FIRST);
      Object  isoExpr     = buildIsoExpression(fieldReference, fieldFunction);

      if(BooleanUtils.isTrue(sundayFirst))
      {
         return new Document("$mod", List.of(isoExpr, 7));
      }

      return isoExpr;
   }



   /*******************************************************************************
    ** Build the ISO-8601 weekday expression with timezone awareness.
    *******************************************************************************/
   private Object buildIsoExpression(String fieldReference, FieldFunction fieldFunction)
   {
      String timeZoneId = resolveTimeZoneId(fieldFunction);

      Object mongoDow = new Document("$dayOfWeek", new Document("date", fieldReference).append("timezone", timeZoneId));
      Object plus5    = new Document("$add", List.of(mongoDow, 5));
      Object mod7     = new Document("$mod", List.of(plus5, 7));
      return new Document("$add", List.of(mod7, 1));
   }



   /*******************************************************************************
    ** Resolve the timezone ID using the same priority as the RDBMS adapter.
    *******************************************************************************/
   private String resolveTimeZoneId(FieldFunction fieldFunction)
   {
      Boolean useSessionZoneId = fieldFunction.getArgumentValueOrDefault(Boolean.class, WeekdayOfDateTimeFunction.PARAM_USE_SESSION_ZONE_ID);
      String  timeZoneId;

      if(BooleanUtils.isTrue(useSessionZoneId))
      {
         timeZoneId = ObjectUtils.tryElse(() -> ValueUtils.getSessionOrInstanceZoneId().toString(), ZoneId.systemDefault().getId());
      }
      else
      {
         timeZoneId = QContext.getQInstance().getDefaultTimeZoneId();
      }

      String timeZoneIdArg = fieldFunction.getArgumentValueOrDefault(String.class, WeekdayOfDateTimeFunction.PARAM_TIME_ZONE_ID);
      if(StringUtils.hasContent(timeZoneIdArg))
      {
         timeZoneId = ValueUtils.getValueAsString(timeZoneIdArg);
      }

      return timeZoneId;
   }

}
