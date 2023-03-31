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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.audits.DMLAuditAction;
import com.kingsrook.qqq.backend.core.actions.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.DMLAuditInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Action to delete 1 or more records.
 **
 *******************************************************************************/
public class DeleteAction
{
   private static final QLogger LOG = QLogger.getLogger(DeleteAction.class);

   public static final String NOT_FOUND_ERROR_PREFIX = "No record was found to delete";



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteOutput execute(DeleteInput deleteInput) throws QException
   {
      ActionHelper.validateSession(deleteInput);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(deleteInput.getBackend());

      if(CollectionUtils.nullSafeHasContents(deleteInput.getPrimaryKeys()) && deleteInput.getQueryFilter() != null)
      {
         throw (new QException("A delete request may not contain both a list of primary keys and a query filter."));
      }

      DeleteInterface deleteInterface = qModule.getDeleteInterface();
      if(deleteInput.getQueryFilter() != null && !deleteInterface.supportsQueryFilterInput())
      {
         LOG.info("Querying for primary keys, for backend module " + qModule.getBackendType() + " which does not support queryFilter input for deletes");
         List<Serializable> primaryKeyList = getPrimaryKeysFromQueryFilter(deleteInput);
         deleteInput.setPrimaryKeys(primaryKeyList);

         if(primaryKeyList.isEmpty())
         {
            LOG.info("0 primaryKeys found.  Returning with no-op");
            DeleteOutput deleteOutput = new DeleteOutput();
            deleteOutput.setRecordsWithErrors(new ArrayList<>());
            deleteOutput.setDeletedRecordCount(0);
            return (deleteOutput);
         }
      }

      List<QRecord> recordListForAudit          = getRecordListForAuditIfNeeded(deleteInput);
      List<QRecord> recordsWithValidationErrors = validateRecordsExistAndCanBeAccessed(deleteInput, recordListForAudit);

      DeleteOutput deleteOutput = deleteInterface.execute(deleteInput);

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // merge the backend's output with any validation errors we found (whose ids wouldn't have gotten into the backend delete) //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      List<QRecord> outputRecordsWithErrors = deleteOutput.getRecordsWithErrors();
      if(outputRecordsWithErrors == null)
      {
         deleteOutput.setRecordsWithErrors(new ArrayList<>());
         outputRecordsWithErrors = deleteOutput.getRecordsWithErrors();
      }

      outputRecordsWithErrors.addAll(recordsWithValidationErrors);

      manageAssociations(deleteInput);

      new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(deleteInput).withRecordList(recordListForAudit));

