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

package com.kingsrook.qqq.middleware.javalin.specs.v1.responses;


import com.kingsrook.qqq.middleware.javalin.executors.io.TableCountOutputInterface;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;


/*******************************************************************************
 **
 *******************************************************************************/
public class TableCountResponseV1 implements TableCountOutputInterface, ToSchema
{
   @OpenAPIDescription("Number (count) of records that satisfy the query request")
   private Long count;

   @OpenAPIDescription("Number (count) of distinct records that satisfy the query request.  Only included if requested.")
   private Long distinctCount;


   /*******************************************************************************
    ** Getter for count
    *******************************************************************************/
   public Long getCount()
   {
      return (this.count);
   }



   /*******************************************************************************
    ** Setter for count
    *******************************************************************************/
   public void setCount(Long count)
   {
      this.count = count;
   }



   /*******************************************************************************
    ** Fluent setter for count
    *******************************************************************************/
   public TableCountResponseV1 withCount(Long count)
   {
      this.count = count;
      return (this);
   }


   /*******************************************************************************
    ** Getter for distinctCount
    *******************************************************************************/
   public Long getDistinctCount()
   {
      return (this.distinctCount);
   }



   /*******************************************************************************
    ** Setter for distinctCount
    *******************************************************************************/
   public void setDistinctCount(Long distinctCount)
   {
      this.distinctCount = distinctCount;
   }



   /*******************************************************************************
    ** Fluent setter for distinctCount
    *******************************************************************************/
   public TableCountResponseV1 withDistinctCount(Long distinctCount)
   {
      this.distinctCount = distinctCount;
      return (this);
   }


}
