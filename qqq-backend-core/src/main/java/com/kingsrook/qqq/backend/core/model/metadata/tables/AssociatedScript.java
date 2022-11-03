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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import java.io.Serializable;


/*******************************************************************************
 **
 *******************************************************************************/
public class AssociatedScript implements Serializable
{
   private String       fieldName;
   private Serializable scriptTypeId;



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
   public AssociatedScript withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptTypeId
    **
    *******************************************************************************/
   public Serializable getScriptTypeId()
   {
      return scriptTypeId;
   }



   /*******************************************************************************
    ** Setter for scriptTypeId
    **
    *******************************************************************************/
   public void setScriptTypeId(Serializable scriptTypeId)
   {
      this.scriptTypeId = scriptTypeId;
   }



   /*******************************************************************************
    ** Fluent setter for scriptTypeId
    **
    *******************************************************************************/
   public AssociatedScript withScriptTypeId(Serializable scriptTypeId)
   {
      this.scriptTypeId = scriptTypeId;
      return (this);
   }

}
