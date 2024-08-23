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


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.CompositeWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.QWidgetData;


/*******************************************************************************
 ** Base class for the data returned in rendering a block of a specific type.
 **
 ** The type parameters define the structure of the block's data, and should
 ** generally be defined along with a sub-class of this class, in a block-specific
 ** sub-package.
 *******************************************************************************/
public abstract class AbstractBlockWidgetData<
   T extends AbstractBlockWidgetData<T, V, S, SX>,
   V extends BlockValuesInterface,
   S extends BlockSlotsInterface,
   SX extends BlockStylesInterface> extends QWidgetData
{
   private String blockId;

   private BlockTooltip tooltip;
   private BlockLink    link;

   private Map<S, BlockTooltip> tooltipMap;
   private Map<S, BlockLink>    linkMap;

   private V  values;
   private SX styles;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public final String getType()
   {
      return "block";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract String getBlockTypeName();



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withTooltip(S key, String value)
   {
      addTooltip(key, value);
      return (T) (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addTooltip(S key, String value)
   {
      if(this.tooltipMap == null)
      {
         this.tooltipMap = new HashMap<>();
      }
      this.tooltipMap.put(key, new BlockTooltip().withTitle(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withTooltip(S key, BlockTooltip value)
   {
      addTooltip(key, value);
      return (T) (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addTooltip(S key, BlockTooltip value)
   {
      if(this.tooltipMap == null)
      {
         this.tooltipMap = new HashMap<>();
      }
      this.tooltipMap.put(key, value);
   }



   /*******************************************************************************
    ** Getter for tooltipMap
    *******************************************************************************/
   public Map<S, BlockTooltip> getTooltipMap()
   {
      return (this.tooltipMap);
   }



   /*******************************************************************************
    ** Setter for tooltipMap
    *******************************************************************************/
   public void setTooltipMap(Map<S, BlockTooltip> tooltipMap)
   {
      this.tooltipMap = tooltipMap;
   }



   /*******************************************************************************
    ** Fluent setter for tooltipMap
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withTooltipMap(Map<S, BlockTooltip> tooltipMap)
   {
      this.tooltipMap = tooltipMap;
      return (T) (this);
   }



   /*******************************************************************************
    ** Getter for tooltip
    **
    *******************************************************************************/
   public BlockTooltip getTooltip()
   {
      return tooltip;
   }



   /*******************************************************************************
    ** Setter for tooltip
    **
    *******************************************************************************/
   public void setTooltip(BlockTooltip tooltip)
   {
      this.tooltip = tooltip;
   }



   /*******************************************************************************
    ** Fluent setter for tooltip
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withTooltip(String tooltip)
   {
      this.tooltip = new BlockTooltip(tooltip);
      return (T) (this);
   }



   /*******************************************************************************
    ** Fluent setter for tooltip
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withTooltip(BlockTooltip tooltip)
   {
      this.tooltip = tooltip;
      return (T) (this);
   }



   /*******************************************************************************
    ** Fluent setter for tooltip
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withTooltip(CompositeWidgetData data)
   {
      this.tooltip = new BlockTooltip(data);
      return (T) (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withLink(S key, String value)
   {
      addLink(key, value);
      return (T) (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addLink(S key, String value)
   {
      if(this.linkMap == null)
      {
         this.linkMap = new HashMap<>();
      }
      this.linkMap.put(key, new BlockLink(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withLink(S key, BlockLink value)
   {
      addLink(key, value);
      return (T) (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addLink(S key, BlockLink value)
   {
      if(this.linkMap == null)
      {
         this.linkMap = new HashMap<>();
      }
      this.linkMap.put(key, value);
   }



   /*******************************************************************************
    ** Getter for linkMap
    *******************************************************************************/
   public Map<S, BlockLink> getLinkMap()
   {
      return (this.linkMap);
   }



   /*******************************************************************************
    ** Setter for linkMap
    *******************************************************************************/
   public void setLinkMap(Map<S, BlockLink> linkMap)
   {
      this.linkMap = linkMap;
   }



   /*******************************************************************************
    ** Fluent setter for linkMap
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withLinkMap(Map<S, BlockLink> linkMap)
   {
      this.linkMap = linkMap;
      return (T) (this);
   }



   /*******************************************************************************
    ** Getter for link
    **
    *******************************************************************************/
   public BlockLink getLink()
   {
      return link;
   }



   /*******************************************************************************
    ** Setter for link
    **
    *******************************************************************************/
   public void setLink(BlockLink link)
   {
      this.link = link;
   }



   /*******************************************************************************
    ** Fluent setter for link
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withLink(String link)
   {
      this.link = new BlockLink(link);
      return (T) (this);
   }



   /*******************************************************************************
    ** Fluent setter for link
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withLink(BlockLink link)
   {
      this.link = link;
      return (T) this;
   }



   /*******************************************************************************
    ** Getter for values
    *******************************************************************************/
   public V getValues()
   {
      return (this.values);
   }



   /*******************************************************************************
    ** Setter for values
    *******************************************************************************/
   public void setValues(V values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Fluent setter for values
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withValues(V values)
   {
      this.values = values;
      return (T) this;
   }



   /*******************************************************************************
    ** Getter for styles
    *******************************************************************************/
   public SX getStyles()
   {
      return (this.styles);
   }



   /*******************************************************************************
    ** Setter for styles
    *******************************************************************************/
   public void setStyles(SX styles)
   {
      this.styles = styles;
   }



   /*******************************************************************************
    ** Fluent setter for styles
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withStyles(SX styles)
   {
      this.styles = styles;
      return (T) this;
   }



   /*******************************************************************************
    ** Getter for blockId
    *******************************************************************************/
   public String getBlockId()
   {
      return (this.blockId);
   }



   /*******************************************************************************
    ** Setter for blockId
    *******************************************************************************/
   public void setBlockId(String blockId)
   {
      this.blockId = blockId;
   }



   /*******************************************************************************
    ** Fluent setter for blockId
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public T withBlockId(String blockId)
   {
      this.blockId = blockId;
      return (T) this;
   }

}
