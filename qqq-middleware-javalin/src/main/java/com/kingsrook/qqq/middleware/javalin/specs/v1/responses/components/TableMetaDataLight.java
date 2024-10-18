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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;


/***************************************************************************
 **
 ***************************************************************************/
public class TableMetaDataLight implements ToSchema
{
   @OpenAPIExclude()
   private QFrontendTableMetaData wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TableMetaDataLight(QFrontendTableMetaData wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TableMetaDataLight()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Unique name for this table within the QQQ Instance")
   public String getName()
   {
      return (this.wrapped.getName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("User-facing name for this table")
   public String getLabel()
   {
      return (this.wrapped.getLabel());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Boolean indicator of whether the table should be shown to users or not")
   public Boolean getIsHidden()
   {
      return (this.wrapped.getIsHidden());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Name of an icon for the table, from the material UI icon set")
   public String getIconName()
   {
      return (this.wrapped.getIconName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("List of strings describing actions that are supported by the backend application for the table.")
   @OpenAPIListItems(value = String.class) // todo - better, enum
   public List<String> getCapabilities()
   {
      return (new ArrayList<>(this.wrapped.getCapabilities()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Boolean to indicate if the user has read permission for the table.")
   public Boolean getReadPermission()
   {
      return (this.wrapped.getReadPermission());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Boolean to indicate if the user has insert permission for the table.")
   public Boolean getInsertPermission()
   {
      return (this.wrapped.getInsertPermission());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Boolean to indicate if the user has edit permission for the table.")
   public Boolean getEditPermission()
   {
      return (this.wrapped.getEditPermission());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Boolean to indicate if the user has delete permission for the table.")
   public Boolean getDeletePermission()
   {
      return (this.wrapped.getDeletePermission());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("If the table uses variants, this is the user-facing label for the table that supplies variants for this table.")
   public String getVariantTableLabel()
   {
      return (this.wrapped.getVariantTableLabel());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Help Contents for this table.") // todo describe more
   public Map<String, List<QHelpContent>> getHelpContents()
   {
      return (this.wrapped.getHelpContents());
   }

}
