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

package com.kingsrook.qqq.backend.core.actions.async;


import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.state.InMemoryStateProvider;
import com.kingsrook.qqq.backend.core.state.StateProviderInterface;
import com.kingsrook.qqq.backend.core.state.StateType;
import com.kingsrook.qqq.backend.core.state.UUIDAndTypeStateKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Class to manage running asynchronous actions, and working with their statuses.
 *******************************************************************************/
public class AsyncJobManager
{
   private static final Logger LOG = LogManager.getLogger(AsyncJobManager.class);



   /*******************************************************************************
    ** Start a job - if it finishes within the specified timeout, get its results,
    ** else, get back an exception with the job id.
    *******************************************************************************/
   public <T extends Serializable> T startJob(long timeout, TimeUnit timeUnit, AsyncJob<T> asyncJob) throws JobGoingAsyncException, QException
   {
      return (startJob("Anonymous", timeout, timeUnit, asyncJob));
   }



   /*******************************************************************************
    ** Start a job - if it finishes within the specified timeout, get its results,
    ** else, get back an exception with the job id.
    *******************************************************************************/
   public <T extends Serializable> T startJob(String jobName, long timeout, TimeUnit timeUnit, AsyncJob<T> asyncJob) throws JobGoingAsyncException, QException
   {
      UUIDAndTypeStateKey uuidAndTypeStateKey = new UUIDAndTypeStateKey(UUID.randomUUID(), StateType.ASYNC_JOB_STATUS);
      AsyncJobStatus      asyncJobStatus      = new AsyncJobStatus();
      asyncJobStatus.setState(AsyncJobState.RUNNING);
      getStateProvider().put(uuidAndTypeStateKey, asyncJobStatus);

      try
      {
         CompletableFuture<T> future = CompletableFuture.supplyAsync(() ->
         {
            return (runAsyncJob(jobName, asyncJob, uuidAndTypeStateKey, asyncJobStatus));
         });

         if(timeout == 0)
         {
            throw (new JobGoingAsyncException(uuidAndTypeStateKey.getUuid().toString()));
         }

         T result = future.get(timeout, timeUnit);
         return (result);
      }
      catch(InterruptedException | ExecutionException e)
      {
         throw (new QException("Error running job", e));
      }
      catch(TimeoutException e)
      {
         LOG.info("Job going async " + uuidAndTypeStateKey.getUuid());
         throw (new JobGoingAsyncException(uuidAndTypeStateKey.getUuid().toString()));
      }
   }



   /*******************************************************************************
    ** Start a job, and always, just get back the job UUID.
    *******************************************************************************/
   public <T extends Serializable> String startJob(AsyncJob<T> asyncJob) throws QException
   {
      return (startJob("Anonymous", asyncJob));
   }



   /*******************************************************************************
    ** Start a job, and always, just get back the job UUID.
    *******************************************************************************/
   public <T extends Serializable> String startJob(String jobName, AsyncJob<T> asyncJob) throws QException
   {
      try
      {
         startJob(jobName, 0, TimeUnit.MILLISECONDS, asyncJob);
         throw (new QException("Job was expected to go asynchronous, but did not"));
      }
      catch(JobGoingAsyncException jgae)
      {
         return (jgae.getJobUUID());
      }
   }



   /*******************************************************************************
    ** run the job.
    *******************************************************************************/
   private <T extends Serializable> T runAsyncJob(String jobName, AsyncJob<T> asyncJob, UUIDAndTypeStateKey uuidAndTypeStateKey, AsyncJobStatus asyncJobStatus)
   {
      String originalThreadName = Thread.currentThread().getName();
      Thread.currentThread().setName("Job:" + jobName + ":" + uuidAndTypeStateKey.getUuid().toString().substring(0, 8));
      try
      {
         LOG.info("Starting job " + uuidAndTypeStateKey.getUuid());
         T result = asyncJob.run(new AsyncJobCallback(uuidAndTypeStateKey.getUuid(), asyncJobStatus));
         asyncJobStatus.setState(AsyncJobState.COMPLETE);
         getStateProvider().put(uuidAndTypeStateKey, asyncJobStatus);
         LOG.info("Completed job " + uuidAndTypeStateKey.getUuid());
         return (result);
      }
      catch(Exception e)
      {
         asyncJobStatus.setState(AsyncJobState.ERROR);
         asyncJobStatus.setCaughtException(e);
         getStateProvider().put(uuidAndTypeStateKey, asyncJobStatus);
         LOG.warn("Job " + uuidAndTypeStateKey.getUuid() + " ended with an exception: ", e);
         throw (new CompletionException(e));
      }
      finally
      {
         Thread.currentThread().setName(originalThreadName);
      }
   }



   /*******************************************************************************
    ** Get the status of the job identified by the given UUID.
    **
    *******************************************************************************/
   public Optional<AsyncJobStatus> getJobStatus(String uuid)
   {
      UUIDAndTypeStateKey uuidAndTypeStateKey = new UUIDAndTypeStateKey(UUID.fromString(uuid), StateType.ASYNC_JOB_STATUS);
      return (getStateProvider().get(AsyncJobStatus.class, uuidAndTypeStateKey));
   }



   /*******************************************************************************
    ** Load an instance of the appropriate state provider
    **
    *******************************************************************************/
   static StateProviderInterface getStateProvider()
   {
      // TODO - read this from somewhere in meta data eh?
      return InMemoryStateProvider.getInstance();

      // todo - by using JSON serialization internally, this makes stupidly large payloads and crashes things.
      // return TempFileStateProvider.getInstance();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void cancelJob(String jobUUID)
   {
      Optional<AsyncJobStatus> jobStatus = getJobStatus(jobUUID);
      jobStatus.ifPresent(asyncJobStatus -> asyncJobStatus.setCancelRequested(true));
   }

}
