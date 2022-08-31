package com.kingsrook.qqq.backend.core.actions.automation.polling;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Singleton that runs a Polling Automation Provider.  Call its 'start' method
 ** to make it go.  Likely you need to set a sessionSupplier before you start -
 ** so that threads that do work will have a valid session.
 *******************************************************************************/
public class PollingAutomationExecutor
{
   private static final Logger LOG = LogManager.getLogger(PollingAutomationExecutor.class);

   private static PollingAutomationExecutor pollingAutomationExecutor = null;

   private Integer initialDelayMillis = 3000;
   private Integer delayMillis        = 1000;

   private Supplier<QSession> sessionSupplier;

   private RunningState             runningState = RunningState.STOPPED;
   private ScheduledExecutorService service;



   /*******************************************************************************
    ** Singleton constructor
    *******************************************************************************/
   private PollingAutomationExecutor()
   {

   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static PollingAutomationExecutor getInstance()
   {
      if(pollingAutomationExecutor == null)
      {
         pollingAutomationExecutor = new PollingAutomationExecutor();
      }
      return (pollingAutomationExecutor);
   }



   /*******************************************************************************
    **
    ** @return true iff the schedule was started
    *******************************************************************************/
   public boolean start(QInstance instance, String providerName)
   {
      if(!runningState.equals(RunningState.STOPPED))
      {
         LOG.info("Request to start from an invalid running state [" + runningState + "].  Must be STOPPED.");
         return (false);
      }

      LOG.info("Starting PollingAutomationExecutor");
      service = Executors.newSingleThreadScheduledExecutor();
      service.scheduleWithFixedDelay(new PollingAutomationRunner(instance, providerName, sessionSupplier), initialDelayMillis, delayMillis, TimeUnit.MILLISECONDS);
      runningState = RunningState.RUNNING;
      return (true);
   }



   /*******************************************************************************
    ** Stop, and don't wait to check if it worked or anything
    *******************************************************************************/
   public void stopAsync()
   {
      Runnable stopper = this::stop;
      stopper.run();
   }



   /*******************************************************************************
    ** Issue a stop, and wait (a while) for it to succeed.
    **
    ** @return true iff we see that the service fully stopped.
    *******************************************************************************/
   public boolean stop()
   {
      if(!runningState.equals(RunningState.RUNNING))
      {
         LOG.info("Request to stop from an invalid running state [" + runningState + "].  Must be RUNNING.");
         return (false);
      }

      LOG.info("Stopping PollingAutomationExecutor");
      runningState = RunningState.STOPPING;
      service.shutdown();

      try
      {
         if(service.awaitTermination(300, TimeUnit.SECONDS))
         {
            LOG.info("Successfully stopped PollingAutomationExecutor");
            runningState = RunningState.STOPPED;
            return (true);
         }

         LOG.info("Timed out waiting for service to fully terminate.  Will be left in STOPPING state.");
      }
      catch(InterruptedException ie)
      {
         ///////////////////////////////
         // what does this ever mean? //
         ///////////////////////////////
      }

      return (false);
   }



   /*******************************************************************************
    ** Getter for initialDelayMillis
    **
    *******************************************************************************/
   public Integer getInitialDelayMillis()
   {
      return initialDelayMillis;
   }



   /*******************************************************************************
    ** Setter for initialDelayMillis
    **
    *******************************************************************************/
   public void setInitialDelayMillis(Integer initialDelayMillis)
   {
      this.initialDelayMillis = initialDelayMillis;
   }



   /*******************************************************************************
    ** Getter for delayMillis
    **
    *******************************************************************************/
   public Integer getDelayMillis()
   {
      return delayMillis;
   }



   /*******************************************************************************
    ** Setter for delayMillis
    **
    *******************************************************************************/
   public void setDelayMillis(Integer delayMillis)
   {
      this.delayMillis = delayMillis;
   }



   /*******************************************************************************
    ** Setter for sessionSupplier
    **
    *******************************************************************************/
   public void setSessionSupplier(Supplier<QSession> sessionSupplier)
   {
      this.sessionSupplier = sessionSupplier;
   }



   /*******************************************************************************
    ** Getter for runningState
    **
    *******************************************************************************/
   public RunningState getRunningState()
   {
      return runningState;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum RunningState
   {
      STOPPED,
      RUNNING,
      STOPPING,
   }

}