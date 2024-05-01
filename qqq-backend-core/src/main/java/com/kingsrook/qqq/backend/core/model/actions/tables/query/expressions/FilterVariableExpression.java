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
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;


/*******************************************************************************
 **
 *******************************************************************************/
public class FilterVariableExpression extends AbstractFilterExpression<Serializable>
{
   private String variableName;
   private String fieldName;
   private String operator;
   private int    valueIndex = 0;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Serializable evaluate() throws QException
   {
      throw (new QUserFacingException("Missing variable value."));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Serializable evaluateInputValues(Map<String, Serializable> inputValues) throws QException
   {
      if(!inputValues.containsKey(variableName))
      {
         throw (new QUserFacingException("Missing variable value."));
      }
      return (inputValues.get(variableName));
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FilterVariableExpression()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   private FilterVariableExpression(String fieldName, int valueIndex)
   {
      this.fieldName = fieldName;
      this.valueIndex = valueIndex;
   }



   /*******************************************************************************
    ** Getter for valueIndex
    *******************************************************************************/
   public int getValueIndex()
   {
      return (this.valueIndex);
   }



   /*******************************************************************************
    ** Setter for valueIndex
    *******************************************************************************/
   public void setValueIndex(int valueIndex)
   {
      this.valueIndex = valueIndex;
   }



   /*******************************************************************************
    ** Fluent setter for valueIndex
    *******************************************************************************/
   public FilterVariableExpression withValueIndex(int valueIndex)
   {
      this.valueIndex = valueIndex;
      return (this);
   }



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
   public FilterVariableExpression withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for variableName
    *******************************************************************************/
   public String getVariableName()
   {
      return (this.variableName);
   }



   /*******************************************************************************
    ** Setter for variableName
    *******************************************************************************/
   public void setVariableName(String variableName)
   {
      this.variableName = variableName;
   }



   /*******************************************************************************
    ** Fluent setter for variableName
    *******************************************************************************/
   public FilterVariableExpression withVariableName(String variableName)
   {
      this.variableName = variableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for operator
    *******************************************************************************/
   public String getOperator()
   {
      return (this.operator);
   }



   /*******************************************************************************
    ** Setter for operator
    *******************************************************************************/
   public void setOperator(String operator)
   {
      this.operator = operator;
   }



   /*******************************************************************************
    ** Fluent setter for operator
    *******************************************************************************/
   public FilterVariableExpression withOperator(String operator)
   {
      this.operator = operator;
      return (this);
   }

}
