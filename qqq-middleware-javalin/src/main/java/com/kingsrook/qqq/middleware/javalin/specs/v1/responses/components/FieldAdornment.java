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


import java.io.Serializable;
import java.util.EnumSet;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIEnumSubSet;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;


/*******************************************************************************
 **
 *******************************************************************************/
public class FieldAdornment implements ToSchema
{
   @OpenAPIExclude()
   private com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FieldAdornment(com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FieldAdornment()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class FieldAdornmentSubSet implements OpenAPIEnumSubSet.EnumSubSet<AdornmentType>
   {
      private static EnumSet<AdornmentType> subSet = null;



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public EnumSet<AdornmentType> getSubSet()
      {
         if(subSet == null)
         {
            EnumSet<AdornmentType> subSet = EnumSet.allOf(AdornmentType.class);
            subSet.remove(AdornmentType.FILE_UPLOAD); // todo - remove for next version!
            FieldAdornmentSubSet.subSet = subSet;
         }

         return (subSet);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Type of this adornment")
   @OpenAPIEnumSubSet(FieldAdornmentSubSet.class)
   public AdornmentType getType()
   {
      return (this.wrapped == null || this.wrapped.getType() == null ? null : this.wrapped.getType());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Values associated with this adornment.  Keys and the meanings of their values will differ by type.")
   public Map<String, Serializable> getValues()
   {
      return (this.wrapped.getValues());
   }

}
