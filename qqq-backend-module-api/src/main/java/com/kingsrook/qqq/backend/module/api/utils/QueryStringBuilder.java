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

package com.kingsrook.qqq.backend.module.api.utils;


import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Utility for building a query string - taking care of things like:
 ** - do I need the "?"
 ** - do I need a "&"
 ** - urlEncoding params (depending on which method you call: (name, value) does
 **   encode -- (pair) does not.)
 *******************************************************************************/
public class QueryStringBuilder
{
   private List<Pair<String, String>> pairs = new ArrayList<>();



   /*******************************************************************************
    ** Assumes both name and value have NOT been previous URL Encoded
    *******************************************************************************/
   public void addPair(String name, Serializable value)
   {
      String valueString = urlEncode(ValueUtils.getValueAsString(value));
      pairs.add(new Pair<>(urlEncode(name), valueString));
   }



   /*******************************************************************************
    ** Assumes both name and value have NOT been previous URL Encoded
    *******************************************************************************/
   public QueryStringBuilder withPair(String name, Serializable value)
   {
      addPair(name, value);
      return (this);
   }



   /*******************************************************************************
    ** Assumes both parts are already properly uri encoded
    *******************************************************************************/
   public void addPair(String pair)
   {
      String[] parts = pair.split("=", 2);
      if(parts.length == 1)
      {
         pairs.add(new Pair<>(parts[0], ""));
      }
      else
      {
         pairs.add(new Pair<>(parts[0], parts[1]));
      }
   }



   /*******************************************************************************
    ** Assumes both parts are already properly uri encoded
    *******************************************************************************/
   public QueryStringBuilder withPair(String pair)
   {
      addPair(pair);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String toQueryString()
   {
      if(pairs.isEmpty())
      {
         return ("");
      }

      return ("?" + pairs.stream().map(p -> p.getA() + "=" + p.getB()).collect(Collectors.joining("&")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return (toQueryString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String urlEncode(Serializable s)
   {
      return (URLEncoder.encode(ValueUtils.getValueAsString(s), StandardCharsets.UTF_8));
   }

}
