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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.image;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockStylesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class ImageStyles implements BlockStylesInterface
{
   private String  width;
   private String  height;
   private boolean bordered = false;



   /*******************************************************************************
    ** Getter for bordered
    *******************************************************************************/
   public boolean getBordered()
   {
      return (this.bordered);
   }



   /*******************************************************************************
    ** Setter for bordered
    *******************************************************************************/
   public void setBordered(boolean bordered)
   {
      this.bordered = bordered;
   }



   /*******************************************************************************
    ** Fluent setter for bordered
    *******************************************************************************/
   public ImageStyles withBordered(boolean bordered)
   {
      this.bordered = bordered;
      return (this);
   }



   /*******************************************************************************
    ** Getter for width
    *******************************************************************************/
   public String getWidth()
   {
      return (this.width);
   }



   /*******************************************************************************
    ** Setter for width
    *******************************************************************************/
   public void setWidth(String width)
   {
      this.width = width;
   }



   /*******************************************************************************
    ** Fluent setter for width
    *******************************************************************************/
   public ImageStyles withWidth(String width)
   {
      this.width = width;
      return (this);
   }



   /*******************************************************************************
    ** Getter for height
    *******************************************************************************/
   public String getHeight()
   {
      return (this.height);
   }



   /*******************************************************************************
    ** Setter for height
    *******************************************************************************/
   public void setHeight(String height)
   {
      this.height = height;
   }



   /*******************************************************************************
    ** Fluent setter for height
    *******************************************************************************/
   public ImageStyles withHeight(String height)
   {
      this.height = height;
      return (this);
   }

}
