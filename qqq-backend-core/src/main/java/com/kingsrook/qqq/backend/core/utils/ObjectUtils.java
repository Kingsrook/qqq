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

package com.kingsrook.qqq.backend.core.utils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ObjectUtils
{

   /*******************************************************************************
    ** A varargs version of Objects.requireNonNullElse
    *******************************************************************************/
   public static <T> T requireNonNullElse(T... objects)
   {
      if(objects == null)
      {
         throw (new NullPointerException("null objects array as input"));
      }

      for(T object : objects)
      {
         if(object != null)
         {
            return (object);
         }
      }

      throw (new NullPointerException("all null objects"));
   }
}
