/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable;


import java.io.Serializable;


/*******************************************************************************
 ** a value (e.g., field name + function) used in a pivot table
 *******************************************************************************/
public class PivotTableValue implements Cloneable, Serializable
{
   private String             fieldName;
   private PivotTableFunction function;



   /*******************************************************************************
    ** Getter for fieldName
    *******************************************************************************/
   public String getFieldName()
   {
      return (this.fieldName);
   }



   /*******************************************************************************
    ** Setter for fieldName
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fieldName
    *******************************************************************************/
   public PivotTableValue withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for function
    *******************************************************************************/
   public PivotTableFunction getFunction()
   {
      return (this.function);
   }



   /*******************************************************************************
    ** Setter for function
    *******************************************************************************/
   public void setFunction(PivotTableFunction function)
   {
      this.function = function;
   }



   /*******************************************************************************
    ** Fluent setter for function
    *******************************************************************************/
   public PivotTableValue withFunction(PivotTableFunction function)
   {
      this.function = function;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public PivotTableValue clone() throws CloneNotSupportedException
   {
      PivotTableValue clone = (PivotTableValue) super.clone();
      return clone;
   }

}
