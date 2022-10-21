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

package com.kingsrook.qqq.backend.core.utils;


import java.util.Objects;


/*******************************************************************************
 ** Simple container for two objects
 *******************************************************************************/
public class Pair<A, B> implements Cloneable
{
   private A a;
   private B b;



   /*******************************************************************************
    **
    *******************************************************************************/
   public Pair(A a, B b)
   {
      this.a = a;
      this.b = b;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return (a + ":" + b);
   }



   /*******************************************************************************
    ** Getter for a
    **
    *******************************************************************************/
   public A getA()
   {
      return a;
   }



   /*******************************************************************************
    ** Getter for b
    **
    *******************************************************************************/
   public B getB()
   {
      return b;
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
      Pair<?, ?> pair = (Pair<?, ?>) o;
      return Objects.equals(a, pair.a) && Objects.equals(b, pair.b);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(a, b);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   @Override
   public Pair<A, B> clone()
   {
      try
      {
         return (Pair<A, B>) super.clone();
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }
}
