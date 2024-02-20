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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.bignumberblock;


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class BigNumberValues implements BlockValuesInterface
{
   private String       heading;
   private Serializable number;
   private String       context;



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
   public BigNumberValues withHeading(String heading)
   {
      this.heading = heading;
      return (this);
   }



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
   public BigNumberValues withNumber(Serializable number)
   {
      this.number = number;
      return (this);
   }



   /*******************************************************************************
    ** Getter for context
    *******************************************************************************/
   public String getContext()
   {
      return (this.context);
   }



   /*******************************************************************************
    ** Setter for context
    *******************************************************************************/
   public void setContext(String context)
   {
      this.context = context;
   }



   /*******************************************************************************
    ** Fluent setter for context
    *******************************************************************************/
   public BigNumberValues withContext(String context)
   {
      this.context = context;
      return (this);
   }

}
