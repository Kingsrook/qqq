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

package com.kingsrook.qqq.middleware.javalin.executors.io;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;


/*******************************************************************************
 **
 *******************************************************************************/
public class QueryMiddlewareInput extends AbstractMiddlewareInput
{
   private String          table;
   private QQueryFilter    filter;
   private List<QueryJoin> queryJoins;



   /*******************************************************************************
    ** Getter for table
    *******************************************************************************/
   public String getTable()
   {
      return (this.table);
   }



   /*******************************************************************************
    ** Setter for table
    *******************************************************************************/
   public void setTable(String table)
   {
      this.table = table;
   }



   /*******************************************************************************
    ** Fluent setter for table
    *******************************************************************************/
   public QueryMiddlewareInput withTable(String table)
   {
      this.table = table;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filter
    *******************************************************************************/
   public QQueryFilter getFilter()
   {
      return (this.filter);
   }



   /*******************************************************************************
    ** Setter for filter
    *******************************************************************************/
   public void setFilter(QQueryFilter filter)
   {
      this.filter = filter;
   }



   /*******************************************************************************
    ** Fluent setter for filter
    *******************************************************************************/
   public QueryMiddlewareInput withFilter(QQueryFilter filter)
   {
      this.filter = filter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryJoins
    *******************************************************************************/
   public List<QueryJoin> getQueryJoins()
   {
      return (this.queryJoins);
   }



   /*******************************************************************************
    ** Setter for queryJoins
    *******************************************************************************/
   public void setQueryJoins(List<QueryJoin> queryJoins)
   {
      this.queryJoins = queryJoins;
   }



   /*******************************************************************************
    ** Fluent setter for queryJoins
    *******************************************************************************/
   public QueryMiddlewareInput withQueryJoins(List<QueryJoin> queryJoins)
   {
      this.queryJoins = queryJoins;
      return (this);
   }

}
