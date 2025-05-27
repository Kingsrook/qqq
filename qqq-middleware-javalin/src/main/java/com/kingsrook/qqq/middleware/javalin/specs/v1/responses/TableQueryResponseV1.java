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


import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.middleware.javalin.executors.io.TableQueryOutputInterface;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;
import com.kingsrook.qqq.middleware.javalin.specs.v1.responses.components.OutputRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class TableQueryResponseV1 implements TableQueryOutputInterface, ToSchema
{
   @OpenAPIDescription("List of records that satisfy the query request")
   @OpenAPIListItems(value = OutputRecord.class, useRef = true)
   private List<OutputRecord> records;



   /*******************************************************************************
    ** Setter for records
    *******************************************************************************/
   @Override
   public void setRecords(List<QRecord> records)
   {
      if(records == null)
      {
         this.records = null;
      }
      else
      {
         this.records = records.stream().map(qr -> new OutputRecord(qr)).collect(Collectors.toList());
      }
   }


   /*******************************************************************************
    ** Setter for records
    *******************************************************************************/
   public TableQueryResponseV1 withRecords(List<QRecord> records)
   {
      setRecords(records);
      return this;
   }



   /*******************************************************************************
    ** Getter for records
    *******************************************************************************/
   public List<OutputRecord> getRecords()
   {
      return (this.records);
   }

}
