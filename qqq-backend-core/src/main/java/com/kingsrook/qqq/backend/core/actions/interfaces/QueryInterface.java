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

package com.kingsrook.qqq.backend.core.actions.interfaces;


import java.time.Instant;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.querystats.QueryStat;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;


/*******************************************************************************
 ** Interface for the Query action.
 **
 *******************************************************************************/
public interface QueryInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   QueryOutput execute(QueryInput queryInput) throws QException;

   /*******************************************************************************
    **
    *******************************************************************************/
   default void setQueryStat(QueryStat queryStat)
   {
      //////////
      // noop //
      //////////
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default QueryStat getQueryStat()
   {
      return (null);
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default void setQueryStatJoinTables(Set<String> joinTableNames)
   {
      QueryStat queryStat = getQueryStat();
      if(queryStat != null)
      {
         queryStat.setJoinTables(joinTableNames);
      }
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default void setQueryStatFirstResultTime()
   {
      QueryStat queryStat = getQueryStat();
      if(queryStat != null)
      {
         if(queryStat.getFirstResultTimestamp() == null)
         {
            queryStat.setFirstResultTimestamp(Instant.now());
         }
      }
   }
}
