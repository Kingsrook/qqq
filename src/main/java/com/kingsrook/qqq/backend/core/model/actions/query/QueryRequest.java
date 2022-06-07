/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.model.actions.query;


import com.kingsrook.qqq.backend.core.model.actions.AbstractQTableRequest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 ** Request data for the Query action
 **
 *******************************************************************************/
public class QueryRequest extends AbstractQTableRequest
{
   private QQueryFilter filter;
   private Integer skip;
   private Integer limit;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryRequest(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    ** Getter for filter
    **
    *******************************************************************************/
   public QQueryFilter getFilter()
   {
      return filter;
   }



   /*******************************************************************************
    ** Setter for filter
    **
    *******************************************************************************/
   public void setFilter(QQueryFilter filter)
   {
      this.filter = filter;
   }



   /*******************************************************************************
    ** Getter for skip
    **
    *******************************************************************************/
   public Integer getSkip()
   {
      return skip;
   }



   /*******************************************************************************
    ** Setter for skip
    **
    *******************************************************************************/
   public void setSkip(Integer skip)
   {
      this.skip = skip;
   }



   /*******************************************************************************
    ** Getter for limit
    **
    *******************************************************************************/
   public Integer getLimit()
   {
      return limit;
   }



   /*******************************************************************************
    ** Setter for limit
    **
    *******************************************************************************/
   public void setLimit(Integer limit)
   {
      this.limit = limit;
   }
}
