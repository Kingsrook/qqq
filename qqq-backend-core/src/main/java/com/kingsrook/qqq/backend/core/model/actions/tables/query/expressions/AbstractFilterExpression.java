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

package com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractFilterExpression<T extends Serializable> implements Serializable
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract T evaluate() throws QException;



   /*******************************************************************************
    ** Evaluate the expression, given a map of input values.
    **
    ** By default, this will defer to the evaluate(void) method - but, a subclass
    ** (e.g., FilterVariableExpression) may react differently.
    *******************************************************************************/
   public T evaluateInputValues(Map<String, Serializable> inputValues) throws QException
   {
      return evaluate();
   }



   /*******************************************************************************
    ** To help with serialization, define a "type" in all subclasses
    *******************************************************************************/
   public String getType()
   {
      return (getClass().getSimpleName());
   }



   /*******************************************************************************
    ** noop - but here so serialization won't be upset about there being a type
    ** in a json object.
    *******************************************************************************/
   public void setType(String type)
   {

   }
}
