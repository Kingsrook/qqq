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


import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 ** Meta-data to represent a single field in a table.
 **
 *******************************************************************************/
public class QPossibleValueSource<T>
{
   private String name;
   private QPossibleValueSourceType type;

   // should these be in sub-types??
   private List<T> enumValues;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource<T> withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSourceType getType()
   {
      return type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setType(QPossibleValueSourceType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource<T> withType(QPossibleValueSourceType type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for enumValues
    **
    *******************************************************************************/
   public List<T> getEnumValues()
   {
      return enumValues;
   }



   /*******************************************************************************
    ** Setter for enumValues
    **
    *******************************************************************************/
   public void setEnumValues(List<T> enumValues)
   {
      this.enumValues = enumValues;
   }



   /*******************************************************************************
    ** Fluent setter for enumValues
    **
    *******************************************************************************/
   public QPossibleValueSource<T> withEnumValues(List<T> enumValues)
   {
      this.enumValues = enumValues;
      return this;
   }



   /*******************************************************************************
    ** Fluent adder for enumValues
    **
    *******************************************************************************/
   public QPossibleValueSource<T> addEnumValue(T enumValue)
   {
      if(this.enumValues == null)
      {
         this.enumValues = new ArrayList<>();
      }
      this.enumValues.add(enumValue);
      return this;
   }
}
