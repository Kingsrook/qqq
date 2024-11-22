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


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.button.ButtonValues;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;


/*******************************************************************************
 **
 *******************************************************************************/
@OpenAPIDescription("Values used for a BUTTON type widget block")
public final class WidgetBlockButtonValues implements WidgetBlockValues
{
   @OpenAPIExclude()
   private ButtonValues wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WidgetBlockButtonValues(ButtonValues buttonValues)
   {
      this.wrapped = buttonValues;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WidgetBlockButtonValues()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("User-facing label to display in the button")
   public String getLabel()
   {
      return (this.wrapped.getLabel());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Code used within the app as the value submitted when the button is clicked")
   public String getActionCode()
   {
      return (this.wrapped.getActionCode());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("""
      Instructions for what should happen in the frontend (e.g., within a screen), when the button is clicked.
      
      To show a modal composite block, use format:  `showModal:${blockId}` (e.g., `showModal:myBlock`)
      
      To hide a modal composite block, use format:  `hideModal:${blockId}` (e.g., `hideModal:myBlock`)
      
      To toggle visibility of a modal composite block, use format:  `toggleModal:${blockId}` (e.g., `toggleModal:myBlock`)
      """)
   public String getControlCode()
   {
      return (this.wrapped.getControlCode());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("An optional icon to display before the text in the button")
   public Icon getStartIcon()
   {
      return (this.wrapped.getStartIcon() == null ? null : new Icon(this.wrapped.getStartIcon()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("An optional icon to display after the text in the button")
   public Icon getEndIcon()
   {
      return (this.wrapped.getEndIcon() == null ? null : new Icon(this.wrapped.getEndIcon()));
   }

}
