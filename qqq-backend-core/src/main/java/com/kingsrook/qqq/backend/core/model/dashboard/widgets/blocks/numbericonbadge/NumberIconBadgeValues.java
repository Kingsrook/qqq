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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.numbericonbadge;


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class NumberIconBadgeValues implements BlockValuesInterface
{
   private Serializable number;
   private String       iconName;



   /*******************************************************************************
    ** Getter for number
    *******************************************************************************/
   public Serializable getNumber()
   {
      return (this.number);
   }



   /*******************************************************************************
    ** Setter for number
    *******************************************************************************/
   public void setNumber(Serializable number)
   {
      this.number = number;
   }



   /*******************************************************************************
    ** Fluent setter for number
    *******************************************************************************/
   public NumberIconBadgeValues withNumber(Serializable number)
   {
      this.number = number;
      return (this);
   }



   /*******************************************************************************
    ** Getter for iconName
    *******************************************************************************/
   public String getIconName()
   {
      return (this.iconName);
   }



   /*******************************************************************************
    ** Setter for iconName
    *******************************************************************************/
   public void setIconName(String iconName)
   {
      this.iconName = iconName;
   }



   /*******************************************************************************
    ** Fluent setter for iconName
    *******************************************************************************/
   public NumberIconBadgeValues withIconName(String iconName)
   {
      this.iconName = iconName;
      return (this);
   }

}
