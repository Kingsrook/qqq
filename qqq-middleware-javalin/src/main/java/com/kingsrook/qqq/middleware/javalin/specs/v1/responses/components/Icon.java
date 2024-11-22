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


import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;


/***************************************************************************
 **
 ***************************************************************************/
public class Icon implements ToSchema
{
   @OpenAPIExclude()
   private QIcon wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public Icon(QIcon wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public Icon()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("A material UI icon name.")
   public String getName()
   {
      return (this.wrapped.getName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("A color code to use for displaying the icon")
   public String getColor()
   {
      return (this.wrapped.getColor());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("A path to an image file that can be requested from the server, to serve as the icon image instead of a material UI icon.")
   public String getPath()
   {
      return (this.wrapped.getPath());
   }
}
