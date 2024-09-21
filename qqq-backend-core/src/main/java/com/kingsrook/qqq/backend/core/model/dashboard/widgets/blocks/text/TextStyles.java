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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.text;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockStylesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class TextStyles implements BlockStylesInterface
{
   private StandardColor standardColor;

   private boolean isAlert;



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



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TextStyles()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public TextStyles(StandardColor standardColor)
   {
      setStandardColor(standardColor);
   }



   /*******************************************************************************
    ** Getter for standardColor
    *******************************************************************************/
   public StandardColor getStandardColor()
   {
      return (this.standardColor);
   }



   /*******************************************************************************
    ** Setter for standardColor
    *******************************************************************************/
   public void setStandardColor(StandardColor standardColor)
   {
      this.standardColor = standardColor;
   }



   /*******************************************************************************
    ** Fluent setter for standardColor
    *******************************************************************************/
   public TextStyles withStandardColor(StandardColor standardColor)
   {
      this.standardColor = standardColor;
      return (this);
   }


   /*******************************************************************************
    ** Getter for isAlert
    *******************************************************************************/
   public boolean getIsAlert()
   {
      return (this.isAlert);
   }



   /*******************************************************************************
    ** Setter for isAlert
    *******************************************************************************/
   public void setIsAlert(boolean isAlert)
   {
      this.isAlert = isAlert;
   }



   /*******************************************************************************
    ** Fluent setter for isAlert
    *******************************************************************************/
   public TextStyles withIsAlert(boolean isAlert)
   {
      this.isAlert = isAlert;
      return (this);
   }


}
