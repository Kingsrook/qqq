/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.scheduler.processes;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.InitializableViaCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReferenceWithProperties;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJob;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** an implementation of a TableCustomizer that runs a subclass of
 ** AbstractRecordSyncToScheduledJobProcess - to manage scheduledJob records that
 ** correspond to records in another table (e.g., a job for each Client)
 **
 ** Easiest way to use is:
 ** - BaseSyncToScheduledJobTableCustomizer.setTableCustomizers(tableMetaData, new YourSyncScheduledJobProcessSubclass());
 ** which adds post-insert, -update, and -delete customizers to your table.
 **
 ** If you need additional table customizer code in those slots, I suppose you could
 ** simply make your customizer create an instance of this class, set its
 ** properties, and run its appropriate postInsertOrUpdate/postDelete methods.
 *******************************************************************************/
public class BaseSyncToScheduledJobTableCustomizer implements TableCustomizerInterface, InitializableViaCodeReference
{
   private static final QLogger LOG = QLogger.getLogger(BaseSyncToScheduledJobTableCustomizer.class);

   public static final String KEY_TABLE_NAME                     = "tableName";
   public static final String KEY_SYNC_PROCESS_NAME              = "syncProcessName";
   public static final String KEY_SCHEDULED_JOB_FOREIGN_KEY_TYPE = "scheduledJobForeignKeyType";

   private String tableName;
   private String syncProcessName;
   private String scheduledJobForeignKeyType;



