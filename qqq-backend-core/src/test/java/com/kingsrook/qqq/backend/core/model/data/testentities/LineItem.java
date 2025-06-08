/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.data.testentities;


import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** Sample of an entity that can be converted to & from a QRecord
 *******************************************************************************/
public class LineItem extends QRecordEntity
{
   public static final String TABLE_NAME = "lineItem";

   @QField()
   private String sku;

   @QField()
   private Integer quantity;



   /*******************************************************************************
    ** Getter for sku
    *******************************************************************************/
   public String getSku()
   {
      return (this.sku);
   }



   /*******************************************************************************
    ** Setter for sku
    *******************************************************************************/
   public void setSku(String sku)
   {
      this.sku = sku;
   }



   /*******************************************************************************
    ** Fluent setter for sku
    *******************************************************************************/
   public LineItem withSku(String sku)
   {
      this.sku = sku;
      return (this);
   }



   /*******************************************************************************
    ** Getter for quantity
    *******************************************************************************/
   public Integer getQuantity()
   {
      return (this.quantity);
   }



   /*******************************************************************************
    ** Setter for quantity
    *******************************************************************************/
   public void setQuantity(Integer quantity)
   {
      this.quantity = quantity;
   }



   /*******************************************************************************
    ** Fluent setter for quantity
    *******************************************************************************/
   public LineItem withQuantity(Integer quantity)
   {
      this.quantity = quantity;
      return (this);
   }

}