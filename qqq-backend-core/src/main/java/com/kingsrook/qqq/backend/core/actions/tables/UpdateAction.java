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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.audits.DMLAuditAction;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationStatusUpdater;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.DMLAuditInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Action to update one or more records.
 **
 *******************************************************************************/
public class UpdateAction
{
   private static final QLogger LOG = QLogger.getLogger(UpdateAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateOutput execute(UpdateInput updateInput) throws QException
   {
      ActionHelper.validateSession(updateInput);
      setAutomationStatusField(updateInput);

      ValueBehaviorApplier.applyFieldBehaviors(updateInput.getInstance(), updateInput.getTable(), updateInput.getRecords());
      // todo - need to handle records with errors coming out of here...

      List<QRecord> oldRecordList = getOldRecordListForAuditIfNeeded(updateInput);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(updateInput.getBackend());

      validateRequiredFields(updateInput);

      // todo pre-customization - just get to modify the request?
      UpdateOutput updateOutput = qModule.getUpdateInterface().execute(updateInput);
      // todo post-customization - can do whatever w/ the result if you want

      List<String> errors = updateOutput.getRecords().stream().flatMap(r -> r.getErrors().stream()).toList();
      if(CollectionUtils.nullSafeHasContents(errors))
      {
         LOG.warn("Errors in updateAction", logPair("tableName", updateInput.getTableName()), logPair("errorCount", errors.size()), errors.size() < 10 ? logPair("errors", errors) : logPair("first10Errors", errors.subList(0, 10)));
      }

      manageAssociations(updateInput);

      if(updateInput.getOmitDmlAudit())
      {
         LOG.debug("Requested to omit DML audit");
      }
      else
      {
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(updateInput).withRecordList(updateOutput.getRecords()).withOldRecordList(oldRecordList));
      }

      return updateOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateRequiredFields(UpdateInput updateInput)
   {
      QTableMetaData table = updateInput.getTable();
      Set<QFieldMetaData> requiredFields = table.getFields().values().stream()
         .filter(f -> f.getIsRequired())
         .collect(Collectors.toSet());

      if(!requiredFields.isEmpty())
      {
         for(QRecord record : updateInput.getRecords())
         {
            for(QFieldMetaData requiredField : requiredFields)
            {
               /////////////////////////////////////////////////////////////////////////////////////////////
               // only consider fields that were set in the record to be updated (e.g., "patch" semantic) //
               /////////////////////////////////////////////////////////////////////////////////////////////
               if(record.getValues().containsKey(requiredField.getName()))
               {
                  if(record.getValue(requiredField.getName()) == null || (requiredField.getType().isStringLike() && record.getValueString(requiredField.getName()).trim().equals("")))
                  {
                     record.addError("Missing value in required field: " + requiredField.getLabel());
                  }
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void manageAssociations(UpdateInput updateInput) throws QException
   {
      QTableMetaData table = updateInput.getTable();
      for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
      {
         // e.g., order -> orderLine
         QTableMetaData associatedTable = QContext.getQInstance().getTable(association.getAssociatedTableName());
         QJoinMetaData  join            = QContext.getQInstance().getJoin(association.getJoinName()); // todo ... ever need to flip?
         // just assume this, at least for now... if(BooleanUtils.isTrue(association.getDoInserts()))

         for(List<QRecord> page : CollectionUtils.getPages(updateInput.getRecords(), 500))
         {
            List<QRecord> nextLevelUpdates  = new ArrayList<>();
            List<QRecord> nextLevelInserts  = new ArrayList<>();
            QQueryFilter  findDeletesFilter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR);
            boolean       lookForDeletes    = false;

            //////////////////////////////////////////////////////
            // for each updated record, look at as associations //
            //////////////////////////////////////////////////////
            for(QRecord record : page)
            {
               if(record.getAssociatedRecords() != null && record.getAssociatedRecords().containsKey(association.getName()))
               {
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // build a sub-query to find the children of this record - and we'll exclude (below) any whose ids are given //
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  QQueryFilter subFilter = new QQueryFilter();
                  findDeletesFilter.addSubFilter(subFilter);
                  lookForDeletes = true;
                  List<Serializable> idsBeingUpdated = new ArrayList<>();
                  for(JoinOn joinOn : join.getJoinOns())
                  {
                     subFilter.addCriteria(new QFilterCriteria(joinOn.getRightField(), QCriteriaOperator.EQUALS, record.getValue(joinOn.getLeftField())));
                  }

                  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // for any associated records present here, figure out if they're being inserted (no primaryKey) or updated //
                  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  for(QRecord associatedRecord : CollectionUtils.nonNullList(record.getAssociatedRecords().get(association.getName())))
                  {
                     Serializable associatedId = associatedRecord.getValue(associatedTable.getPrimaryKeyField());
                     if(associatedId == null)
                     {
                        //////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // if inserting, add to the inserts list, and propagate values from the header record down to the child //
                        //////////////////////////////////////////////////////////////////////////////////////////////////////////
                        for(JoinOn joinOn : join.getJoinOns())
                        {
                           associatedRecord.setValue(joinOn.getRightField(), record.getValue(joinOn.getLeftField()));
                        }
                        nextLevelInserts.add(associatedRecord);
                     }
                     else
                     {
                        ///////////////////////////////////////////////////////////////////////////////
                        // if updating, add to the updates list, and add the id as one to not delete //
                        ///////////////////////////////////////////////////////////////////////////////
                        idsBeingUpdated.add(associatedId);
                        nextLevelUpdates.add(associatedRecord);
                     }
                  }

                  if(!idsBeingUpdated.isEmpty())
                  {
                     ///////////////////////////////////////////////////////////////////////////////
                     // if any records are being updated, add them to the query to NOT be deleted //
                     ///////////////////////////////////////////////////////////////////////////////
                     subFilter.addCriteria(new QFilterCriteria(associatedTable.getPrimaryKeyField(), QCriteriaOperator.NOT_IN, idsBeingUpdated));
                  }
               }
            }

            if(lookForDeletes)
            {
               QueryInput queryInput = new QueryInput();
               queryInput.setTableName(associatedTable.getName());
               queryInput.setFilter(findDeletesFilter);
               QueryOutput queryOutput = new QueryAction().execute(queryInput);
               if(!queryOutput.getRecords().isEmpty())
               {
                  LOG.debug("Deleting associatedRecords", logPair("associatedTable", associatedTable.getName()), logPair("noOfRecords", queryOutput.getRecords().size()));
                  DeleteInput deleteInput = new DeleteInput();
                  deleteInput.setTableName(association.getAssociatedTableName());
                  deleteInput.setPrimaryKeys(queryOutput.getRecords().stream().map(r -> r.getValue(associatedTable.getPrimaryKeyField())).collect(Collectors.toList()));
                  DeleteOutput deleteOutput = new DeleteAction().execute(deleteInput);
               }
            }

            if(CollectionUtils.nullSafeHasContents(nextLevelUpdates))
            {
               LOG.debug("Updating associatedRecords", logPair("associatedTable", associatedTable.getName()), logPair("noOfRecords", nextLevelUpdates.size()));
               UpdateInput nextLevelUpdateInput = new UpdateInput();
               nextLevelUpdateInput.setTableName(association.getAssociatedTableName());
               nextLevelUpdateInput.setRecords(nextLevelUpdates);
               UpdateOutput nextLevelUpdateOutput = new UpdateAction().execute(nextLevelUpdateInput);
            }

            if(CollectionUtils.nullSafeHasContents(nextLevelInserts))
            {
               LOG.debug("Inserting associatedRecords", logPair("associatedTable", associatedTable.getName()), logPair("noOfRecords", nextLevelUpdates.size()));
               InsertInput nextLevelInsertInput = new InsertInput();
               nextLevelInsertInput.setTableName(association.getAssociatedTableName());
               nextLevelInsertInput.setRecords(nextLevelInserts);
               InsertOutput nextLevelInsertOutput = new InsertAction().execute(nextLevelInsertInput);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QRecord> getOldRecordListForAuditIfNeeded(UpdateInput updateInput)
   {
      if(updateInput.getOmitDmlAudit())
      {
         return (null);
      }

      try
      {
         AuditLevel    auditLevel    = DMLAuditAction.getAuditLevel(updateInput);
         List<QRecord> oldRecordList = null;
         if(AuditLevel.FIELD.equals(auditLevel))
         {
            String             primaryKeyField   = updateInput.getTable().getPrimaryKeyField();
            List<Serializable> pkeysBeingUpdated = updateInput.getRecords().stream().map(r -> r.getValue(primaryKeyField)).toList();

            QueryInput queryInput = new QueryInput();
            queryInput.setTableName(updateInput.getTableName());
            queryInput.setFilter(new QQueryFilter(new QFilterCriteria(primaryKeyField, QCriteriaOperator.IN, pkeysBeingUpdated)));
            // todo - need a limit?  what if too many??
            QueryOutput queryOutput = new QueryAction().execute(queryInput);

            oldRecordList = queryOutput.getRecords();
         }
         return oldRecordList;
      }
      catch(Exception e)
      {
         LOG.warn("Error getting old record list for audit", e, logPair("table", updateInput.getTableName()));
         return (null);
      }
   }



   /*******************************************************************************
    ** If the table being updated uses an automation-status field, populate it now.
    *******************************************************************************/
   private void setAutomationStatusField(UpdateInput updateInput)
   {
      RecordAutomationStatusUpdater.setAutomationStatusInRecords(updateInput.getSession(), updateInput.getTable(), updateInput.getRecords(), AutomationStatus.PENDING_UPDATE_AUTOMATIONS);
   }

}
