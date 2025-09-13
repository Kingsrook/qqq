/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.utils.collections;


import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ForwardingList;


/*******************************************************************************
 * Alternative list-builder pattern (vs. List.of() or ListBuilder.of()) for contexts
 * where you want to make a list out of elements and lists of those elements.
 *
 * e.g., if you wanted to make a list that had value (1, 2, 3, 4), but were building
 * it in such a way that you were getting the elements like this:
 *
 * List.of(
 *    1,
 *    List.of(2, 3),
 *    4)
 *
 * which might make more sense if you were calling methods that returned either
 * those individual elements or lists...
 *
 * anyway, with this class, you could do that as:
 *
 * new FluentList()
 *    .with(1)
 *    .with(List.of(2, 3))
 *    .with(4)
 *
 *******************************************************************************/
public class FluentList<E> extends ForwardingList<E>
{
   List<E> list;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FluentList()
   {
      this.list = new ArrayList<>();
   }



   /*******************************************************************************
    * Constructor that lets you supply a source list (which will be wrapped by
    * MutableList, just in case you pass in an immutable one)
    *
    *******************************************************************************/
   public FluentList(List<E> list)
   {
      this.list = new MutableList<>(list);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @SafeVarargs
   public final FluentList<E> with(E... elements)
   {
      if(elements != null)
      {
         for(E e : elements)
         {
            list.add(e);
         }
      }
      return (this);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public final FluentList<E> with(List<E> elements)
   {
      if(elements != null)
      {
         list.addAll(elements);
      }
      return (this);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   protected List<E> delegate()
   {
      return list;
   }

}
