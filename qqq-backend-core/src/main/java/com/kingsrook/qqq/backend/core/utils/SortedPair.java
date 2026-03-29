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

package com.kingsrook.qqq.backend.core.utils;


/*******************************************************************************
 **
 *******************************************************************************/
public class SortedPair<A extends Comparable<A>> extends Pair<A, A>
{

   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SortedPair(A a, A b)
   {
      super(compare(a, b) <= 0 ? a : b, compare(a, b) <= 0 ? b : a);
   }


   /***************************************************************************
    * do a null-safe compare that the constructor can use.
    ***************************************************************************/
   private static <A extends Comparable<A>> int compare(A a, A b)
   {
      if(a == null && b == null)
      {
         return (0);
      }
      else if(a == null)
      {
         return (-1);
      }
      else if(b == null)
      {
         return (1);
      }
      else
      {
         return (a.compareTo(b));
      }
   }

}
