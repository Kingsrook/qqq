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

package com.kingsrook.qqq.backend.core.model.actions.tables.aggregate;


import java.io.Serializable;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;


/*******************************************************************************
 ** Define an "aggregate", e.g., to be selected in an Aggregate action.
 ** Such as SUM(cost).
 *******************************************************************************/
public class Aggregate implements Serializable
{
   private String            fieldName;
   private AggregateOperator operator;
   private QFieldType        fieldType;



   /*******************************************************************************
    **
    *******************************************************************************/
   public Aggregate()
   {
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

      Aggregate aggregate = (Aggregate) o;
      return Objects.equals(fieldName, aggregate.fieldName) && operator == aggregate.operator && fieldType == aggregate.fieldType;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(fieldName, operator, fieldType);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Aggregate(String fieldName, AggregateOperator operator)
   {
      this.fieldName = fieldName;
      this.operator = operator;
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
    ** Fluent setter for fieldName
    **
    *******************************************************************************/
   public Aggregate withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for operator
    **
    *******************************************************************************/
   public AggregateOperator getOperator()
   {
      return operator;
   }



   /*******************************************************************************
    ** Setter for operator
    **
    *******************************************************************************/
   public void setOperator(AggregateOperator operator)
   {
      this.operator = operator;
   }



   /*******************************************************************************
    ** Fluent setter for operator
    **
    *******************************************************************************/
   public Aggregate withOperator(AggregateOperator operator)
   {
      this.operator = operator;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fieldType
    *******************************************************************************/
   public QFieldType getFieldType()
   {
      return (this.fieldType);
   }



   /*******************************************************************************
    ** Setter for fieldType
    *******************************************************************************/
   public void setFieldType(QFieldType fieldType)
   {
      this.fieldType = fieldType;
   }



   /*******************************************************************************
    ** Fluent setter for fieldType
    *******************************************************************************/
   public Aggregate withFieldType(QFieldType fieldType)
   {
      this.fieldType = fieldType;
      return (this);
   }

}
