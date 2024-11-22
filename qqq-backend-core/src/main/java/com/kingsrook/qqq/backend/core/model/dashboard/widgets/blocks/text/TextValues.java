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


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;


/*******************************************************************************
 **
 *******************************************************************************/
public class TextValues implements BlockValuesInterface
{
   private String text;

   private QIcon startIcon;
   private QIcon endIcon;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TextValues()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TextValues(String text)
   {
      setText(text);
   }



   /*******************************************************************************
    ** Getter for text
    *******************************************************************************/
   public String getText()
   {
      return (this.text);
   }



   /*******************************************************************************
    ** Setter for text
    *******************************************************************************/
   public void setText(String text)
   {
      this.text = text;
   }



   /*******************************************************************************
    ** Fluent setter for text
    *******************************************************************************/
   public TextValues withText(String text)
   {
      this.text = text;
      return (this);
   }


   /*******************************************************************************
    ** Getter for startIcon
    *******************************************************************************/
   public QIcon getStartIcon()
   {
      return (this.startIcon);
   }



   /*******************************************************************************
    ** Setter for startIcon
    *******************************************************************************/
   public void setStartIcon(QIcon startIcon)
   {
      this.startIcon = startIcon;
   }



   /*******************************************************************************
    ** Fluent setter for startIcon
    *******************************************************************************/
   public TextValues withStartIcon(QIcon startIcon)
   {
      this.startIcon = startIcon;
      return (this);
   }



   /*******************************************************************************
    ** Getter for endIcon
    *******************************************************************************/
   public QIcon getEndIcon()
   {
      return (this.endIcon);
   }



   /*******************************************************************************
    ** Setter for endIcon
    *******************************************************************************/
   public void setEndIcon(QIcon endIcon)
   {
      this.endIcon = endIcon;
   }



   /*******************************************************************************
    ** Fluent setter for endIcon
    *******************************************************************************/
   public TextValues withEndIcon(QIcon endIcon)
   {
      this.endIcon = endIcon;
      return (this);
   }


}
