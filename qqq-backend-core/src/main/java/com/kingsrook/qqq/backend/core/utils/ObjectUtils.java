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


import java.util.function.Consumer;
import java.util.function.Predicate;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeConsumer;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeSupplier;


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



   /*******************************************************************************
    ** Like Objects.requireNonNullElse, only use an (unsafe) supplier as the first
    ** arg, and only if it throws, return the 2nd arg
    *******************************************************************************/
   public static <T> T tryElse(UnsafeSupplier<T, ?> supplier, T defaultIfThrew)
   {
      try
      {
         return (supplier.get());
      }
      catch(Exception e)
      {
         return (defaultIfThrew);
      }
   }



   /*******************************************************************************
    ** Like Objects.requireNonNullElse, only use an (unsafe) supplier as the first
    ** arg, and if it throws or returns null, then return the 2nd arg
    *******************************************************************************/
   public static <T> T tryAndRequireNonNullElse(UnsafeSupplier<T, ?> supplier, T defaultIfThrew)
   {
      try
      {
         T t = supplier.get();
         if(t != null)
         {
            return (t);
         }
      }
      catch(Exception e)
      {
         //////////
         // noop //
         //////////
      }

      return (defaultIfThrew);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <T> void ifNotNull(T object, Consumer<T> consumer)
   {
      if(object != null)
      {
         consumer.accept(object);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <T, E extends Exception> void ifNotNullUnsafe(T object, UnsafeConsumer<T, E> consumer) throws E
   {
      if(object != null)
      {
         consumer.run(object);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <T> T requireConditionElse(T a, Predicate<T> condition, T b)
   {
      if(condition.test(a))
      {
         return (a);
      }
      return (b);
   }

}
