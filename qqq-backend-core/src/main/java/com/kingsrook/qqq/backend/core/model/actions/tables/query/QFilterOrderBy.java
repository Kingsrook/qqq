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


/*******************************************************************************
 ** Bean representing an element of a query order-by clause.
 **
 *******************************************************************************/
public class QFilterOrderBy implements Serializable
{
   private String fieldName;
   private boolean isAscending = true;



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
    ** Fluent Setter for fieldName
    **
    *******************************************************************************/
   public QFilterOrderBy withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isAscending
    **
    *******************************************************************************/
   public boolean getIsAscending()
   {
      return isAscending;
   }



   /*******************************************************************************
    ** Setter for isAscending
    **
    *******************************************************************************/
   public void setIsAscending(boolean ascending)
   {
      isAscending = ascending;
   }



   /*******************************************************************************
    ** Fluent Setter for isAscending
    **
    *******************************************************************************/
   public QFilterOrderBy withIsAscending(boolean ascending)
   {
      this.isAscending = ascending;
      return (this);
   }

}
