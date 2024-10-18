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
   @OpenAPIDescription("Indicate if the text should be displayed as an alert (e.g., modal popup)")
   public Boolean getIsAlert()
   {
      return (this.wrapped.getIsAlert());
   }


   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("A Standard Color to display the text in (e.g., not a hex or RGB code).")
   public TextStyles.StandardColor getStandardColor()
   {
      return (this.wrapped.getStandardColor());
   }

}
