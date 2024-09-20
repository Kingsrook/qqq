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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.actionbutton;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class ActionButtonValues implements BlockValuesInterface
{
   private String label;
   private String actionCode;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ActionButtonValues()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ActionButtonValues(String label, String actionCode)
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
   public ActionButtonValues withLabel(String label)
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
   public ActionButtonValues withActionCode(String actionCode)
   {
      this.actionCode = actionCode;
      return (this);
   }

}
