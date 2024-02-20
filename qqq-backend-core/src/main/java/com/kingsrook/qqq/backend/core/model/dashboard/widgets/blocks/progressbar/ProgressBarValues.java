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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.progressbar;


import java.io.Serializable;
import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class ProgressBarValues implements BlockValuesInterface
{
   private String       heading;
   private BigDecimal   percent;
   private Serializable value;



   /*******************************************************************************
    ** Getter for heading
    *******************************************************************************/
   public String getHeading()
   {
      return (this.heading);
   }



   /*******************************************************************************
    ** Setter for heading
    *******************************************************************************/
   public void setHeading(String heading)
   {
      this.heading = heading;
   }



   /*******************************************************************************
    ** Fluent setter for heading
    *******************************************************************************/
   public ProgressBarValues withHeading(String heading)
   {
      this.heading = heading;
      return (this);
   }



   /*******************************************************************************
    ** Getter for percent
    *******************************************************************************/
   public BigDecimal getPercent()
   {
      return (this.percent);
   }



   /*******************************************************************************
    ** Setter for percent
    *******************************************************************************/
   public void setPercent(BigDecimal percent)
   {
      this.percent = percent;
   }



   /*******************************************************************************
    ** Fluent setter for percent
    *******************************************************************************/
   public ProgressBarValues withPercent(BigDecimal percent)
   {
      this.percent = percent;
      return (this);
   }



   /*******************************************************************************
    ** Getter for value
    *******************************************************************************/
   public Serializable getValue()
   {
      return (this.value);
   }



   /*******************************************************************************
    ** Setter for value
    *******************************************************************************/
   public void setValue(Serializable value)
   {
      this.value = value;
   }



   /*******************************************************************************
    ** Fluent setter for value
    *******************************************************************************/
   public ProgressBarValues withValue(Serializable value)
   {
      this.value = value;
      return (this);
   }
}
