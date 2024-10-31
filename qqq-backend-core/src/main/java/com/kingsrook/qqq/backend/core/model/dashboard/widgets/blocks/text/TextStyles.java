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
   private String color;
   private String format;
   private String weight;
   private String size;



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



   /***************************************************************************
    **
    ***************************************************************************/
   public enum StandardFormat
   {
      DEFAULT,
      ALERT,
      BANNER
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public enum StandardSize
   {
      LARGEST,
      HEADLINE,
      TITLE,
      BODY,
      SMALLEST
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public enum StandardWeight
   {
      EXTRA_LIGHT("extralight"),
      THIN("thin"),
      MEDIUM("medium"),
      SEMI_BOLD("semibold"),
      BLACK("black"),
      BOLD("bold"),
      EXTRA_BOLD("extrabold"),
      W100("100"),
      W200("200"),
      W300("300"),
      W400("400"),
      W500("500"),
      W600("600"),
      W700("700"),
      W800("800"),
      W900("900");

      private final String value;



      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      StandardWeight(String value)
      {
         this.value = value;
      }



      /*******************************************************************************
       ** Getter for value
       **
       *******************************************************************************/
      public String getValue()
      {
         return value;
      }
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
      setColor(standardColor);
   }



   /*******************************************************************************
    ** Getter for format
    *******************************************************************************/
   public String getFormat()
   {
      return (this.format);
   }



   /*******************************************************************************
    ** Setter for format
    *******************************************************************************/
   public void setFormat(String format)
   {
      this.format = format;
   }



   /*******************************************************************************
    ** Fluent setter for format
    *******************************************************************************/
   public TextStyles withFormat(String format)
   {
      this.format = format;
      return (this);
   }



   /*******************************************************************************
    ** Setter for format
    *******************************************************************************/
   public void setFormat(StandardFormat format)
   {
      this.format = format == null ? null : format.name().toLowerCase();
   }



   /*******************************************************************************
    ** Fluent setter for format
    *******************************************************************************/
   public TextStyles withFormat(StandardFormat format)
   {
      this.setFormat(format);
      return (this);
   }



   /*******************************************************************************
    ** Getter for weight
    *******************************************************************************/
   public String getWeight()
   {
      return (this.weight);
   }



   /*******************************************************************************
    ** Setter for weight
    *******************************************************************************/
   public void setWeight(String weight)
   {
      this.weight = weight;
   }



   /*******************************************************************************
    ** Fluent setter for weight
    *******************************************************************************/
   public TextStyles withWeight(String weight)
   {
      this.weight = weight;
      return (this);
   }



   /*******************************************************************************
    ** Setter for weight
    *******************************************************************************/
   public void setWeight(StandardWeight weight)
   {
      setWeight(weight == null ? null : weight.getValue());
   }



   /*******************************************************************************
    ** Fluent setter for weight
    *******************************************************************************/
   public TextStyles withWeight(StandardWeight weight)
   {
      setWeight(weight);
      return (this);
   }



   /*******************************************************************************
    ** Getter for size
    *******************************************************************************/
   public String getSize()
   {
      return (this.size);
   }



   /*******************************************************************************
    ** Setter for size
    *******************************************************************************/
   public void setSize(String size)
   {
      this.size = size;
   }



   /*******************************************************************************
    ** Fluent setter for size
    *******************************************************************************/
   public TextStyles withSize(String size)
   {
      this.size = size;
      return (this);
   }


   /*******************************************************************************
    ** Setter for size
    *******************************************************************************/
   public void setSize(StandardSize size)
   {
      this.size = (size == null ? null : size.name().toLowerCase());
   }



   /*******************************************************************************
    ** Fluent setter for size
    *******************************************************************************/
   public TextStyles withSize(StandardSize size)
   {
      setSize(size);
      return (this);
   }



   /*******************************************************************************
    ** Getter for color
    *******************************************************************************/
   public String getColor()
   {
      return (this.color);
   }



   /*******************************************************************************
    ** Setter for color
    *******************************************************************************/
   public void setColor(String color)
   {
      this.color = color;
   }



   /*******************************************************************************
    ** Fluent setter for color
    *******************************************************************************/
   public TextStyles withColor(String color)
   {
      this.color = color;
      return (this);
   }



   /*******************************************************************************
    ** Setter for color
    *******************************************************************************/
   public void setColor(StandardColor color)
   {
      this.color = color == null ? null : color.name();
   }



   /*******************************************************************************
    ** Fluent setter for color
    *******************************************************************************/
   public TextStyles withColor(StandardColor color)
   {
      setColor(color);
      return (this);
   }

}
