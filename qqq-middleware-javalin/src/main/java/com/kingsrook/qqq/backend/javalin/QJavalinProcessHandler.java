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

package com.kingsrook.qqq.backend.javalin;


import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobManager;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobState;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobStatus;
import com.kingsrook.qqq.backend.core.actions.async.JobGoingAsyncException;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallback;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessState;
import com.kingsrook.qqq.backend.core.model.actions.processes.QUploadedFile;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.state.StateType;
import com.kingsrook.qqq.backend.core.state.TempFileStateProvider;
import com.kingsrook.qqq.backend.core.state.UUIDAndTypeStateKey;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;


/*******************************************************************************
 ** methods for handling qqq processes in javalin.
 *******************************************************************************/
public class QJavalinProcessHandler
{
   private static final Logger LOG = LogManager.getLogger(QJavalinProcessHandler.class);

   private static int ASYNC_STEP_TIMEOUT_MILLIS = 3_000;



   /*******************************************************************************
    ** Define the routes
    *******************************************************************************/
   public static EndpointGroup getRoutes()
   {
      return (() ->
      {
         path("/processes", () ->
         {
            path("/{processName}", () ->
            {
               get("/init", QJavalinProcessHandler::processInit);
               post("/init", QJavalinProcessHandler::processInit);

               path("/{processUUID}", () ->
               {
                  post("/step/{step}", QJavalinProcessHandler::processStep);
                  get("/status/{jobUUID}", QJavalinProcessHandler::processStatus);
                  get("/records", QJavalinProcessHandler::processRecords);
               });
            });
         });
      });
   }



