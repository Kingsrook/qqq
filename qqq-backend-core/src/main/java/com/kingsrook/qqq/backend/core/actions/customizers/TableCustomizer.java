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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.util.function.Consumer;


/*******************************************************************************
 ** Object used by TableCustomizers enum (and similar enums in backend modules)
 ** to assist with definition and validation of Customizers applied to tables.
 *******************************************************************************/
public class TableCustomizer
{
   private final String           role;
   private final Class<?>         expectedType;
   private final Consumer<Object> validationFunction;



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableCustomizer(String role, Class<?> expectedType, Consumer<Object> validationFunction)
   {
      this.role = role;
      this.expectedType = expectedType;
      this.validationFunction = validationFunction;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableCustomizer(String role, Class<?> expectedType)
   {
      this.role = role;
      this.expectedType = expectedType;
      this.validationFunction = null;
   }



   /*******************************************************************************
    ** Getter for role
    **
    *******************************************************************************/
   public String getRole()
   {
      return role;
   }



   /*******************************************************************************
    ** Getter for expectedType
    **
    *******************************************************************************/
   public Class<?> getExpectedType()
   {
      return expectedType;
   }



   /*******************************************************************************
    ** Getter for validationFunction
    **
    *******************************************************************************/
   public Consumer<Object> getValidationFunction()
   {
      return validationFunction;
   }
}
