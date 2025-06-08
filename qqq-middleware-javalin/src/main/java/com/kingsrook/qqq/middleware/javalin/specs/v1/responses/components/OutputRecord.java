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

package com.kingsrook.qqq.middleware.javalin.specs.v1.responses.components;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;


/***************************************************************************
 **
 ***************************************************************************/
public class OutputRecord implements ToSchema
{
   @OpenAPIExclude()
   private QRecord wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OutputRecord(QRecord wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OutputRecord()
   {
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   @OpenAPIDescription("Name of the table that the record is from.")
   public String getTableName()
   {
      return this.wrapped.getTableName();
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   @OpenAPIDescription("Label to identify the record to a user.")
   public String getRecordLabel()
   {
      return this.wrapped.getRecordLabel();
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   @OpenAPIDescription("Raw values that make up the record.  Keys are Strings, which match the table's field names. Values can be any type, as per the table's fields.")
   public Map<String, Serializable> getValues()
   {
      return this.wrapped.getValues();
   }



   /*******************************************************************************
    ** Getter for displayValues
    **
    *******************************************************************************/
   @OpenAPIDescription("Formatted string versions of the values that make up the record.  Keys are Strings, which match the table's field names. Values are all Strings.")
   public Map<String, String> getDisplayValues()
   {
      return this.wrapped.getDisplayValues();
   }

}
