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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.basic;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Function body for performing the Extract step of a basic ETL process.
 *******************************************************************************/
public class BasicETLExtractFunction implements BackendStep
{
   private static final Logger LOG = LogManager.getLogger(BasicETLExtractFunction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String tableName = runBackendStepInput.getValueString(BasicETLProcess.FIELD_SOURCE_TABLE);
      LOG.info("Start query on table: " + tableName);

      QueryInput queryInput = new QueryInput(runBackendStepInput.getInstance());
      queryInput.setSession(runBackendStepInput.getSession());
      queryInput.setTableName(tableName);

      // queryRequest.setSkip(integerQueryParam(context, "skip"));
      // queryRequest.setLimit(integerQueryParam(context, "limit"));

      // todo? String filter = stringQueryParam(context, "filter");
      // if(filter != null)
      // {
      //    queryRequest.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
      // }

      QueryAction queryAction = new QueryAction();
      QueryOutput queryOutput = queryAction.execute(queryInput);

      runBackendStepOutput.setRecords(queryOutput.getRecords());
      LOG.info("Query on table " + tableName + " produced " + queryOutput.getRecords().size() + " records.");
   }
}
