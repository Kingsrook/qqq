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

package com.kingsrook.qqq.backend.core.actions.tables;


import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.interfaces.AggregateInterface;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.QueryStatManager;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.querystats.QueryStat;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 ** Action to run an aggregate against a table.
 **
 *******************************************************************************/
public class AggregateAction
{
   private static final QLogger LOG = QLogger.getLogger(AggregateAction.class);

   private AggregateInterface aggregateInterface;



   /*******************************************************************************
    **
    *******************************************************************************/
   public AggregateOutput execute(AggregateInput aggregateInput) throws QException
   {
      ActionHelper.validateSession(aggregateInput);

      QTableMetaData   table   = aggregateInput.getTable();
      QBackendMetaData backend = aggregateInput.getBackend();

      QueryStat queryStat = QueryStatManager.newQueryStat(backend, table, aggregateInput.getFilter());

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(aggregateInput.getBackend());

      aggregateInterface = qModule.getAggregateInterface();
      aggregateInterface.setQueryStat(queryStat);
      AggregateOutput aggregateOutput = aggregateInterface.execute(aggregateInput);

      QueryStatManager.getInstance().add(queryStat);

      return aggregateOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void cancel()
   {
      if(aggregateInterface == null)
      {
         LOG.warn("aggregateInterface object was null when requested to cancel");
         return;
      }

      aggregateInterface.cancelAction();
   }
}
