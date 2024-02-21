/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.automation;


import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.NowWithOffset;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.HtmlWrapper;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.WidgetHtmlLine;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DynamicDefaultValueBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.NoCodeWidgetFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.TablesPossibleValueSourceMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MultiLevelMapHelper;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Process to find records with a bad automation status, and repair them.
 **
 ** Bad status are defined as:
 ** - failed insert or updates.
 ** - running insert or updates for more than X minutes (see input field value).
 **
 ** Repair in this case means resetting their status to the corresponding (e.g.,
 ** insert/update) pending status.
 **
 *******************************************************************************/
public class HealBadRecordAutomationStatusesProcessStep implements BackendStep, MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "HealBadRecordAutomationStatusesProcess";

   private static final QLogger LOG = QLogger.getLogger(HealBadRecordAutomationStatusesProcessStep.class);

   private static final Map<Integer, Integer> statusUpdateMap = Map.of(
      AutomationStatus.FAILED_INSERT_AUTOMATIONS.getId(), AutomationStatus.PENDING_INSERT_AUTOMATIONS.getId(),
      AutomationStatus.RUNNING_INSERT_AUTOMATIONS.getId(), AutomationStatus.PENDING_INSERT_AUTOMATIONS.getId(),
      AutomationStatus.FAILED_UPDATE_AUTOMATIONS.getId(), AutomationStatus.PENDING_UPDATE_AUTOMATIONS.getId(),
      AutomationStatus.RUNNING_UPDATE_AUTOMATIONS.getId(), AutomationStatus.PENDING_UPDATE_AUTOMATIONS.getId()
   );



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData processMetaData = new QProcessMetaData()
         .withName(NAME)
         .withStepList(List.of(
            new QFrontendStepMetaData()
               .withName("input")
               .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
               .withFormField(new QFieldMetaData("tableName", QFieldType.STRING).withPossibleValueSourceName(TablesPossibleValueSourceMetaDataProvider.NAME))
               .withFormField(new QFieldMetaData("minutesOldLimit", QFieldType.INTEGER).withDefaultValue(60)),
            new QBackendStepMetaData()
               .withName("run")
               .withCode(new QCodeReference(getClass())),
            new QFrontendStepMetaData()
               .withName("output")

               .withComponent(new NoCodeWidgetFrontendComponentMetaData()
                  .withOutput(new WidgetHtmlLine()
                     .withCondition(new QFilterCriteria("warningCount", QCriteriaOperator.GREATER_THAN, 0))
                     .withWrapper(HtmlWrapper.divWithStyles(HtmlWrapper.STYLE_YELLOW))
                     .withVelocityTemplate("<b>Warning:</b>"))
                  .withOutput(new WidgetHtmlLine()
                     .withCondition(new QFilterCriteria("warningCount", QCriteriaOperator.GREATER_THAN, 0))
                     .withWrapper(HtmlWrapper.divWithStyles(HtmlWrapper.STYLE_INDENT_1))
                     .withWrapper(HtmlWrapper.divWithStyles(HtmlWrapper.STYLE_YELLOW))
                     .withVelocityTemplate("""
                        <ul>
                           #foreach($string in $warnings)
                              <li>$string</li>
                           #end
                        </ul>
                        """)))

               .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
               .withViewField(new QFieldMetaData("totalRecordsUpdated", QFieldType.INTEGER) /* todo - didn't display commas... .withDisplayFormat(DisplayFormat.COMMAS) */)

               .withComponent(new QFrontendComponentMetaData().withType(QComponentType.RECORD_LIST))
               .withRecordListField(new QFieldMetaData("tableName", QFieldType.STRING))
               .withRecordListField(new QFieldMetaData("badStatus", QFieldType.STRING))
               .withRecordListField(new QFieldMetaData("count", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS) /* todo - didn't display commas... */)

         ));

      return (processMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      int recordsUpdated = 0;

      ////////////////////////////////////////////////////////////////////////
      // if a table name is given, validate it, and run for just that table //
      ////////////////////////////////////////////////////////////////////////
      String            tableName = runBackendStepInput.getValueString("tableName");
      ArrayList<String> warnings  = new ArrayList<>();
      if(StringUtils.hasContent(tableName))
      {
         if(!QContext.getQInstance().getTables().containsKey(tableName))
         {
            throw (new QException("Unrecognized table name: " + tableName));
         }

         recordsUpdated += processTable(tableName, runBackendStepInput, runBackendStepOutput, warnings);
      }
      else
      {
         //////////////////////////////////////////////////////////////////////////
         // else, try to run for all tables that have an automation status field //
         //////////////////////////////////////////////////////////////////////////
         for(QTableMetaData table : QContext.getQInstance().getTables().values())
         {
            recordsUpdated += processTable(table.getName(), runBackendStepInput, runBackendStepOutput, warnings);
         }
      }

      runBackendStepOutput.addValue("totalRecordsUpdated", recordsUpdated);
      runBackendStepOutput.addValue("warnings", warnings);
      runBackendStepOutput.addValue("warningCount", warnings.size());

      if(CollectionUtils.nullSafeIsEmpty(runBackendStepOutput.getRecords()))
      {
         runBackendStepOutput.addRecord(new QRecord()
            .withValue("tableName", "--")
            .withValue("badStatus", "--")
            .withValue("count", "0"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int processTable(String tableName, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput, List<String> warnings)
   {
      try
      {
         Integer        minutesOldLimit = Objects.requireNonNullElse(runBackendStepInput.getValueInteger("minutesOldLimit"), 60);
         QTableMetaData table           = QContext.getQInstance().getTable(tableName);

         //////////////////////////////////////////////////////////////////////////
         // only process tables w/ automation details w/ a status tracking field //
         //////////////////////////////////////////////////////////////////////////
         if(table != null && table.getAutomationDetails() != null && table.getAutomationDetails().getStatusTracking() != null && StringUtils.hasContent(table.getAutomationDetails().getStatusTracking().getFieldName()))
         {
            String automationStatusFieldName = table.getAutomationDetails().getStatusTracking().getFieldName();

            /////////////////////////////////////////////
            // find the modify-date field on the table //
            /////////////////////////////////////////////
            String modifyDateFieldName = null;
            for(QFieldMetaData field : table.getFields().values())
            {
               if(DynamicDefaultValueBehavior.MODIFY_DATE.equals(field.getBehaviorOnlyIfSet(DynamicDefaultValueBehavior.class)))
               {
                  modifyDateFieldName = field.getName();
                  break;
               }
            }

            if(modifyDateFieldName == null)
            {
               warnings.add("Could not find a Modify Date field on table: " + tableName);
               LOG.info("Couldn't find a MODIFY_DATE field on table", logPair("tableName", tableName));
               return 0;
            }

            ////////////////////////////////////////////////////////////////////////
            // query for records either FAILED, or RUNNING w/ modify date too old //
            ////////////////////////////////////////////////////////////////////////
            QueryInput queryInput = new QueryInput();
            queryInput.setTableName(tableName);
            queryInput.setFilter(new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR)
               .withSubFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria(automationStatusFieldName, QCriteriaOperator.IN, AutomationStatus.FAILED_INSERT_AUTOMATIONS.getId(), AutomationStatus.FAILED_UPDATE_AUTOMATIONS.getId())))
               .withSubFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria(automationStatusFieldName, QCriteriaOperator.IN, AutomationStatus.RUNNING_INSERT_AUTOMATIONS.getId(), AutomationStatus.RUNNING_UPDATE_AUTOMATIONS.getId()))
                  .withCriteria(new QFilterCriteria(modifyDateFieldName, QCriteriaOperator.LESS_THAN, NowWithOffset.minus(minutesOldLimit, ChronoUnit.MINUTES))))
            );
            QueryOutput queryOutput = new QueryAction().execute(queryInput);

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // foreach record found, add it to list of records to be updated - mapping status to appropriate pending status //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            List<QRecord>        recordsToUpdate = new ArrayList<>();
            Map<String, Integer> countByStatus   = new HashMap<>();
            for(QRecord record : queryOutput.getRecords())
            {
               Integer badAutomationStatusId = record.getValueInteger(automationStatusFieldName);
               Integer updateStatus          = statusUpdateMap.get(badAutomationStatusId);
               if(updateStatus != null)
               {
                  AutomationStatus badStatus = AutomationStatus.getById(badAutomationStatusId);
                  if(badStatus != null)
                  {
                     MultiLevelMapHelper.getOrPutAndIncrement(countByStatus, badStatus.getLabel());
                  }

                  recordsToUpdate.add(new QRecord()
                     .withValue(table.getPrimaryKeyField(), record.getValue(table.getPrimaryKeyField()))
                     .withValue(automationStatusFieldName, updateStatus));
               }
            }

            if(!recordsToUpdate.isEmpty())
            {
               LOG.info("Healing bad record automation statuses", logPair("tableName", tableName), logPair("count", recordsToUpdate.size()));
               new UpdateAction().execute(new UpdateInput(tableName).withRecords(recordsToUpdate).withOmitTriggeringAutomations(true));
            }

            for(Map.Entry<String, Integer> entry : countByStatus.entrySet())
            {
               runBackendStepOutput.addRecord(new QRecord()
                  .withValue("tableName", tableName)
                  .withValue("badStatus", entry.getKey())
                  .withValue("count", entry.getValue()));
            }

            return (recordsToUpdate.size());
         }
      }
      catch(Exception e)
      {
         warnings.add("Error processing table: " + tableName + ": " + ExceptionUtils.getTopAndBottomMessages(e));
         LOG.warn("Error processing table for bad automation statuses", e, logPair("tableName, name"));
      }

      return 0;
   }

}
