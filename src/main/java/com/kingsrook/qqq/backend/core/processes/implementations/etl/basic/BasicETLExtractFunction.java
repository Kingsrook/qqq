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


import com.kingsrook.qqq.backend.core.actions.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.interfaces.FunctionBody;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;


/*******************************************************************************
 ** Function body for performing the Extract step of a basic ETL process.
 *******************************************************************************/
public class BasicETLExtractFunction implements FunctionBody
{
   @Override
   public void run(RunFunctionRequest runFunctionRequest, RunFunctionResult runFunctionResult) throws QException
   {
      QueryRequest queryRequest = new QueryRequest(runFunctionRequest.getInstance());
      queryRequest.setSession(runFunctionRequest.getSession());
      queryRequest.setTableName(runFunctionRequest.getValueString(BasicETLProcess.FIELD_SOURCE_TABLE));
      // queryRequest.setSkip(integerQueryParam(context, "skip"));
      // queryRequest.setLimit(integerQueryParam(context, "limit"));

      // todo? String filter = stringQueryParam(context, "filter");
      // if(filter != null)
      // {
      //    queryRequest.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
      // }

      QueryAction queryAction = new QueryAction();
      QueryResult queryResult = queryAction.execute(queryRequest);

      runFunctionResult.setRecords(queryResult.getRecords());
   }
}
