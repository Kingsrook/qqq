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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import java.io.Serializable;
import java.util.List;


/*******************************************************************************
 * A single criteria Component of a Query
 *
 *******************************************************************************/
public class QFilterCriteria implements Serializable
{
   private String             fieldName;
   private QCriteriaOperator  operator;
   private List<Serializable> values;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterCriteria()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterCriteria(String fieldName, QCriteriaOperator operator, List<Serializable> values)
   {
      this.fieldName = fieldName;
      this.operator = operator;
      this.values = values;
   }



   /*******************************************************************************
    ** Getter for fieldName
    **
    *******************************************************************************/
   public String getFieldName()
   {
      return fieldName;
   }



   /*******************************************************************************
    ** Setter for fieldName
    **
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Setter for fieldName
    **
    *******************************************************************************/
   public QFilterCriteria withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return this;
   }



   /*******************************************************************************
    ** Getter for operator
    **
    *******************************************************************************/
   public QCriteriaOperator getOperator()
   {
      return operator;
   }



   /*******************************************************************************
    ** Setter for operator
    **
    *******************************************************************************/
   public void setOperator(QCriteriaOperator operator)
   {
      this.operator = operator;
   }



   /*******************************************************************************
    ** Setter for operator
    **
    *******************************************************************************/
   public QFilterCriteria withOperator(QCriteriaOperator operator)
   {
      this.operator = operator;
      return this;
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   public List<Serializable> getValues()
   {
      return values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public void setValues(List<Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public QFilterCriteria withValues(List<Serializable> values)
   {
      this.values = values;
      return this;
   }
}
