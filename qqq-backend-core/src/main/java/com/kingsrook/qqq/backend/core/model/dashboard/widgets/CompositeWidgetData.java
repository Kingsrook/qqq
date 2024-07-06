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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.AbstractBlockWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.base.BaseSlots;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.base.BaseStyles;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.base.BaseValues;


/*******************************************************************************
 ** Data used to render a Composite Widget - e.g., a collection of blocks
 *******************************************************************************/
public class CompositeWidgetData extends AbstractBlockWidgetData<CompositeWidgetData, BaseValues, BaseSlots, BaseStyles>
{
   private List<AbstractBlockWidgetData<?, ?, ?, ?>> blocks = new ArrayList<>();

   private Map<String, Serializable> styleOverrides = new HashMap<>();

   private Layout layout;



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum Layout
   {
      /////////////////////////////////////////////////////////////
      // note, these are used in QQQ FMD CompositeWidgetData.tsx //
      /////////////////////////////////////////////////////////////
      FLEX_COLUMN,
      FLEX_ROW_WRAPPED,
      FLEX_ROW_SPACE_BETWEEN,
      TABLE_SUB_ROW_DETAILS,
      BADGES_WRAPPER
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getBlockTypeName()
   {
      return "COMPOSITE";
   }



   /*******************************************************************************
    ** Getter for blocks
    **
    *******************************************************************************/
   public List<AbstractBlockWidgetData<?, ?, ?, ?>> getBlocks()
   {
      return blocks;
   }



   /*******************************************************************************
    ** Setter for blocks
    **
    *******************************************************************************/
   public void setBlocks(List<AbstractBlockWidgetData<?, ?, ?, ?>> blocks)
   {
      this.blocks = blocks;
   }



   /*******************************************************************************
    ** Fluent setter for blocks
    **
    *******************************************************************************/
   public CompositeWidgetData withBlocks(List<AbstractBlockWidgetData<?, ?, ?, ?>> blocks)
   {
      this.blocks = blocks;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public CompositeWidgetData withBlock(AbstractBlockWidgetData<?, ?, ?, ?> block)
   {
      addBlock(block);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addBlock(AbstractBlockWidgetData<?, ?, ?, ?> block)
   {
      if(this.blocks == null)
      {
         this.blocks = new ArrayList<>();
      }
      this.blocks.add(block);
   }



   /*******************************************************************************
    ** Getter for styleOverrides
    *******************************************************************************/
   public Map<String, Serializable> getStyleOverrides()
   {
      return (this.styleOverrides);
   }



   /*******************************************************************************
    ** Setter for styleOverrides
    *******************************************************************************/
   public void setStyleOverrides(Map<String, Serializable> styleOverrides)
   {
      this.styleOverrides = styleOverrides;
   }



   /*******************************************************************************
    ** Fluent setter for styleOverrides
    *******************************************************************************/
   public CompositeWidgetData withStyleOverrides(Map<String, Serializable> styleOverrides)
   {
      this.styleOverrides = styleOverrides;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public CompositeWidgetData withStyleOverride(String key, Serializable value)
   {
      addStyleOverride(key, value);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addStyleOverride(String key, Serializable value)
   {
      if(this.styleOverrides == null)
      {
         this.styleOverrides = new HashMap<>();
      }
      this.styleOverrides.put(key, value);
   }



   /*******************************************************************************
    ** Getter for layout
    *******************************************************************************/
   public Layout getLayout()
   {
      return (this.layout);
   }



   /*******************************************************************************
    ** Setter for layout
    *******************************************************************************/
   public void setLayout(Layout layout)
   {
      this.layout = layout;
   }



   /*******************************************************************************
    ** Fluent setter for layout
    *******************************************************************************/
   public CompositeWidgetData withLayout(Layout layout)
   {
      this.layout = layout;
      return (this);
   }

}
