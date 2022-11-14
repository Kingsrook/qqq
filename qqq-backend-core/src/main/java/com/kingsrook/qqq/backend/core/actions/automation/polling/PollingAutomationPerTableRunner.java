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

package com.kingsrook.qqq.backend.core.actions.automation.polling;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.async.AsyncRecordPipeLoop;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationStatusUpdater;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallback;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.automation.RecordAutomationInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.AutomationStatusTrackingType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TableAutomationAction;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TriggerEvent;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.scheduler.StandardScheduledExecutor;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Runnable for the Polling Automation Provider, that looks for records that
 ** need automations, and executes them.
 **
 ** An instance of this class should be created for each table/automation-status
 ** - see the TableActions inner record for that definition, and the static
 ** getTableActions method that helps someone who wants to start these threads
 ** figure out which ones are needed.
 *******************************************************************************/
public class PollingAutomationPerTableRunner implements Runnable
{
   private static final Logger LOG = LogManager.getLogger(PollingAutomationPerTableRunner.class);

   private final TableActions tableActions;
   private final String       name;

   private QInstance          instance;
   private Supplier<QSession> sessionSupplier;

   private static Map<TriggerEvent, AutomationStatus> triggerEventAutomationStatusMap = Map.of(
      TriggerEvent.POST_INSERT, AutomationStatus.PENDING_INSERT_AUTOMATIONS,
      TriggerEvent.POST_UPDATE, AutomationStatus.PENDING_UPDATE_AUTOMATIONS
   );

   private static Map<AutomationStatus, AutomationStatus> pendingToRunningStatusMap = Map.of(
      AutomationStatus.PENDING_INSERT_AUTOMATIONS, AutomationStatus.RUNNING_INSERT_AUTOMATIONS,
      AutomationStatus.PENDING_UPDATE_AUTOMATIONS, AutomationStatus.RUNNING_UPDATE_AUTOMATIONS
   );

   private static Map<AutomationStatus, AutomationStatus> pendingToFailedStatusMap = Map.of(
      AutomationStatus.PENDING_INSERT_AUTOMATIONS, AutomationStatus.FAILED_INSERT_AUTOMATIONS,
      AutomationStatus.PENDING_UPDATE_AUTOMATIONS, AutomationStatus.FAILED_UPDATE_AUTOMATIONS
   );



