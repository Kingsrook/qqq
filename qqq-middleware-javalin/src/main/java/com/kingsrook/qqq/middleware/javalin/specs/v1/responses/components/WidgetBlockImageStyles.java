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


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.base.BaseStyles;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.image.ImageStyles;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;


/*******************************************************************************
 **
 *******************************************************************************/
public final class WidgetBlockImageStyles implements WidgetBlockStyles
{
   @OpenAPIExclude()
   private ImageStyles wrapped;



   /***************************************************************************
    **
    ***************************************************************************/
   public WidgetBlockImageStyles(ImageStyles imageStyles)
   {
      this.wrapped = imageStyles;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WidgetBlockImageStyles()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("A request to render the image at a specified width.")
   public String getWidth()
   {
      return (this.wrapped.getWidth());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("A request to render the image at a specified height.")
   public String getHeight()
   {
      return (this.wrapped.getHeight());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Optional padding to apply to the image")
   public BaseStyles.Directional<String> getPadding()
   {
      return (this.wrapped.getPadding());
   }

}
