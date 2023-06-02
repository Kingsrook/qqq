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

package com.kingsrook.qqq.backend.core.actions.tables.helpers.querystats;


import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 **
 *******************************************************************************/
public class QueryStatFilterCriteria extends QRecordEntity
{
   private Integer queryStatId;
   private String  fieldName;
   private String  operator;
   private String  values;



   /*******************************************************************************
    ** Getter for queryStatId
    *******************************************************************************/
   public Integer getQueryStatId()
   {
      return (this.queryStatId);
   }



   /*******************************************************************************
    ** Setter for queryStatId
    *******************************************************************************/
   public void setQueryStatId(Integer queryStatId)
   {
      this.queryStatId = queryStatId;
   }



   /*******************************************************************************
    ** Fluent setter for queryStatId
    *******************************************************************************/
   public QueryStatFilterCriteria withQueryStatId(Integer queryStatId)
   {
      this.queryStatId = queryStatId;
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
   public QueryStatFilterCriteria withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
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
   public QueryStatFilterCriteria withOperator(String operator)
   {
      this.operator = operator;
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    *******************************************************************************/
   public String getValues()
   {
      return (this.values);
   }



   /*******************************************************************************
    ** Setter for values
    *******************************************************************************/
   public void setValues(String values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Fluent setter for values
    *******************************************************************************/
   public QueryStatFilterCriteria withValues(String values)
   {
      this.values = values;
      return (this);
   }

}
