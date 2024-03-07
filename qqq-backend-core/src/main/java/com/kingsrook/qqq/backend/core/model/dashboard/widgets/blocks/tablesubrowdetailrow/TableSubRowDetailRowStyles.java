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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.tablesubrowdetailrow;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockStylesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class TableSubRowDetailRowStyles implements BlockStylesInterface
{
   private String labelColor;
   private String valueColor;



   /*******************************************************************************
    ** Getter for labelColor
    *******************************************************************************/
   public String getLabelColor()
   {
      return (this.labelColor);
   }



   /*******************************************************************************
    ** Setter for labelColor
    *******************************************************************************/
   public void setLabelColor(String labelColor)
   {
      this.labelColor = labelColor;
   }



   /*******************************************************************************
    ** Fluent setter for labelColor
    *******************************************************************************/
   public TableSubRowDetailRowStyles withLabelColor(String labelColor)
   {
      this.labelColor = labelColor;
      return (this);
   }



   /*******************************************************************************
    ** Getter for valueColor
    *******************************************************************************/
   public String getValueColor()
   {
      return (this.valueColor);
   }



   /*******************************************************************************
    ** Setter for valueColor
    *******************************************************************************/
   public void setValueColor(String valueColor)
   {
      this.valueColor = valueColor;
   }



   /*******************************************************************************
    ** Fluent setter for valueColor
    *******************************************************************************/
   public TableSubRowDetailRowStyles withValueColor(String valueColor)
   {
      this.valueColor = valueColor;
      return (this);
   }

}
