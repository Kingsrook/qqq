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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Generic implementation of an ExtractStep - that runs a Query action for a
 ** specified table.
 **
 ** If a query is specified from the caller (e.g., using the process Callback
 ** mechanism), that will be used.  Else a filter (object or json) in
 ** StreamedETLWithFrontendProcess.FIELD_DEFAULT_QUERY_FILTER will be checked.
 *******************************************************************************/
public class ExtractViaQueryStep extends AbstractExtractStep
{
   public static final String FIELD_SOURCE_TABLE = "sourceTable";

   private QQueryFilter queryFilter;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void preRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      super.preRun(runBackendStepInput, runBackendStepOutput);
      queryFilter = getQueryFilter(runBackendStepInput);
   }



   /*******************************************************************************
    ** Execute the backend step - using the request as input, and the result as output.
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(runBackendStepInput.getValueString(FIELD_SOURCE_TABLE));
      queryInput.setFilter(queryFilter);
      queryInput.setRecordPipe(getRecordPipe());
      queryInput.setLimit(getLimit());
      queryInput.setAsyncJobCallback(runBackendStepInput.getAsyncJobCallback());
      new QueryAction().execute(queryInput);

      ///////////////////////////////////////////////////////////////////
      // output is done into the pipe - so, nothing for us to do here. //
      ///////////////////////////////////////////////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer doCount(RunBackendStepInput runBackendStepInput) throws QException
   {
      CountInput countInput = new CountInput();
      countInput.setTableName(runBackendStepInput.getValueString(FIELD_SOURCE_TABLE));
      countInput.setFilter(queryFilter);
      CountOutput countOutput = new CountAction().execute(countInput);
      return (countOutput.getCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QQueryFilter getQueryFilter(RunBackendStepInput runBackendStepInput) throws QException
   {
      String       queryFilterJson    = runBackendStepInput.getValueString("queryFilterJson");
      Serializable defaultQueryFilter = runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_DEFAULT_QUERY_FILTER);

      //////////////////////////////////////////////////////////////////////////////////////
      // if the queryFilterJson field is populated, read the filter from it and return it //
      //////////////////////////////////////////////////////////////////////////////////////
      if(queryFilterJson != null)
      {
         return getQueryFilterFromJson(queryFilterJson, "Error loading query filter from json field");
      }
      else if(runBackendStepInput.getCallback() != null && runBackendStepInput.getCallback().getQueryFilter() != null)
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // else, try to get filter from process callback.  if we got one, store it as a process value for later steps //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         QQueryFilter queryFilter = runBackendStepInput.getCallback().getQueryFilter();
         runBackendStepInput.addValue("queryFilterJson", JsonUtils.toJson(queryFilter));
         return (queryFilter);
      }
      else if(defaultQueryFilter instanceof QQueryFilter filter)
      {
         /////////////////////////////////////////////////////////////////////////////
         // else, see if a defaultQueryFilter was specified as a QueryFilter object //
         /////////////////////////////////////////////////////////////////////////////
         return (filter);
      }
      else if(defaultQueryFilter instanceof String string)
      {
         /////////////////////////////////////////////////////////////////////////////
         // else, see if a defaultQueryFilter was specified as a JSON string
         /////////////////////////////////////////////////////////////////////////////
         return getQueryFilterFromJson(string, "Error loading default query filter from json");
      }
      else if(StringUtils.hasContent(runBackendStepInput.getValueString("filterJSON")))
      {
         ///////////////////////////////////////////////////////////////////////
         // else, check for filterJSON from a frontend launching of a process //
         ///////////////////////////////////////////////////////////////////////
         return getQueryFilterFromJson(runBackendStepInput.getValueString("filterJSON"), "Error loading default query filter from json");
      }
      else if(StringUtils.hasContent(runBackendStepInput.getValueString("recordIds")))
      {
         //////////////////////////////////////////////////////////////////////
         // else, check for recordIds from a frontend launching of a process //
         //////////////////////////////////////////////////////////////////////
         QTableMetaData table = runBackendStepInput.getInstance().getTable(runBackendStepInput.getValueString(FIELD_SOURCE_TABLE));
         if(table == null)
         {
            throw (new QException("source table name was not set - could not load records by id"));
         }
         String             recordIds = runBackendStepInput.getValueString("recordIds");
         Serializable[]     split     = recordIds.split(",");
         List<Serializable> idStrings = Arrays.stream(split).toList();
         return (new QQueryFilter().withCriteria(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, idStrings)));
      }

      throw (new QException("Could not find query filter for Extract step."));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QQueryFilter getQueryFilterFromJson(String queryFilterJson, String message) throws QException
   {
      try
      {
         return (JsonUtils.toObject(queryFilterJson, QQueryFilter.class));
      }
      catch(IOException e)
      {
         throw new QException(message, e);
      }
   }

}
