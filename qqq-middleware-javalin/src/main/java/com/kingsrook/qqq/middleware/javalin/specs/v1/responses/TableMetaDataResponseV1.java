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


import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;
import com.kingsrook.qqq.middleware.javalin.executors.io.TableMetaDataOutputInterface;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.SchemaBuilder;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.specs.v1.responses.components.TableMetaData;
import com.kingsrook.qqq.openapi.model.Schema;


/*******************************************************************************
 **
 *******************************************************************************/
public class TableMetaDataResponseV1 implements TableMetaDataOutputInterface, ToSchema
{
   private TableMetaData tableMetaData;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void setTableMetaData(QFrontendTableMetaData frontendTableMetaData)
   {
      this.tableMetaData = new TableMetaData(frontendTableMetaData);
   }



   /*******************************************************************************
    ** Fluent setter for frontendTableMetaData
    **
    *******************************************************************************/
   public TableMetaDataResponseV1 withTableMetaData(QFrontendTableMetaData frontendTableMetaData)
   {
      setTableMetaData(frontendTableMetaData);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Schema toSchema()
   {
      return new SchemaBuilder().classToSchema(TableMetaData.class);
   }



   /*******************************************************************************
    ** Getter for tableMetaData
    **
    *******************************************************************************/
   public TableMetaData getTableMetaData()
   {
      return tableMetaData;
   }
}