   /*******************************************************************************
    **
    *******************************************************************************/
   public record TableActions(String tableName, AutomationStatus status, List<TableAutomationAction> actions)
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<TableActions> getTableActions(QInstance instance, String providerName)
   {
      Map<String, Map<AutomationStatus, List<TableAutomationAction>>> workingTableActionMap = new HashMap<>();
      List<TableActions>                                              tableActionList       = new ArrayList<>();

      //////////////////////////////////////////////////////////////////////
      // todo - share logic like this among any automation implementation //
      //////////////////////////////////////////////////////////////////////
      for(QTableMetaData table : instance.getTables().values())
      {
         if(table.getAutomationDetails() != null && providerName.equals(table.getAutomationDetails().getProviderName()))
         {
            ///////////////////////////////////////////////////////////////////////////
            // organize the table's actions by type                                  //
            // todo - in future, need user-defined actions here too (and refreshed!) //
            ///////////////////////////////////////////////////////////////////////////
            for(TableAutomationAction action : table.getAutomationDetails().getActions())
            {
               AutomationStatus automationStatus = triggerEventAutomationStatusMap.get(action.getTriggerEvent());
               workingTableActionMap.putIfAbsent(table.getName(), new HashMap<>());
               workingTableActionMap.get(table.getName()).putIfAbsent(automationStatus, new ArrayList<>());
               workingTableActionMap.get(table.getName()).get(automationStatus).add(action);
            }

            ////////////////////////////////////////////
            // convert the map to tableAction records //
            ////////////////////////////////////////////
            for(Map.Entry<AutomationStatus, List<TableAutomationAction>> entry : workingTableActionMap.get(table.getName()).entrySet())
            {
               AutomationStatus            automationStatus = entry.getKey();
               List<TableAutomationAction> actionList       = entry.getValue();

               actionList.sort(Comparator.comparing(TableAutomationAction::getPriority));

               tableActionList.add(new TableActions(table.getName(), automationStatus, actionList));
            }
         }
      }

      return (tableActionList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public PollingAutomationPerTableRunner(QInstance instance, String providerName, Supplier<QSession> sessionSupplier, TableActions tableActions)
   {
      this.instance = instance;
      this.sessionSupplier = sessionSupplier;
      this.tableActions = tableActions;
      this.name = providerName + ">" + tableActions.tableName() + ">" + tableActions.status().getInsertOrUpdate();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run()
   {
      String originalThreadName = Thread.currentThread().getName();
      Thread.currentThread().setName(name + StandardScheduledExecutor.newThreadNameRandomSuffix());
      LOG.debug("Running " + this.getClass().getSimpleName() + "[" + name + "]");

      try
      {
         QSession session = sessionSupplier != null ? sessionSupplier.get() : new QSession();
         processTableInsertOrUpdate(instance.getTable(tableActions.tableName()), session, tableActions.status(), tableActions.actions());
      }
      catch(Exception e)
      {
         LOG.warn("Error running automations", e);
      }
      finally
      {
         Thread.currentThread().setName(originalThreadName);
      }
   }



   /*******************************************************************************
    ** Query for and process records that have a PENDING_INSERT or PENDING_UPDATE status on a given table.
    *******************************************************************************/
   private void processTableInsertOrUpdate(QTableMetaData table, QSession session, AutomationStatus automationStatus, List<TableAutomationAction> actions) throws QException
   {
      if(CollectionUtils.nullSafeIsEmpty(actions))
      {
         return;
      }

      LOG.debug("  Query for records " + automationStatus + " in " + table);

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // run an async-pipe loop - that will query for records in PENDING - put them in a pipe - then apply actions to them //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      RecordPipe          recordPipe          = new RecordPipe();
      AsyncRecordPipeLoop asyncRecordPipeLoop = new AsyncRecordPipeLoop();
      asyncRecordPipeLoop.run("PollingAutomationRunner>Query>" + automationStatus + ">" + table.getName(), null, recordPipe, (status) ->
         {
            QueryInput queryInput = new QueryInput(instance);
            queryInput.setSession(session);
            queryInput.setTableName(table.getName());

            AutomationStatusTrackingType statusTrackingType = table.getAutomationDetails().getStatusTracking().getType();
            if(AutomationStatusTrackingType.FIELD_IN_TABLE.equals(statusTrackingType))
            {
               queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria(table.getAutomationDetails().getStatusTracking().getFieldName(), QCriteriaOperator.EQUALS, List.of(automationStatus.getId()))));
            }
            else
            {
               throw (new NotImplementedException("Automation Status Tracking type [" + statusTrackingType + "] is not yet implemented in here."));
            }

            queryInput.setRecordPipe(recordPipe);
            return (new QueryAction().execute(queryInput));
         }, () ->
         {
            List<QRecord> records = recordPipe.consumeAvailableRecords();
            applyActionsToRecords(session, table, records, actions, automationStatus);
            return (records.size());
         }
      );
   }



   /*******************************************************************************
    ** For a set of records that were found to be in a PENDING state - run all the
    ** table's actions against them - IF they are found to match the action's filter
    ** (assuming it has one - if it doesn't, then all records match).
    *******************************************************************************/
   private void applyActionsToRecords(QSession session, QTableMetaData table, List<QRecord> records, List<TableAutomationAction> actions, AutomationStatus automationStatus) throws QException
   {
      if(CollectionUtils.nullSafeIsEmpty(records))
      {
         return;
      }

      ///////////////////////////////////////////////////
      // mark the records as RUNNING their automations //
      ///////////////////////////////////////////////////
      RecordAutomationStatusUpdater.setAutomationStatusInRecordsAndUpdate(instance, session, table, records, pendingToRunningStatusMap.get(automationStatus));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // foreach action - run it against the records (but only if they match the action's filter, if there is one) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      boolean anyActionsFailed = false;
      for(TableAutomationAction action : actions)
      {
         try
         {
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // note - this method - will re-query the objects, so we should have confidence that their data is fresh... //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            List<QRecord> matchingQRecords = getRecordsMatchingActionFilter(session, table, records, action);
            LOG.debug("Of the {} records that were pending automations, {} of them match the filter on the action {}", records.size(), matchingQRecords.size(), action);
            if(CollectionUtils.nullSafeHasContents(matchingQRecords))
            {
               LOG.debug("  Processing " + matchingQRecords.size() + " records in " + table + " for action " + action);
               applyActionToMatchingRecords(session, table, matchingQRecords, action);
            }
         }
         catch(Exception e)
         {
            LOG.warn("Caught exception processing records on " + table + " for action " + action, e);
            anyActionsFailed = true;
         }
      }

      ////////////////////////////////////////
      // update status on all these records //
      ////////////////////////////////////////
      if(anyActionsFailed)
      {
         RecordAutomationStatusUpdater.setAutomationStatusInRecordsAndUpdate(instance, session, table, records, pendingToFailedStatusMap.get(automationStatus));
      }
      else
      {
         RecordAutomationStatusUpdater.setAutomationStatusInRecordsAndUpdate(instance, session, table, records, AutomationStatus.OK);
      }
   }



   /*******************************************************************************
    ** For a given action, and a list of records - return a new list, of the ones
    ** which match the action's filter (if there is one - if not, then all match).
    **
    ** Note that this WILL re-query the objects  (ALWAYS - even if the action has no filter).
    ** This has the nice side effect of always giving fresh/updated records, despite having
    ** some cost.
    **
    ** At one point, we considered just applying the filter using java-comparisons,
    ** but that will almost certainly give potentially different results than a true
    ** backend - e.g., just consider if the DB is case-sensitive for strings...
    *******************************************************************************/
   private List<QRecord> getRecordsMatchingActionFilter(QSession session, QTableMetaData table, List<QRecord> records, TableAutomationAction action) throws QException
   {
      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(session);
      queryInput.setTableName(table.getName());

      QQueryFilter filter = new QQueryFilter();

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      // copy filter criteria from the action's filter to a new filter that we'll run here.              //
      // Critically - don't modify the filter object on the action!  as that object has a long lifespan. //
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      if(action.getFilter() != null)
      {
         if(action.getFilter().getCriteria() != null)
         {
            action.getFilter().getCriteria().forEach(filter::addCriteria);
         }
         if(action.getFilter().getOrderBys() != null)
         {
            action.getFilter().getOrderBys().forEach(filter::addOrderBy);
         }
      }

      filter.addCriteria(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, records.stream().map(r -> r.getValue(table.getPrimaryKeyField())).toList()));

      /////////////////////////////////////////////////////////////////////////////////////////////
      // always add order-by the primary key, to give more predictable/consistent results        //
      // todo - in future - if this becomes a source of slowness, make this a config to opt-out? //
      /////////////////////////////////////////////////////////////////////////////////////////////
      filter.addOrderBy(new QFilterOrderBy().withFieldName(table.getPrimaryKeyField()));

      queryInput.setFilter(filter);

      return (new QueryAction().execute(queryInput).getRecords());
   }



