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

package com.kingsrook.qqq.backend.module.api.utils;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Help parse query strings into maps.
 *******************************************************************************/
public class QueryStringParser
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static Map<String, String> parseQueryStringSingleValuePerKey(String queryString)
   {
      Map<String, String> rs = new LinkedHashMap<>();
      if(StringUtils.hasContent(queryString))
      {
         for(String nameValuePair : queryString.split("&"))
         {
            String[] nameAndValue = nameValuePair.split("=", 2);
            String   name         = nameAndValue[0];
            String   value        = nameAndValue.length > 1 ? nameAndValue[1] : "";
            rs.put(name, value);
         }
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Map<String, List<String>> parseQueryStringMultiValuePerKey(String queryString)
   {
      Map<String, List<String>> rs = new LinkedHashMap<>();
      if(StringUtils.hasContent(queryString))
      {
         for(String nameValuePair : queryString.split("&"))
         {
            String[] nameAndValue = nameValuePair.split("=", 2);
            String   name         = nameAndValue[0];
            String   value        = nameAndValue.length > 1 ? nameAndValue[1] : "";
            rs.computeIfAbsent(name, (key) -> new ArrayList<>());
            rs.get(name).add(value);
         }
      }

      return (rs);
   }

}
