package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.io.IOException;
import java.io.Serializable;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;


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



   /*******************************************************************************
    ** Execute the backend step - using the request as input, and the result as output.
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QueryInput queryInput = new QueryInput(runBackendStepInput.getInstance());
      queryInput.setSession(runBackendStepInput.getSession());
      queryInput.setTableName(runBackendStepInput.getValueString(FIELD_SOURCE_TABLE));
      queryInput.setFilter(getQueryFilter(runBackendStepInput));
      queryInput.setRecordPipe(getRecordPipe());
      queryInput.setLimit(getLimit());
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
      CountInput countInput = new CountInput(runBackendStepInput.getInstance());
      countInput.setSession(runBackendStepInput.getSession());
      countInput.setTableName(runBackendStepInput.getValueString(FIELD_SOURCE_TABLE));
      countInput.setFilter(getQueryFilter(runBackendStepInput));
      CountOutput countOutput = new CountAction().execute(countInput);
      return (countOutput.getCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QQueryFilter getQueryFilter(RunBackendStepInput runBackendStepInput) throws QException
   {
      //////////////////////////////////////////////////////////////////////////////////////
      // if the queryFilterJson field is populated, read the filter from it and return it //
      //////////////////////////////////////////////////////////////////////////////////////
      String queryFilterJson = runBackendStepInput.getValueString("queryFilterJson");
      if(queryFilterJson != null)
      {
         try
         {
            return (JsonUtils.toObject(queryFilterJson, QQueryFilter.class));
         }
         catch(IOException e)
         {
            throw new QException("Error loading query filter from json field", e);
         }
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
      else
      {
         /////////////////////////////////////////////////////
         // else, see if a defaultQueryFilter was specified //
         /////////////////////////////////////////////////////
         Serializable defaultQueryFilter = runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_DEFAULT_QUERY_FILTER);
         if(defaultQueryFilter instanceof QQueryFilter filter)
         {
            return (filter);
         }
         if(defaultQueryFilter instanceof String string)
         {
            try
            {
               return (JsonUtils.toObject(string, QQueryFilter.class));
            }
            catch(IOException e)
            {
               throw new QException("Error loading default query filter from json", e);
            }
         }
      }

      throw (new QException("Could not find query filter for Extract step."));
   }

}
