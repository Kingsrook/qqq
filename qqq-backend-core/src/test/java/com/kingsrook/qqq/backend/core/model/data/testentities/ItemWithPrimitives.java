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


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** Sample of an entity that can be converted to & from a QRecord
 *******************************************************************************/
public class ItemWithPrimitives extends QRecordEntity
{
   @QField()
   private String sku;
  
   @QField()
   private String description;

   @QField()
   private int quantity;

   @QField()
   private BigDecimal price;

   @QField()
   private boolean featured;



   /*******************************************************************************
    ** Getter for sku
    **
    *******************************************************************************/
   public String getSku()
   {
      return sku;
   }



   /*******************************************************************************
    ** Setter for sku
    **
    *******************************************************************************/
   public void setSku(String sku)
   {
      this.sku = sku;
   }



   /*******************************************************************************
    ** Getter for description
    **
    *******************************************************************************/
   public String getDescription()
   {
      return description;
   }



   /*******************************************************************************
    ** Setter for description
    **
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Getter for quantity
    **
    *******************************************************************************/
   public int getQuantity()
   {
      return quantity;
   }



   /*******************************************************************************
    ** Setter for quantity
    **
    *******************************************************************************/
   public void setQuantity(int quantity)
   {
      this.quantity = quantity;
   }



   /*******************************************************************************
    ** Getter for price
    **
    *******************************************************************************/
   public BigDecimal getPrice()
   {
      return price;
   }



   /*******************************************************************************
    ** Setter for price
    **
    *******************************************************************************/
   public void setPrice(BigDecimal price)
   {
      this.price = price;
   }



   /*******************************************************************************
    ** Getter for featured
    **
    *******************************************************************************/
   public boolean getFeatured()
   {
      return featured;
   }



   /*******************************************************************************
    ** Setter for featured
    **
    *******************************************************************************/
   public void setFeatured(boolean featured)
   {
      this.featured = featured;
   }
}