   /*******************************************************************************
    ** Finally, actually run action code against a list of known matching records.
    *******************************************************************************/
   private void applyActionToMatchingRecords(QSession session, QTableMetaData table, List<QRecord> records, TableAutomationAction action) throws Exception
   {
      if(StringUtils.hasContent(action.getProcessName()))
      {
         /////////////////////////////////////////////////////////////////////////////////////////
         // if the action has a process associated with it - run that process.                  //
         // tell it to SKIP frontend steps.                                                     //
         // give the process a callback w/ a query filter that has the p-keys of these records. //
         /////////////////////////////////////////////////////////////////////////////////////////
         RunProcessInput runProcessInput = new RunProcessInput(instance);
         runProcessInput.setSession(session);
         runProcessInput.setProcessName(action.getProcessName());
         runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
         runProcessInput.setCallback(new QProcessCallback()
         {
            @Override
            public QQueryFilter getQueryFilter()
            {
               List<Serializable> recordIds = records.stream().map(r -> r.getValueInteger(table.getPrimaryKeyField())).collect(Collectors.toList());
               return (new QQueryFilter().withCriteria(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, recordIds)));
            }
         });

         RunProcessAction runProcessAction = new RunProcessAction();
         RunProcessOutput runProcessOutput = runProcessAction.execute(runProcessInput);
         if(runProcessOutput.getException().isPresent())
         {
            throw (runProcessOutput.getException().get());
         }
      }
      else if(action.getCodeReference() != null)
      {
         LOG.debug("    Executing action: [" + action.getName() + "] as code reference: " + action.getCodeReference());
         RecordAutomationInput input = new RecordAutomationInput(instance);
         input.setSession(session);
         input.setTableName(table.getName());
         input.setRecordList(records);

         RecordAutomationHandler recordAutomationHandler = QCodeLoader.getRecordAutomationHandler(action);
         recordAutomationHandler.execute(input);
      }
   }



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }
}
