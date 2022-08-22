package com.kingsrook.qqq.backend.core.actions.async;


import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class AsyncRecordPipeLoop
{
   private static final Logger LOG = LogManager.getLogger(AsyncRecordPipeLoop.class);

   private static final int TIMEOUT_AFTER_NO_RECORDS_MS = 10 * 60 * 1000;

   private static final int MAX_SLEEP_MS  = 1000;
   private static final int INIT_SLEEP_MS = 10;



   /*******************************************************************************
    **
    *******************************************************************************/
   public int run(String jobName, Integer recordLimit, RecordPipe recordPipe, UnsafeFunction<AsyncJobCallback, ? extends Serializable> job, UnsafeSupplier<Integer> consumer) throws QException
   {
      ///////////////////////////////////////////////////
      // start the extraction function as an async job //
      ///////////////////////////////////////////////////
      AsyncJobManager asyncJobManager = new AsyncJobManager();
      String          jobUUID         = asyncJobManager.startJob(jobName, job::apply);
      LOG.info("Started job [" + jobUUID + "] for record pipe streaming");

      AsyncJobState  jobState       = AsyncJobState.RUNNING;
      AsyncJobStatus asyncJobStatus = null;

      int  recordCount           = 0;
      int  nextSleepMillis       = INIT_SLEEP_MS;
      long lastReceivedRecordsAt = System.currentTimeMillis();
      long jobStartTime          = System.currentTimeMillis();

      while(jobState.equals(AsyncJobState.RUNNING))
      {
         if(recordPipe.countAvailableRecords() == 0)
         {
            ///////////////////////////////////////////////////////////
            // if the pipe is empty, sleep to let the producer work. //
            // todo - smarter sleep?  like get notified vs. sleep?   //
            ///////////////////////////////////////////////////////////
            LOG.info("No records are available in the pipe. Sleeping [" + nextSleepMillis + "] ms to give producer a chance to work");
            SleepUtils.sleep(nextSleepMillis, TimeUnit.MILLISECONDS);
            nextSleepMillis = Math.min(nextSleepMillis * 2, MAX_SLEEP_MS);

            long timeSinceLastReceivedRecord = System.currentTimeMillis() - lastReceivedRecordsAt;
            if(timeSinceLastReceivedRecord > TIMEOUT_AFTER_NO_RECORDS_MS)
            {
               throw (new QException("Job appears to have stopped producing records (last record received " + timeSinceLastReceivedRecord + " ms ago)."));
            }
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            // if the pipe has records, consume them.  reset the sleep timer so if we sleep again it'll be short. //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            lastReceivedRecordsAt = System.currentTimeMillis();
            nextSleepMillis = INIT_SLEEP_MS;

            recordCount += consumer.get();
            LOG.info(String.format("Processed %,d records so far", recordCount));

            if(recordLimit != null && recordCount >= recordLimit)
            {
               asyncJobManager.cancelJob(jobUUID);

               ////////////////////////////////////////////////////////////////////////////////////////////////////
               // in case the extract function doesn't recognize the cancellation request,                       //
               // tell the pipe to "terminate" - meaning - flush its queue and just noop when given new records. //
               // this should prevent anyone writing to such a pipe from potentially filling & blocking.         //
               ////////////////////////////////////////////////////////////////////////////////////////////////////
               recordPipe.terminate();

               break;
            }
         }

         //////////////////////////////
         // refresh the job's status //
         //////////////////////////////
         Optional<AsyncJobStatus> optionalAsyncJobStatus = asyncJobManager.getJobStatus(jobUUID);
         if(optionalAsyncJobStatus.isEmpty())
         {
            /////////////////////////////////////////////////
            // todo - ... maybe some version of try-again? //
            /////////////////////////////////////////////////
            throw (new QException("Could not get status of job [" + jobUUID + "]"));
         }
         asyncJobStatus = optionalAsyncJobStatus.get();
         jobState = asyncJobStatus.getState();
      }

      LOG.info("Job [" + jobUUID + "] completed with status: " + asyncJobStatus);

      ///////////////////////////////////
      // propagate errors from the job //
      ///////////////////////////////////
      if(asyncJobStatus != null && asyncJobStatus.getState().equals(AsyncJobState.ERROR))
      {
         throw (new QException("Job failed with an error", asyncJobStatus.getCaughtException()));
      }

      //////////////////////////////////////////////////////
      // send the final records to transform & load steps //
      //////////////////////////////////////////////////////
      recordCount += consumer.get();

      long endTime = System.currentTimeMillis();
      LOG.info(String.format("Processed %,d records", recordCount)
         + String.format(" at end of job in %,d ms (%.2f records/second).", (endTime - jobStartTime), 1000d * (recordCount / (.001d + (endTime - jobStartTime)))));

      return (recordCount);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @FunctionalInterface
   public interface UnsafeFunction<T, R>
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      R apply(T t) throws QException;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @FunctionalInterface
   public interface UnsafeSupplier<T>
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      T get() throws QException;
   }

}
