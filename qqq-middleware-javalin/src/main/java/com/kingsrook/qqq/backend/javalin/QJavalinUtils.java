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

package com.kingsrook.qqq.backend.javalin;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import io.javalin.http.Context;


/*******************************************************************************
 ** Utility methods shared by javalin implementations
 *******************************************************************************/
public class QJavalinUtils
{

   /*******************************************************************************
    ** Returns Integer if context has a valid int query parameter by the given name,
    **  Returns null if no param (or empty value).
    **  Throws QValueException for malformed numbers.
    *******************************************************************************/
   public static Integer integerQueryParam(Context context, String name) throws QValueException
   {
      String value = context.queryParam(name);
      if(StringUtils.hasContent(value))
      {
         return (ValueUtils.getValueAsInteger(value));
      }

      return (null);
   }



   /*******************************************************************************
    ** Returns true iff context has a valid query parameter by the given name, with
    ** a value of "true".
    *******************************************************************************/
   public static boolean queryParamIsTrue(Context context, String name) throws QValueException
   {
      String value = context.queryParam(name);
      if(Objects.equals(value, "true"))
      {
         return (true);
      }

      return (false);
   }



   /*******************************************************************************
    ** Returns Integer if context has a valid int form parameter by the given name,
    **  Returns null if no param (or empty value).
    **  Throws QValueException for malformed numbers.
    *******************************************************************************/
   public static Integer integerFormParam(Context context, String name) throws QValueException
   {
      String value = context.formParam(name);
      if(StringUtils.hasContent(value))
      {
         return (ValueUtils.getValueAsInteger(value));
      }

      return (null);
   }



   /*******************************************************************************
    ** Returns String if context has a valid query parameter by the given name,
    *  Returns null if no param (or empty value).
    *******************************************************************************/
   public static String stringQueryParam(Context context, String name)
   {
      String value = context.queryParam(name);
      if(StringUtils.hasContent(value))
      {
         return (value);
      }

      return (null);
   }



   /***************************************************************************
    ** get a param value from either the form-body, or query string returning
    ** the first one found, looking in that order, null if neither is found.
    ** uses try-catch on reading each of those, as they apparently can throw!
    ***************************************************************************/
   static String getFormParamOrQueryParam(Context context, String parameterName)
   {
      String value = null;
      try
      {
         value = context.formParam(parameterName);
      }
      catch(Exception e)
      {
         ////////////////
         // leave null //
         ////////////////
      }

      if(!StringUtils.hasContent(value))
      {
         try
         {
            value = context.queryParam(parameterName);
         }
         catch(Exception e)
         {
            ////////////////
            // leave null //
            ////////////////
         }
      }

      return value;
   }



   /***************************************************************************
    ** get a param value from either the query string, or form-body, returning
    ** the first one found, looking in that order, null if neither is found.
    ** uses try-catch on reading each of those, as they apparently can throw!
    ***************************************************************************/
   static String getQueryParamOrFormParam(Context context, String parameterName)
   {
      String value = null;
      try
      {
         value = context.queryParam(parameterName);
      }
      catch(Exception e)
      {
         ////////////////
         // leave null //
         ////////////////
      }

      if(!StringUtils.hasContent(value))
      {
         try
         {
            value = context.formParam(parameterName);
         }
         catch(Exception e)
         {
            ////////////////
            // leave null //
            ////////////////
         }
      }

      return value;
   }
}
