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
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.audits.DMLAuditAction;
import com.kingsrook.qqq.backend.core.actions.interfaces.DeleteInterface;
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
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Action to delete 1 or more records.
 **
 *******************************************************************************/
public class DeleteAction
{
   private static final QLogger LOG = QLogger.getLogger(DeleteAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteOutput execute(DeleteInput deleteInput) throws QException
   {
      ActionHelper.validateSession(deleteInput);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(deleteInput.getBackend());
      // todo pre-customization - just get to modify the request?

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

      List<QRecord> recordListForAudit = getRecordListForAuditIfNeeded(deleteInput);

      DeleteOutput deleteOutput = deleteInterface.execute(deleteInput);
      // todo post-customization - can do whatever w/ the result if you want

      new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(deleteInput).withRecordList(recordListForAudit));

      return deleteOutput;
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
