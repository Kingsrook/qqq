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


import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;


/***************************************************************************
 **
 ***************************************************************************/
public class ExposedJoin implements ToSchema
{
   @OpenAPIExclude()
   private QFrontendExposedJoin wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ExposedJoin(QFrontendExposedJoin section)
   {
      this.wrapped = section;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ExposedJoin()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("User-facing label to display for this join")
   public String getLabel()
   {
      return (this.wrapped.getLabel());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Whether or not this join is 'to many' in nature")
   public Boolean getIsMany()
   {
      return (this.wrapped.getIsMany());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("The meta-data for the joined table")
   public TableMetaData getJoinTable()
   {
      return (new TableMetaData(this.wrapped.getJoinTable()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("A list of joins that travel from the base table to the exposed join table")
   @OpenAPIListItems(value = QJoinMetaData.class, useRef = false)
   public List<QJoinMetaData> getJoinPath()
   {
      return (this.wrapped.getJoinPath());
   }

}