   /*******************************************************************************
    ** Init a process (named in path param :process)
    **
    *******************************************************************************/
   public static void processInit(Context context) throws QException
   {
      doProcessInitOrStep(context, null, null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doProcessInitOrStep(Context context, String processUUID, String startAfterStep)
   {
      Map<String, Object> resultForCaller = new HashMap<>();

      try
      {
         if(processUUID == null)
         {
            processUUID = UUID.randomUUID().toString();
         }
         resultForCaller.put("processUUID", processUUID);

         String processName = context.pathParam("processName");
         LOG.info(startAfterStep == null ? "Initiating process [" + processName + "] [" + processUUID + "]"
            : "Resuming process [" + processName + "] [" + processUUID + "] after step [" + startAfterStep + "]");

         RunProcessInput runProcessInput = new RunProcessInput(QJavalinImplementation.qInstance);
         QJavalinImplementation.setupSession(context, runProcessInput);
         runProcessInput.setProcessName(processName);
         runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);
         runProcessInput.setProcessUUID(processUUID);
         runProcessInput.setStartAfterStep(startAfterStep);
         populateRunProcessRequestWithValuesFromContext(context, runProcessInput);

         ////////////////////////////////////////
         // run the process as an async action //
         ////////////////////////////////////////
         Integer timeout = getTimeoutMillis(context);
         RunProcessOutput runProcessOutput = new AsyncJobManager().startJob(timeout, TimeUnit.MILLISECONDS, (callback) ->
         {
            runProcessInput.setAsyncJobCallback(callback);
            return (new RunProcessAction().execute(runProcessInput));
         });

         LOG.info("Process result error? " + runProcessOutput.getException());
         for(QFieldMetaData outputField : QJavalinImplementation.qInstance.getProcess(runProcessInput.getProcessName()).getOutputFields())
         {
            LOG.info("Process result output value: " + outputField.getName() + ": " + runProcessOutput.getValues().get(outputField.getName()));
         }

         serializeRunProcessResultForCaller(resultForCaller, runProcessOutput);
      }
      catch(JobGoingAsyncException jgae)
      {
         resultForCaller.put("jobUUID", jgae.getJobUUID());
      }
      catch(Exception e)
      {
         //////////////////////////////////////////////////////////////////////////////
         // our other actions in here would do: handleException(context, e);         //
         // which would return a 500 to the client.                                  //
         // but - other process-step actions, they always return a 200, just with an //
         // optional error message - so - keep all of the processes consistent.      //
         //////////////////////////////////////////////////////////////////////////////
         serializeRunProcessExceptionForCaller(resultForCaller, e);
      }

      context.result(JsonUtils.toJson(resultForCaller));
   }



   /*******************************************************************************
    ** Whether a step finished synchronously or asynchronously, return its data
    ** to the caller the same way.
    *******************************************************************************/
   private static void serializeRunProcessResultForCaller(Map<String, Object> resultForCaller, RunProcessOutput runProcessOutput)
   {
      if(runProcessOutput.getException().isPresent())
      {
         ////////////////////////////////////////////////////////////////
         // per code coverage, this path may never actually get hit... //
         ////////////////////////////////////////////////////////////////
         serializeRunProcessExceptionForCaller(resultForCaller, runProcessOutput.getException().get());
      }
      resultForCaller.put("values", runProcessOutput.getValues());
      runProcessOutput.getProcessState().getNextStepName().ifPresent(nextStep -> resultForCaller.put("nextStep", nextStep));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void serializeRunProcessExceptionForCaller(Map<String, Object> resultForCaller, Exception exception)
   {
      QUserFacingException userFacingException = ExceptionUtils.findClassInRootChain(exception, QUserFacingException.class);

      if(userFacingException != null)
      {
         LOG.info("User-facing exception in process", userFacingException);
         resultForCaller.put("error", userFacingException.getMessage()); // todo - put this somewhere else (make error an object w/ user-facing and/or other error?)
      }
      else
      {
         Throwable rootException = ExceptionUtils.getRootException(exception);
         LOG.warn("Uncaught Exception in process", exception);
         resultForCaller.put("error", "Error message: " + rootException.getMessage());
      }
   }



   /*******************************************************************************
    ** take values from query-string params, and put them into the run process request
    ** todo - make query params have a "field-" type of prefix??
    **
    *******************************************************************************/
   private static void populateRunProcessRequestWithValuesFromContext(Context context, RunProcessInput runProcessInput) throws IOException
   {
      //////////////////////////
      // process query string //
      //////////////////////////
      for(Map.Entry<String, List<String>> queryParam : context.queryParamMap().entrySet())
      {
         String       fieldName = queryParam.getKey();
         List<String> values    = queryParam.getValue();
         if(CollectionUtils.nullSafeHasContents(values))
         {
            runProcessInput.addValue(fieldName, values.get(0));
         }
      }

      ////////////////////////////
      // process form/post body //
      ////////////////////////////
      for(Map.Entry<String, List<String>> formParam : context.formParamMap().entrySet())
      {
         String       fieldName = formParam.getKey();
         List<String> values    = formParam.getValue();
         if(CollectionUtils.nullSafeHasContents(values))
         {
            runProcessInput.addValue(fieldName, values.get(0));
         }
      }

      ////////////////////////////
      // process uploaded files //
      ////////////////////////////
      for(UploadedFile uploadedFile : context.uploadedFiles())
      {
         QUploadedFile qUploadedFile = new QUploadedFile();
         qUploadedFile.setBytes(uploadedFile.getContent().readAllBytes());
         qUploadedFile.setFilename(uploadedFile.getFilename());

         UUIDAndTypeStateKey key = new UUIDAndTypeStateKey(StateType.UPLOADED_FILE);
         TempFileStateProvider.getInstance().put(key, qUploadedFile);
         LOG.info("Stored uploaded file in TempFileStateProvider under key: " + key);
         runProcessInput.addValue(QUploadedFile.DEFAULT_UPLOADED_FILE_FIELD_NAME, key);
      }

      /////////////////////////////////////////////////////////////
      // deal with params that specify an initial-records filter //
      /////////////////////////////////////////////////////////////
      QQueryFilter initialRecordsFilter = buildProcessInitRecordsFilter(context, runProcessInput);
      if(initialRecordsFilter != null)
      {
         runProcessInput.setCallback(new QProcessCallback()
         {
            @Override
            public QQueryFilter getQueryFilter()
            {
               return (initialRecordsFilter);
            }



            @Override
            public Map<String, Serializable> getFieldValues(List<QFieldMetaData> fields)
            {
               return (Collections.emptyMap());
            }
         });
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QQueryFilter buildProcessInitRecordsFilter(Context context, RunProcessInput runProcessInput) throws IOException
   {
      QInstance        instance = runProcessInput.getInstance();
      QProcessMetaData process  = instance.getProcess(runProcessInput.getProcessName());
      QTableMetaData   table    = instance.getTable(process.getTableName());

      if(table == null)
      {
         LOG.info("No table found in process - so not building an init records filter.");
         return (null);
      }
      String primaryKeyField = table.getPrimaryKeyField();

      String recordsParam = context.queryParam("recordsParam");
      if(StringUtils.hasContent(recordsParam))
      {
         @SuppressWarnings("ConstantConditions")
         String paramValue = context.queryParam(recordsParam);
         if(!StringUtils.hasContent(paramValue))
         {
            throw (new IllegalArgumentException("Missing value in query parameter: " + recordsParam + " (which was specified as the recordsParam)"));
         }

         switch(recordsParam)
         {
            case "recordIds":
               @SuppressWarnings("ConstantConditions")
               Serializable[] idStrings = paramValue.split(",");
               return (new QQueryFilter().withCriteria(new QFilterCriteria()
                  .withFieldName(primaryKeyField)
                  .withOperator(QCriteriaOperator.IN)
                  .withValues(Arrays.stream(idStrings).toList())));
            case "filterJSON":
               return (JsonUtils.toObject(paramValue, QQueryFilter.class));
            case "filterId":
               // return (JsonUtils.toObject(context.queryParam(recordsParam), QQueryFilter.class));
               throw (new NotImplementedException("Saved filters are not yet implemented."));
            default:
               throw (new IllegalArgumentException("Unrecognized value [" + recordsParam + "] for query parameter: recordsParam"));
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Run a step in a process (named in path param :processName)
    **
    *******************************************************************************/
   public static void processStep(Context context) throws QModuleDispatchException
   {
      String processUUID = context.pathParam("processUUID");
      String lastStep    = context.pathParam("step");
      doProcessInitOrStep(context, processUUID, lastStep);
   }



   /*******************************************************************************
    ** Get status for a currently running process (step)
    *******************************************************************************/
   public static void processStatus(Context context)
   {
      String processUUID = context.pathParam("processUUID");
      String jobUUID     = context.pathParam("jobUUID");

      Map<String, Object> resultForCaller = new HashMap<>();
      resultForCaller.put("processUUID", processUUID);

      LOG.info("Request for status of process " + processUUID + ", job " + jobUUID);
      Optional<AsyncJobStatus> optionalJobStatus = new AsyncJobManager().getJobStatus(jobUUID);
      if(optionalJobStatus.isEmpty())
      {
         serializeRunProcessExceptionForCaller(resultForCaller, new RuntimeException("Could not find status of process step job"));
      }
      else
      {
         AsyncJobStatus jobStatus = optionalJobStatus.get();

         resultForCaller.put("jobStatus", jobStatus);
         LOG.info("Job status is " + jobStatus.getState() + " for " + jobUUID);

         if(jobStatus.getState().equals(AsyncJobState.COMPLETE))
         {
            ///////////////////////////////////////////////////////////////////////////////////////
            // if the job is complete, get the process result from state provider, and return it //
            // this output should look like it did if the job finished synchronously!!           //
            ///////////////////////////////////////////////////////////////////////////////////////
            Optional<ProcessState> processState = RunProcessAction.getState(processUUID);
            if(processState.isPresent())
            {
               RunProcessOutput runProcessOutput = new RunProcessOutput(processState.get());
               serializeRunProcessResultForCaller(resultForCaller, runProcessOutput);
            }
            else
            {
               serializeRunProcessExceptionForCaller(resultForCaller, new RuntimeException("Could not find results for process " + processUUID));
            }
         }
         else if(jobStatus.getState().equals(AsyncJobState.ERROR))
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            // if the job had an error (e.g., a process step threw), "nicely" serialize its exception for the caller //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(jobStatus.getCaughtException() != null)
            {
               serializeRunProcessExceptionForCaller(resultForCaller, jobStatus.getCaughtException());
            }
         }
      }

      context.result(JsonUtils.toJson(resultForCaller));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void processRecords(Context context)
   {
      try
      {
         String  processUUID = context.pathParam("processUUID");
         Integer skip        = Objects.requireNonNullElse(QJavalinImplementation.integerQueryParam(context, "skip"), 0);
         Integer limit       = Objects.requireNonNullElse(QJavalinImplementation.integerQueryParam(context, "limit"), 20);

         // todo - potential optimization - if a future state provider could take advantage of it,
         //  we might pass the skip & limit in to a method that fetch just those 'n' rows from state, rather than the whole thing?
         Optional<ProcessState> optionalProcessState = RunProcessAction.getState(processUUID);
         if(optionalProcessState.isEmpty())
         {
            throw (new Exception("Could not find process results."));
         }
         ProcessState processState = optionalProcessState.get();

         List<QRecord> records = processState.getRecords();
         if(CollectionUtils.nullSafeIsEmpty(records))
         {
            throw (new Exception("No records were found for the process."));
         }

         Map<String, Object> resultForCaller = new HashMap<>();
         List<QRecord>       recordPage      = CollectionUtils.safelyGetPage(records, skip, limit);
         resultForCaller.put("records", recordPage);
         resultForCaller.put("totalRecords", records.size());
         context.result(JsonUtils.toJson(resultForCaller));
      }
      catch(Exception e)
      {
         QJavalinImplementation.handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setAsyncStepTimeoutMillis(int asyncStepTimeoutMillis)
   {
      ASYNC_STEP_TIMEOUT_MILLIS = asyncStepTimeoutMillis;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer getTimeoutMillis(Context context)
   {
      Integer timeout = QJavalinImplementation.integerQueryParam(context, "_qStepTimeoutMillis");
      if(timeout == null)
      {
         timeout = ASYNC_STEP_TIMEOUT_MILLIS;
      }
      return timeout;
   }

}