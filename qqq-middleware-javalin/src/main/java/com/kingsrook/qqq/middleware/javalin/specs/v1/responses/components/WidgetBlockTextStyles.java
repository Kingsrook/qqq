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


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.text.TextStyles;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;


/*******************************************************************************
 **
 *******************************************************************************/
public final class WidgetBlockTextStyles implements WidgetBlockStyles
{
   @OpenAPIExclude()
   private TextStyles wrapped;



   /***************************************************************************
    **
    ***************************************************************************/
   public WidgetBlockTextStyles(TextStyles textStyles)
   {
      this.wrapped = textStyles;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WidgetBlockTextStyles()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("A Color to display the text in.  May be specified as a StandardColor (one of: "
                       + "SUCCESS, WARNING, ERROR, INFO, MUTED) or an RGB code.")
   public String getColor()
   {
      return (this.wrapped.getColor());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("An optional indicator of the screen format preferred by the application to be used for this block.  "
                       + "Different frontends may support different formats, and implement them differently.")
   public String getFormat()
   {
      return (this.wrapped.getFormat());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("An optional indicator of the weight at which the text should be rendered.  May be a named value (one of"
                       + "extralight, thin, medium, black, semibold, bold, extrabold) or a numeric, e.g., 100, 200, ..., 900")
   public String getWeight()
   {
      return (this.wrapped.getWeight());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("An optional indicator of the size at which the text should be rendered.  May be a named value (one of"
                       + "largest, headline, title, body, smallest) or a numeric size - both are up to the frontend to interpret.")
   public String getSize()
   {
      return (this.wrapped.getSize());
   }

}
