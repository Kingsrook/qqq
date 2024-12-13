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

package com.kingsrook.qqq.backend.core.model.metadata.possiblevalues;


import java.util.Objects;


/*******************************************************************************
 ** An actual possible value - an id and label.
 **
 ** Type parameter `T` is the type of the id (often Integer, maybe String)
 *******************************************************************************/
public class QPossibleValue<T>
{
   private final T      id;
   private final String label;



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public QPossibleValue(String value)
   {
      this.id = (T) value;
      this.label = value;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValue(T id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public T getId()
   {
      return id;
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
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

      QPossibleValue<?> that = (QPossibleValue<?>) o;
      return Objects.equals(id, that.id) && Objects.equals(label, that.label);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(id, label);
   }
}
