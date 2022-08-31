package com.kingsrook.qqq.backend.core.actions.automation.polling;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationStatusUpdater;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.automation.RecordAutomationInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TableAutomationAction;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TriggerEvent;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Runnable for the Polling Automation Provider, that looks for records that
 ** need automations, and executes them.
 *******************************************************************************/
class PollingAutomationRunner implements Runnable
{
   private static final Logger LOG = LogManager.getLogger(PollingAutomationRunner.class);

   private QInstance instance;
   private String    providerName;
   private Supplier<QSession> sessionSupplier;

   private List<QTableMetaData> managedTables = new ArrayList<>();

   private Map<String, List<TableAutomationAction>> tableInsertActions = new HashMap<>();
   private Map<String, List<TableAutomationAction>> tableUpdateActions = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public PollingAutomationRunner(QInstance instance, String providerName, Supplier<QSession> sessionSupplier)
   {
      this.instance = instance;
      this.providerName = providerName;
      this.sessionSupplier = sessionSupplier;

      //////////////////////////////////////////////////////////////////////
      // todo - share logic like this among any automation implementation //
      //////////////////////////////////////////////////////////////////////
      for(QTableMetaData table : instance.getTables().values())
      {
         if(table.getAutomationDetails() != null && this.providerName.equals(table.getAutomationDetails().getProviderName()))
         {
            managedTables.add(table);

            ///////////////////////////////////////////////////////////////////////////
            // organize the table's actions by type                                  //
            // todo - in future, need user-defined actions here too (and refreshed!) //
            ///////////////////////////////////////////////////////////////////////////
            for(TableAutomationAction action : table.getAutomationDetails().getActions())
            {
               if(TriggerEvent.POST_INSERT.equals(action.getTriggerEvent()))
               {
                  tableInsertActions.putIfAbsent(table.getName(), new ArrayList<>());
                  tableInsertActions.get(table.getName()).add(action);
               }
               else if(TriggerEvent.POST_UPDATE.equals(action.getTriggerEvent()))
               {
                  tableUpdateActions.putIfAbsent(table.getName(), new ArrayList<>());
                  tableUpdateActions.get(table.getName()).add(action);
               }
            }

            //////////////////////////////
            // sort actions by priority //
            //////////////////////////////
            if(tableInsertActions.containsKey(table.getName()))
            {
               tableInsertActions.get(table.getName()).sort(Comparator.comparing(TableAutomationAction::getPriority));
            }

            if(tableUpdateActions.containsKey(table.getName()))
            {
               tableUpdateActions.get(table.getName()).sort(Comparator.comparing(TableAutomationAction::getPriority));
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run()
   {
      Thread.currentThread().setName(getClass().getSimpleName() + ">" + providerName);
      LOG.info("Running " + this.getClass().getSimpleName() + "[providerName=" + providerName + "]");

      for(QTableMetaData table : managedTables)
      {
         try
         {
            processTable(table);
         }
         catch(Exception e)
         {
            LOG.error("Error processing automations on table: " + table, e);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void processTable(QTableMetaData table) throws QException
   {
      QSession session = sessionSupplier != null ? sessionSupplier.get() : new QSession();
      processTableInsertOrUpdate(table, session, true);
      processTableInsertOrUpdate(table, session, false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void processTableInsertOrUpdate(QTableMetaData table, QSession session, boolean isInsert) throws QException
   {
      AutomationStatus            automationStatus = isInsert ? AutomationStatus.PENDING_INSERT_AUTOMATIONS : AutomationStatus.PENDING_UPDATE_AUTOMATIONS;
      List<TableAutomationAction> actions          = (isInsert ? tableInsertActions : tableUpdateActions).get(table.getName());
      if(CollectionUtils.nullSafeIsEmpty(actions))
      {
         return;
      }

      LOG.info("  Query for records " + automationStatus + " in " + table);

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(session); // todo - where the heck can we get this from??
      queryInput.setTableName(table.getName());

      for(TableAutomationAction action : actions)
      {
         QQueryFilter filter = action.getFilter();
         if(filter == null)
         {
            filter = new QQueryFilter();
         }

         filter.addCriteria(new QFilterCriteria(table.getAutomationDetails().getStatusTracking().getFieldName(), QCriteriaOperator.IN, List.of(automationStatus.getId())));
         queryInput.setFilter(filter);

         QueryOutput queryOutput = new QueryAction().execute(queryInput);

         // todo - pipe this query!!

         if(CollectionUtils.nullSafeHasContents(queryOutput.getRecords()))
         {
            LOG.info("  Processing " + queryOutput.getRecords().size() + " records in " + table + " for action " + action);
            processRecords(table, actions, queryOutput.getRecords(), session, isInsert);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void processRecords(QTableMetaData table, List<TableAutomationAction> actions, List<QRecord> records, QSession session, boolean isInsert) throws QException
   {
      try
      {
         updateRecordAutomationStatus(table, session, records, isInsert ? AutomationStatus.RUNNING_INSERT_AUTOMATIONS : AutomationStatus.RUNNING_UPDATE_AUTOMATIONS);

         for(TableAutomationAction action : actions)
         {
            ////////////////////////////////////
            // todo - what, re-query them? :( //
            ////////////////////////////////////
            if(StringUtils.hasContent(action.getProcessName()))
            {
               //////////////////////////////////////////////////////////////////////////////////////////////
               // todo - uh, how to make these records the input, where an extract step might be involved? //
               //  should extract step ... see record list and just use it?  i think maybe?                //
               //////////////////////////////////////////////////////////////////////////////////////////////
               throw (new NotImplementedException("processes for automation not yet implemented"));
            }
            else if(action.getCodeReference() != null)
            {
               LOG.info("    Executing action: [" + action.getName() + "] as code reference: " + action.getCodeReference());
               RecordAutomationInput input = new RecordAutomationInput(instance);
               input.setSession(session);
               input.setTableName(table.getName());
               input.setRecordList(records);

               RecordAutomationHandler recordAutomationHandler = QCodeLoader.getRecordAutomationHandler(action);
               recordAutomationHandler.execute(input);
            }
         }

         updateRecordAutomationStatus(table, session, records, AutomationStatus.OK);
      }
      catch(Exception e)
      {
         updateRecordAutomationStatus(table, session, records, isInsert ? AutomationStatus.FAILED_INSERT_AUTOMATIONS : AutomationStatus.FAILED_UPDATE_AUTOMATIONS);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void updateRecordAutomationStatus(QTableMetaData table, QSession session, List<QRecord> records, AutomationStatus automationStatus) throws QException
   {
      RecordAutomationStatusUpdater.setAutomationStatusInRecordsAndUpdate(instance, session, table, records, automationStatus);
   }

}
