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


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class ImageValues implements BlockValuesInterface
{
   private String path;
   private String alt;



   /*******************************************************************************
    ** Getter for path
    *******************************************************************************/
   public String getPath()
   {
      return (this.path);
   }



   /*******************************************************************************
    ** Setter for path
    *******************************************************************************/
   public void setPath(String path)
   {
      this.path = path;
   }



   /*******************************************************************************
    ** Fluent setter for path
    *******************************************************************************/
   public ImageValues withPath(String path)
   {
      this.path = path;
      return (this);
   }



   /*******************************************************************************
    ** Getter for alt
    *******************************************************************************/
   public String getAlt()
   {
      return (this.alt);
   }



   /*******************************************************************************
    ** Setter for alt
    *******************************************************************************/
   public void setAlt(String alt)
   {
      this.alt = alt;
   }



   /*******************************************************************************
    ** Fluent setter for alt
    *******************************************************************************/
   public ImageValues withAlt(String alt)
   {
      this.alt = alt;
      return (this);
   }

}
