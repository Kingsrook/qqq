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
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;


/*******************************************************************************
 **
 *******************************************************************************/
public class FieldMetaData implements ToSchema
{
   @OpenAPIExclude()
   private QFieldMetaData wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FieldMetaData(QFieldMetaData wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FieldMetaData()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Unique name for this field within its container (table or process)")
   public String getName()
   {
      return (this.wrapped.getName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("User-facing name for this field")
   public String getLabel()
   {
      return (this.wrapped.getLabel());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Data-type for this field") // todo enum
   public String getType()
   {
      return (this.wrapped.getType() == null ? null : this.wrapped.getType().name());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Indicate if a value in this field is required.")
   public Boolean getIsRequired()
   {
      return (this.wrapped.getIsRequired());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Indicate if user may edit the value in this field.")
   public Boolean getIsEditable()
   {
      return (this.wrapped.getIsEditable());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Indicate if this field should be hidden from users")
   public Boolean getIsHidden()
   {
      return (this.wrapped.getIsHidden());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Indicator of 'heavy' fields, which are not loaded by default.  e.g., some blobs or long-texts")
   public Boolean getIsHeavy()
   {
      return (this.wrapped.getIsHeavy());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("C-style format specifier for displaying values in this field.")
   public String getDisplayFormat()
   {
      return (this.wrapped.getDisplayFormat());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Default value to use in this field.")
   public String getDefaultValue()
   {
      return (this.wrapped.getDefaultValue() == null ? null : String.valueOf(this.wrapped.getDefaultValue()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("If this field's values should come from a possible value source, then that PVS is named here.")
   public String getPossibleValueSourceName()
   {
      return (this.wrapped.getPossibleValueSourceName());
   }

   // todo - PVS filter!!

   // todo - inline PVS



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("For String fields, the max length the field supports.")
   public Integer getMaxLength()
   {
      return (this.wrapped.getMaxLength());
   }

   // todo behaviors?




   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Special UI dressings to add to the field.")
   @OpenAPIListItems(value = FieldAdornment.class, useRef = true)
   public List<FieldAdornment> getAdornments()
   {
      return (this.wrapped.getAdornments() == null ? null : this.wrapped.getAdornments().stream().map(a -> new FieldAdornment(a)).toList());
   }

   // todo help content

   // todo supplemental...

}
