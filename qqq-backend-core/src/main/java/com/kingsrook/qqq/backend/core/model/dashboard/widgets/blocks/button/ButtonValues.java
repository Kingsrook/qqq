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


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;


/*******************************************************************************
 **
 *******************************************************************************/
public class ButtonValues implements BlockValuesInterface
{
   private String label;
   private String actionCode;
   private String controlCode;

   private QIcon startIcon;
   private QIcon endIcon;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ButtonValues()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ButtonValues(String label, String actionCode)
   {
      setLabel(label);
      setActionCode(actionCode);
   }



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public ButtonValues withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for actionCode
    *******************************************************************************/
   public String getActionCode()
   {
      return (this.actionCode);
   }



   /*******************************************************************************
    ** Setter for actionCode
    *******************************************************************************/
   public void setActionCode(String actionCode)
   {
      this.actionCode = actionCode;
   }



   /*******************************************************************************
    ** Fluent setter for actionCode
    *******************************************************************************/
   public ButtonValues withActionCode(String actionCode)
   {
      this.actionCode = actionCode;
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
   public ButtonValues withStartIcon(QIcon startIcon)
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
   public ButtonValues withEndIcon(QIcon endIcon)
   {
      this.endIcon = endIcon;
      return (this);
   }



   /*******************************************************************************
    ** Getter for controlCode
    *******************************************************************************/
   public String getControlCode()
   {
      return (this.controlCode);
   }



   /*******************************************************************************
    ** Setter for controlCode
    *******************************************************************************/
   public void setControlCode(String controlCode)
   {
      this.controlCode = controlCode;
   }



   /*******************************************************************************
    ** Fluent setter for controlCode
    *******************************************************************************/
   public ButtonValues withControlCode(String controlCode)
   {
      this.controlCode = controlCode;
      return (this);
   }

}
