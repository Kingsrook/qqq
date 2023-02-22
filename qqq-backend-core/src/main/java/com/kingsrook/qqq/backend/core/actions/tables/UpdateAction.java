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
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.audits.DMLAuditAction;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationStatusUpdater;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.DMLAuditInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
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
      // todo pre-customization - just get to modify the request?
      UpdateOutput updateResult = qModule.getUpdateInterface().execute(updateInput);
      // todo post-customization - can do whatever w/ the result if you want

      if(updateInput.getOmitDmlAudit())
      {
         LOG.debug("Requested to omit DML audit");
      }
      else
      {
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(updateInput).withRecordList(updateResult.getRecords()).withOldRecordList(oldRecordList));
      }

      return updateResult;
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