      return deleteOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void manageAssociations(DeleteInput deleteInput) throws QException
   {
      QTableMetaData table = deleteInput.getTable();
      for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
      {
         // e.g., order -> orderLine
         QJoinMetaData join = QContext.getQInstance().getJoin(association.getJoinName()); // todo ... ever need to flip?
         // just assume this, at least for now... if(BooleanUtils.isTrue(association.getDoInserts()))

         QQueryFilter filter = new QQueryFilter();

         if(join.getJoinOns().size() == 1 && join.getJoinOns().get(0).getLeftField().equals(table.getPrimaryKeyField()))
         {
            filter.addCriteria(new QFilterCriteria(join.getJoinOns().get(0).getRightField(), QCriteriaOperator.IN, deleteInput.getPrimaryKeys()));
         }
         else
         {
            throw (new QException("Join of this type is not supported for an associated delete at this time..."));
         }

         QTableMetaData associatedTable = QContext.getQInstance().getTable(association.getAssociatedTableName());

         QueryInput queryInput = new QueryInput();
         queryInput.setTableName(association.getAssociatedTableName());
         queryInput.setFilter(filter);
         QueryOutput        queryOutput    = new QueryAction().execute(queryInput);
         List<Serializable> associatedKeys = queryOutput.getRecords().stream().map(r -> r.getValue(associatedTable.getPrimaryKeyField())).toList();

         DeleteInput nextLevelDeleteInput = new DeleteInput();
         nextLevelDeleteInput.setTableName(association.getAssociatedTableName());
         nextLevelDeleteInput.setPrimaryKeys(associatedKeys);
         DeleteOutput nextLevelDeleteOutput = new DeleteAction().execute(nextLevelDeleteInput);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QRecord> getRecordListForAuditIfNeeded(DeleteInput deleteInput) throws QException
   {
      List<QRecord> recordListForAudit = null;

      AuditLevel auditLevel = DMLAuditAction.getAuditLevel(deleteInput);
      if(AuditLevel.RECORD.equals(auditLevel) || AuditLevel.FIELD.equals(auditLevel))
      {
         List<Serializable> primaryKeyList = deleteInput.getPrimaryKeys();
         if(CollectionUtils.nullSafeIsEmpty(deleteInput.getPrimaryKeys()) && deleteInput.getQueryFilter() != null)
         {
            primaryKeyList = getPrimaryKeysFromQueryFilter(deleteInput);
         }

         if(CollectionUtils.nullSafeHasContents(primaryKeyList))
         {
            if(CollectionUtils.nullSafeHasContents(deleteInput.getTable().getRecordSecurityLocks()))
            {
               ////////////////////////////////////////////////////////////////////////////////////////////////////////
               // if the table has any security locks, then we need full entities (to record the keys), not just ids //
               ////////////////////////////////////////////////////////////////////////////////////////////////////////
               QueryInput queryInput = new QueryInput();
               queryInput.setTableName(deleteInput.getTableName());
               queryInput.setFilter(new QQueryFilter(new QFilterCriteria(deleteInput.getTable().getPrimaryKeyField(), QCriteriaOperator.IN, primaryKeyList)));
               QueryOutput queryOutput = new QueryAction().execute(queryInput);
               recordListForAudit = queryOutput.getRecords();
            }
            else
            {
               recordListForAudit = primaryKeyList.stream().map(pk -> new QRecord().withValue(deleteInput.getTable().getPrimaryKeyField(), pk)).toList();
            }
         }
      }

      return (recordListForAudit);
   }



   /*******************************************************************************
    ** Note - the "can be accessed" part of this method name - it implies that
    ** records that you can't see because of security - that they won't be found
    ** by the query here, so it's the same to you as if they don't exist at all!
    **
    ** This method, if it finds any missing records, will:
    ** - remove those ids from the deleteInput
    ** - create a QRecord with that id and a not-found error message.
    *******************************************************************************/
   private List<QRecord> validateRecordsExistAndCanBeAccessed(DeleteInput deleteInput, List<QRecord> oldRecordList) throws QException
   {
      List<QRecord> recordsWithErrors = new ArrayList<>();

      QTableMetaData table           = deleteInput.getTable();
      QFieldMetaData primaryKeyField = table.getField(table.getPrimaryKeyField());

      Set<Serializable> primaryKeysToRemoveFromInput = new HashSet<>();

      List<List<Serializable>> pages = CollectionUtils.getPages(deleteInput.getPrimaryKeys(), 1000);
      for(List<Serializable> page : pages)
      {
         List<Serializable> primaryKeysToLookup = new ArrayList<>();
         for(Serializable primaryKeyValue : page)
         {
            if(primaryKeyValue != null)
            {
               primaryKeysToLookup.add(primaryKeyValue);
            }
         }

         Map<Serializable, QRecord> lookedUpRecords = new HashMap<>();
         if(CollectionUtils.nullSafeHasContents(oldRecordList))
         {
            for(QRecord record : oldRecordList)
            {
               lookedUpRecords.put(record.getValue(table.getPrimaryKeyField()), record);
            }
         }
         else if(!primaryKeysToLookup.isEmpty())
         {
            QueryInput queryInput = new QueryInput();
            queryInput.setTableName(table.getName());
            queryInput.setFilter(new QQueryFilter(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, primaryKeysToLookup)));
            QueryOutput queryOutput = new QueryAction().execute(queryInput);
            for(QRecord record : queryOutput.getRecords())
            {
               lookedUpRecords.put(record.getValue(table.getPrimaryKeyField()), record);
            }
         }

         for(Serializable primaryKeyValue : page)
         {
            primaryKeyValue = ValueUtils.getValueAsFieldType(primaryKeyField.getType(), primaryKeyValue);
            if(!lookedUpRecords.containsKey(primaryKeyValue))
            {
               QRecord recordWithError = new QRecord();
               recordsWithErrors.add(recordWithError);
               recordWithError.setValue(primaryKeyField.getName(), primaryKeyValue);
               recordWithError.addError(NOT_FOUND_ERROR_PREFIX + " for " + primaryKeyField.getLabel() + " = " + primaryKeyValue);
               primaryKeysToRemoveFromInput.add(primaryKeyValue);
            }
         }

         /////////////////////////////////////////////////////////////////
         // do one mass removal of any bad keys from the input key list //
         /////////////////////////////////////////////////////////////////
         if(!primaryKeysToRemoveFromInput.isEmpty())
         {
            deleteInput.getPrimaryKeys().removeAll(primaryKeysToRemoveFromInput);
            primaryKeysToRemoveFromInput.clear();
         }
      }

      return (recordsWithErrors);
   }



   /*******************************************************************************
    ** For an implementation that doesn't support a queryFilter as its input,
    ** but a scenario where a query filter was passed in - run the query, to
    ** get a list of primary keys.
    *******************************************************************************/
   public static List<Serializable> getPrimaryKeysFromQueryFilter(DeleteInput deleteInput) throws QException
   {
      try
      {
         QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
         QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(deleteInput.getBackend());

         QueryInput queryInput = new QueryInput();
         queryInput.setTableName(deleteInput.getTableName());
         queryInput.setFilter(deleteInput.getQueryFilter());
         QueryOutput queryOutput = qModule.getQueryInterface().execute(queryInput);

         return (queryOutput.getRecords().stream()
            .map(r -> r.getValue(deleteInput.getTable().getPrimaryKeyField()))
            .toList());
      }
      catch(Exception e)
      {
         LOG.warn("Error getting primary keys from query filter before bulk-delete", e);
         throw (new QException("Error getting keys from filter prior to delete.", e));
      }
   }

}
