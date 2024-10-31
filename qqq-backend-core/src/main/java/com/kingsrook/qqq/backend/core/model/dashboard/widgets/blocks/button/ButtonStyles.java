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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.button;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockStylesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class ButtonStyles implements BlockStylesInterface
{
   private String color;
   private String format;



   /***************************************************************************
    **
    ***************************************************************************/
   public enum StandardColor
   {
      SUCCESS,
      WARNING,
      ERROR,
      INFO,
      MUTED
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public enum StandardFormat
   {
      OUTLINED,
      FILLED,
      TEXT
   }



   /*******************************************************************************
    ** Getter for color
    *******************************************************************************/
   public String getColor()
   {
      return (this.color);
   }



   /*******************************************************************************
    ** Setter for color
    *******************************************************************************/
   public void setColor(String color)
   {
      this.color = color;
   }



   /*******************************************************************************
    ** Fluent setter for color
    *******************************************************************************/
   public ButtonStyles withColor(String color)
   {
      this.color = color;
      return (this);
   }



   /*******************************************************************************
    ** Getter for format
    *******************************************************************************/
   public String getFormat()
   {
      return (this.format);
   }



   /*******************************************************************************
    ** Setter for format
    *******************************************************************************/
   public void setFormat(String format)
   {
      this.format = format;
   }



   /*******************************************************************************
    ** Fluent setter for format
    *******************************************************************************/
   public ButtonStyles withFormat(String format)
   {
      this.format = format;
      return (this);
   }

   /*******************************************************************************
    ** Setter for format
    *******************************************************************************/
   public void setFormat(StandardFormat format)
   {
      this.format = (format == null ? null : format.name().toLowerCase());
   }



   /*******************************************************************************
    ** Fluent setter for format
    *******************************************************************************/
   public ButtonStyles withFormat(StandardFormat format)
   {
      setFormat(format);
      return (this);
   }

}
