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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;


/***************************************************************************
 **
 ***************************************************************************/
public class QueryFilter implements ToSchema
{
   @OpenAPIExclude()
   private QQueryFilter wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QueryFilter(QQueryFilter wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QueryFilter()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Field level criteria that make up the query filter")
   @OpenAPIListItems(value = FilterCriteria.class, useRef = true)
   public List<FilterCriteria> getCriteria()
   {
      return (CollectionUtils.nonNullList(this.wrapped.getCriteria()).stream().map(s -> new FilterCriteria(s)).toList());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("How the query's results should be ordered (sorted).")
   @OpenAPIListItems(value = OrderBy.class, useRef = true)
   public List<OrderBy> getOrderBys()
   {
      return (CollectionUtils.nonNullList(this.wrapped.getOrderBys()).stream().map(s -> new OrderBy(s)).toList());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Boolean operator to apply between criteria")
   public QQueryFilter.BooleanOperator getBooleanOperator()
   {
      return (this.wrapped.getBooleanOperator());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Records to skip (e.g., to implement pagination)")
   public Integer getSkip()
   {
      return (this.wrapped.getSkip());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Maximum number of results to return.")
   public Integer getLimit()
   {
      return (this.wrapped.getLimit());
   }

}
