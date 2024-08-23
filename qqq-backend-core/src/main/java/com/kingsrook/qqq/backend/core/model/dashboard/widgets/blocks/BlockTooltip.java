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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.CompositeWidgetData;


/*******************************************************************************
 ** A tooltip used within a (widget) block.
 **
 *******************************************************************************/
public class BlockTooltip
{
   private CompositeWidgetData blockData;
   private String              title;
   private Placement           placement = Placement.BOTTOM;



   /***************************************************************************
    **
    ***************************************************************************/
   public enum Placement
   {BOTTOM, LEFT, RIGHT, TOP}



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BlockTooltip()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BlockTooltip(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BlockTooltip(CompositeWidgetData blockData)
   {
      this.blockData = blockData;
   }



   /*******************************************************************************
    ** Getter for title
    *******************************************************************************/
   public String getTitle()
   {
      return (this.title);
   }



   /*******************************************************************************
    ** Setter for title
    *******************************************************************************/
   public void setTitle(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Fluent setter for title
    *******************************************************************************/
   public BlockTooltip withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for placement
    *******************************************************************************/
   public Placement getPlacement()
   {
      return (this.placement);
   }



   /*******************************************************************************
    ** Setter for placement
    *******************************************************************************/
   public void setPlacement(Placement placement)
   {
      this.placement = placement;
   }



   /*******************************************************************************
    ** Fluent setter for placement
    *******************************************************************************/
   public BlockTooltip withPlacement(Placement placement)
   {
      this.placement = placement;
      return (this);
   }



   /*******************************************************************************
    ** Getter for blockData
    *******************************************************************************/
   public CompositeWidgetData getBlockData()
   {
      return (this.blockData);
   }



   /*******************************************************************************
    ** Setter for blockData
    *******************************************************************************/
   public void setBlockData(CompositeWidgetData blockData)
   {
      this.blockData = blockData;
   }



   /*******************************************************************************
    ** Fluent setter for blockData
    *******************************************************************************/
   public BlockTooltip withBlockData(CompositeWidgetData blockData)
   {
      this.blockData = blockData;
      return (this);
   }

}
