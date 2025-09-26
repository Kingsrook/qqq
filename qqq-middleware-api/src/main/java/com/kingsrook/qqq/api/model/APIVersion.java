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

package com.kingsrook.qqq.api.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;


/*******************************************************************************
 ** Should work as well for https://semver.org/spec/v2.0.0.html or https://calver.org/
 ** or simple increasing integers, or ?
 *******************************************************************************/
public class APIVersion implements Comparable<APIVersion>
{
   private String   version;
   private String[] parts;

   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // frequent re-validating api table meta data can reveal a hot spot in this class, with the parseInt and regex work //
   // so, memoize those.                                                                                               //
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   private static Memoization<String, Integer>  parseIntMemoization = new Memoization<>();
   private static Memoization<String, String[]> splitMemoization    = new Memoization<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public APIVersion(String version)
   {
      this.version = version;
      this.parts = splitOnDotOrDash(version);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static String[] splitOnDotOrDash(String input)
   {
      return (splitMemoization.getResult(input, s ->
      {
         List<String> result = new ArrayList<>();
         int          start  = 0;

         for(int i = 0; i < input.length(); i++)
         {
            char c = input.charAt(i);
            if(c == '.' || c == '-')
            {
               if(start != i)
               {
                  result.add(input.substring(start, i));
               }
               else
               {
                  ////////////////////////////////////
                  // Handles consecutive delimiters //
                  ////////////////////////////////////
                  result.add("");
               }
               start = i + 1;
            }
         }

         /////////////////////////////
         // Add last segment if any //
         /////////////////////////////
         if(start < input.length())
         {
            result.add(input.substring(start));
         }
         else if(!input.isEmpty() && (input.charAt(input.length() - 1) == '.' || input.charAt(input.length() - 1) == '-'))
         {
            ////////////////////////////////
            // Handles trailing delimiter //
            ////////////////////////////////
            result.add("");
         }

         return result.toArray(new String[0]);
      })).orElse(null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int compareTo(APIVersion that)
   {
      int i = 0;
      while(true)
      {
         if(i >= this.parts.length && i >= that.parts.length)
         {
            return (0);
         }
         else if(i >= this.parts.length)
         {
            return (1); // todo - review
         }
         else if(i >= that.parts.length)
         {
            return (0); // todo - review
         }
         else
         {
            String thisPart = this.parts[i];
            String thatPart = that.parts[i];
            if(!Objects.equals(thisPart, thatPart))
            {
               ///////////////////////////////////////////////////////////////////////////////////////////////////
               // parseInt calls can surprisingly be a hotspot, e.g., in tests if there's a lot of instance     //
               // creation/validation occurring, and the checks for if a field is in a version.  so, memoize it //
               ///////////////////////////////////////////////////////////////////////////////////////////////////
               Optional<Integer> thisInt = parseIntMemoization.getResult(thisPart, this::parseIntOrNull);
               Optional<Integer> thatInt = parseIntMemoization.getResult(thatPart, this::parseIntOrNull);
               if(thisInt.isPresent() && thatInt.isPresent())
               {
                  return (thisInt.get().compareTo(thatInt.get()));
               }
               else
               {
                  //////////////////////////////////////////////////////////////////////////
                  // if either doesn't parse as an integer, then compare them as strings. //
                  //////////////////////////////////////////////////////////////////////////
                  return (thisPart.compareTo(thatPart));
               }
            }
         }
         i++;
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   Integer parseIntOrNull(String s)
   {
      try
      {
         return Integer.parseInt(s);
      }
      catch(Exception e)
      {
         return (null);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return (version);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }

      if(o == null || getClass() != o.getClass())
      {
         return false;
      }

      APIVersion that = (APIVersion) o;
      return Objects.equals(version, that.version);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(version);
   }
}
