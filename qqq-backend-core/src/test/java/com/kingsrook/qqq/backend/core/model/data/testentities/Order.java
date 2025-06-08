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


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** Sample of an entity that can be converted to & from a QRecord
 *******************************************************************************/
public class Order extends QRecordEntity
{
   public static final String TABLE_NAME = "order";

   @QField()
   private String orderNo;

   @QAssociation(name = "lineItems")
   private List<LineItem> lineItems;



   /*******************************************************************************
    ** Getter for orderNo
    *******************************************************************************/
   public String getOrderNo()
   {
      return (this.orderNo);
   }



   /*******************************************************************************
    ** Setter for orderNo
    *******************************************************************************/
   public void setOrderNo(String orderNo)
   {
      this.orderNo = orderNo;
   }



   /*******************************************************************************
    ** Fluent setter for orderNo
    *******************************************************************************/
   public Order withOrderNo(String orderNo)
   {
      this.orderNo = orderNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for lineItems
    *******************************************************************************/
   public List<LineItem> getLineItems()
   {
      return (this.lineItems);
   }



   /*******************************************************************************
    ** Setter for lineItems
    *******************************************************************************/
   public void setLineItems(List<LineItem> lineItems)
   {
      this.lineItems = lineItems;
   }



   /*******************************************************************************
    ** Fluent setter for lineItems
    *******************************************************************************/
   public Order withLineItems(List<LineItem> lineItems)
   {
      this.lineItems = lineItems;
      return (this);
   }

}