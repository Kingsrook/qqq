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

package com.kingsrook.qqq.middleware.javalin.specs.v1.responses.components;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIMapValueType;


/*******************************************************************************
 **
 *******************************************************************************/
public class ProcessMetaDataAdjustment
{
   @OpenAPIDescription("""
      In case the backend has changed the list of frontend steps, it will be set in this field.""")
   @OpenAPIListItems(FrontendStep.class)
   private List<FrontendStep> updatedFrontendStepList = null;

   @OpenAPIDescription("""
      Fields whose meta-data has changed.  e.g., changing a label, or required status, or inline-possible-values.""")
   @OpenAPIMapValueType(value = FieldMetaData.class, useRef = true)
   private Map<String, FieldMetaData> updatedFields = null;



   /*******************************************************************************
    ** Getter for updatedFrontendStepList
    **
    *******************************************************************************/
   public List<FrontendStep> getUpdatedFrontendStepList()
   {
      return updatedFrontendStepList;
   }



   /*******************************************************************************
    ** Setter for updatedFrontendStepList
    **
    *******************************************************************************/
   public void setUpdatedFrontendStepList(List<FrontendStep> updatedFrontendStepList)
   {
      this.updatedFrontendStepList = updatedFrontendStepList;
   }



   /*******************************************************************************
    ** Fluent setter for updatedFrontendStepList
    **
    *******************************************************************************/
   public ProcessMetaDataAdjustment withUpdatedFrontendStepList(List<FrontendStep> updatedFrontendStepList)
   {
      this.updatedFrontendStepList = updatedFrontendStepList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for updatedFields
    *******************************************************************************/
   public Map<String, FieldMetaData> getUpdatedFields()
   {
      return (this.updatedFields);
   }



   /*******************************************************************************
    ** Setter for updatedFields
    *******************************************************************************/
   public void setUpdatedFields(Map<String, FieldMetaData> updatedFields)
   {
      this.updatedFields = updatedFields;
   }



   /*******************************************************************************
    ** Fluent setter for updatedFields
    *******************************************************************************/
   public ProcessMetaDataAdjustment withUpdatedFields(Map<String, FieldMetaData> updatedFields)
   {
      this.updatedFields = updatedFields;
      return (this);
   }

}
