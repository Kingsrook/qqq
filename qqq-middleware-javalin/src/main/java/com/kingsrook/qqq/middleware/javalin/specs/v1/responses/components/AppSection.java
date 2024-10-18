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
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIMapKnownEntries;


/***************************************************************************
 **
 ***************************************************************************/
public class AppSection implements ToSchema
{
   @OpenAPIExclude()
   private QAppSection wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public AppSection(QAppSection wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public AppSection()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Unique (within the app) name for this section.")
   public String getName()
   {
      return (this.wrapped.getName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("User-facing name of the section.")
   public String getLabel()
   {
      return (this.wrapped.getLabel());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Icon to display for the section.")
   @OpenAPIMapKnownEntries(value = Icon.class, useRef = true)
   public Icon getIcon()
   {
      return (this.wrapped.getIcon() == null ? null : new Icon(this.wrapped.getIcon()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("List of table names for the section")
   @OpenAPIListItems(value = String.class)
   public List<String> getTables()
   {
      return (this.wrapped.getTables());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("List of process names for the section")
   @OpenAPIListItems(value = String.class)
   public List<String> getProcesses()
   {
      return (this.wrapped.getProcesses());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("List of report names for the section")
   @OpenAPIListItems(value = String.class)
   public List<String> getReports()
   {
      return (this.wrapped.getReports());
   }

}
