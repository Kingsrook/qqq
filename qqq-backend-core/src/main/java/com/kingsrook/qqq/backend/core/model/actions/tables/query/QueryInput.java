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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Input data for the Query action
 **
 *******************************************************************************/
public class QueryInput extends AbstractTableActionInput
{
   private QQueryFilter filter;
   private Integer skip;
   private Integer limit;

   private RecordPipe recordPipe;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryInput()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryInput(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryInput(QInstance instance, QSession session)
   {
      super(instance);
      setSession(session);
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



   /*******************************************************************************
    ** Getter for recordPipe
    **
    *******************************************************************************/
   public RecordPipe getRecordPipe()
   {
      return recordPipe;
   }



   /*******************************************************************************
    ** Setter for recordPipe
    **
    *******************************************************************************/
   public void setRecordPipe(RecordPipe recordPipe)
   {
      this.recordPipe = recordPipe;
   }

}