   /***************************************************************************
    **
    ***************************************************************************/
   public static void setTableCustomizers(QTableMetaData tableMetaData, AbstractRecordSyncToScheduledJobProcess syncProcess)
   {
      QCodeReference codeReference = new QCodeReferenceWithProperties(BaseSyncToScheduledJobTableCustomizer.class, Map.of(
         KEY_TABLE_NAME, tableMetaData.getName(),
         KEY_SYNC_PROCESS_NAME, syncProcess.getClass().getSimpleName(),
         KEY_SCHEDULED_JOB_FOREIGN_KEY_TYPE, syncProcess.getScheduledJobForeignKeyType()
      ));

      tableMetaData.withCustomizer(TableCustomizers.POST_INSERT_RECORD, codeReference);
      tableMetaData.withCustomizer(TableCustomizers.POST_UPDATE_RECORD, codeReference);
      tableMetaData.withCustomizer(TableCustomizers.POST_DELETE_RECORD, codeReference);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void initialize(QCodeReference codeReference)
   {
      if(codeReference instanceof QCodeReferenceWithProperties codeReferenceWithProperties)
      {
         tableName = ValueUtils.getValueAsString(codeReferenceWithProperties.getProperties().get(KEY_TABLE_NAME));
         syncProcessName = ValueUtils.getValueAsString(codeReferenceWithProperties.getProperties().get(KEY_SYNC_PROCESS_NAME));
         scheduledJobForeignKeyType = ValueUtils.getValueAsString(codeReferenceWithProperties.getProperties().get(KEY_SCHEDULED_JOB_FOREIGN_KEY_TYPE));

         if(!StringUtils.hasContent(tableName))
         {
            LOG.warn("Missing property under KEY_TABLE_NAME [" + KEY_TABLE_NAME + "] in codeReference for BaseSyncToScheduledJobTableCustomizer");
         }

         if(!StringUtils.hasContent(syncProcessName))
         {
            LOG.warn("Missing property under KEY_SYNC_PROCESS_NAME [" + KEY_SYNC_PROCESS_NAME + "] in codeReference for BaseSyncToScheduledJobTableCustomizer");
         }

         if(!StringUtils.hasContent(scheduledJobForeignKeyType))
         {
            LOG.warn("Missing property under KEY_SCHEDULED_JOB_FOREIGN_KEY_TYPE [" + KEY_SCHEDULED_JOB_FOREIGN_KEY_TYPE + "] in codeReference for BaseSyncToScheduledJobTableCustomizer");
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> postInsertOrUpdate(AbstractActionInput input, List<QRecord> records, Optional<List<QRecord>> oldRecordList) throws QException
   {
      runSyncProcessForRecordList(records, syncProcessName);
      return records;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> postDelete(DeleteInput deleteInput, List<QRecord> records) throws QException
   {
      deleteScheduledJobsForRecordList(records);
      return records;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void runSyncProcessForRecordList(List<QRecord> records, String processName)
   {
      if(QContext.getQInstance().getTable(ScheduledJob.TABLE_NAME) == null)
      {
         LOG.info("ScheduledJob table not found, skipping scheduled job sync.");
      }

      String primaryKeyField = QContext.getQInstance().getTable(tableName).getPrimaryKeyField();

      List<Serializable> sourceRecordIds = records.stream()
         .filter(r -> CollectionUtils.nullSafeIsEmpty(r.getErrors()))
         .map(r -> r.getValue(primaryKeyField))
         .filter(Objects::nonNull).toList();

      if(CollectionUtils.nullSafeIsEmpty(sourceRecordIds))
      {
         return;
      }

      try
      {
         RunProcessInput runProcessInput = new RunProcessInput();
         runProcessInput.setProcessName(processName);
         runProcessInput.setCallback(QProcessCallbackFactory.forPrimaryKeys("id", sourceRecordIds));
         runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);

         Serializable processSummary = runProcessOutput.getValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY);
         ProcessSummaryLineInterface.log("Sync to ScheduledJob Process Summary", processSummary, List.of(logPair("sourceTable", tableName)));
      }
      catch(Exception e)
      {
         LOG.warn("Error syncing records to scheduled jobs", e, logPair("sourceTable", tableName), logPair("sourceRecordIds", sourceRecordIds));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void deleteScheduledJobsForRecordList(List<QRecord> records)
   {
      if(QContext.getQInstance().getTable(ScheduledJob.TABLE_NAME) == null)
      {
         LOG.info("ScheduledJob table not found, skipping scheduled job delete.");
      }

      List<String> sourceRecordIds = records.stream()
         .filter(r -> CollectionUtils.nullSafeIsEmpty(r.getErrors()))
         .map(r -> r.getValueString("id")).toList();

      if(sourceRecordIds.isEmpty())
      {
         return;
      }

      ///////////////////////////////////////////////////
      // delete any corresponding scheduledJob records //
      ///////////////////////////////////////////////////
      try
      {
         new DeleteAction().execute(new DeleteInput(ScheduledJob.TABLE_NAME).withQueryFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("foreignKeyType", QCriteriaOperator.EQUALS, getScheduledJobForeignKeyType()))
            .withCriteria(new QFilterCriteria("foreignKeyValue", QCriteriaOperator.IN, sourceRecordIds))));
      }
      catch(Exception e)
      {
         LOG.warn("Error deleting scheduled jobs for scheduled records", e, logPair("sourceTable", tableName), logPair("sourceRecordIds", sourceRecordIds));
      }
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public BaseSyncToScheduledJobTableCustomizer withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for syncProcessName
    *******************************************************************************/
   public String getSyncProcessName()
   {
      return (this.syncProcessName);
   }



   /*******************************************************************************
    ** Setter for syncProcessName
    *******************************************************************************/
   public void setSyncProcessName(String syncProcessName)
   {
      this.syncProcessName = syncProcessName;
   }



   /*******************************************************************************
    ** Fluent setter for syncProcessName
    *******************************************************************************/
   public BaseSyncToScheduledJobTableCustomizer withSyncProcessName(String syncProcessName)
   {
      this.syncProcessName = syncProcessName;
      return (this);
   }


   /*******************************************************************************
    ** Getter for KEY_SCHEDULED_JOB_FOREIGN_KEY_TYPE
    *******************************************************************************/
   public String getKEY_SCHEDULED_JOB_FOREIGN_KEY_TYPE()
   {
      return (BaseSyncToScheduledJobTableCustomizer.KEY_SCHEDULED_JOB_FOREIGN_KEY_TYPE);
   }



   /*******************************************************************************
    ** Getter for scheduledJobForeignKeyType
    *******************************************************************************/
   public String getScheduledJobForeignKeyType()
   {
      return (this.scheduledJobForeignKeyType);
   }



   /*******************************************************************************
    ** Setter for scheduledJobForeignKeyType
    *******************************************************************************/
   public void setScheduledJobForeignKeyType(String scheduledJobForeignKeyType)
   {
      this.scheduledJobForeignKeyType = scheduledJobForeignKeyType;
   }



   /*******************************************************************************
    ** Fluent setter for scheduledJobForeignKeyType
    *******************************************************************************/
   public BaseSyncToScheduledJobTableCustomizer withScheduledJobForeignKeyType(String scheduledJobForeignKeyType)
   {
      this.scheduledJobForeignKeyType = scheduledJobForeignKeyType;
      return (this);
   }


}
