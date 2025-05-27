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


import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;


/***************************************************************************
 **
 ***************************************************************************/
public class QueryJoin implements ToSchema
{
   @OpenAPIExclude()
   private com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QueryJoin(com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QueryJoin()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Table being joined into the query by this QueryJoin")
   public String getJoinTable()
   {
      return (this.wrapped.getJoinTable());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Base table (or an alias) that this QueryJoin is joined against")
   public String getBaseTableOrAlias()
   {
      return (this.wrapped.getBaseTableOrAlias());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Name of a join to use in case the baseTable and joinTable have more than one")
   public String getJoinName()
   {
      return (this.wrapped.getJoinMetaData() == null ? null : this.wrapped.getJoinMetaData().getName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Alias to apply to this table in the join query")
   public String getAlias()
   {
      return (this.wrapped.getAlias());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Whether or not to select values from the join table")
   public Boolean getSelect()
   {
      return (this.wrapped.getSelect());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Type of join being performed (SQL semantics)")
   public com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin.Type getType()
   {
      return (this.wrapped.getType());
   }

}
