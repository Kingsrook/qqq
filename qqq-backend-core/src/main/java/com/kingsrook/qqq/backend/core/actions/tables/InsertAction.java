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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.audits.DMLAuditAction;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationStatusUpdater;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPostInsertCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPreInsertCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.UniqueKeyHelper;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ValidateRecordSecurityLockHelper;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.DMLAuditInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Action to insert one or more records.
 **
 *******************************************************************************/
public class InsertAction extends AbstractQActionFunction<InsertInput, InsertOutput>
{
   private static final QLogger LOG = QLogger.getLogger(InsertAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertOutput execute(InsertInput insertInput) throws QException
   {
      ActionHelper.validateSession(insertInput);
      QTableMetaData table = insertInput.getTable();

      if(table == null)
      {
         throw (new QException("Error:  Undefined table: " + insertInput.getTableName()));
      }

      setAutomationStatusField(insertInput);

      //////////////////////////////////////////////////////
      // load the backend module and its insert interface //
      //////////////////////////////////////////////////////
      QBackendModuleInterface qModule         = getBackendModuleInterface(insertInput);
      InsertInterface         insertInterface = qModule.getInsertInterface();

      /////////////////////////////
      // run standard validators //
      /////////////////////////////
      ValueBehaviorApplier.applyFieldBehaviors(insertInput.getInstance(), table, insertInput.getRecords());
      setErrorsIfUniqueKeyErrors(insertInput, table);
      validateRequiredFields(insertInput);
      ValidateRecordSecurityLockHelper.validateSecurityFields(insertInput.getTable(), insertInput.getRecords(), ValidateRecordSecurityLockHelper.Action.INSERT);

      ///////////////////////////////////////////////////////////////////////////
      // after all validations, run the pre-insert customizer, if there is one //
      ///////////////////////////////////////////////////////////////////////////
      Optional<AbstractPreInsertCustomizer> preInsertCustomizer = QCodeLoader.getTableCustomizer(AbstractPreInsertCustomizer.class, table, TableCustomizers.PRE_INSERT_RECORD.getRole());
      if(preInsertCustomizer.isPresent())
      {
         preInsertCustomizer.get().setInsertInput(insertInput);
         insertInput.setRecords(preInsertCustomizer.get().apply(insertInput.getRecords()));
      }

      ////////////////////////////////////
      // have the backend do the insert //
      ////////////////////////////////////
      InsertOutput insertOutput = insertInterface.execute(insertInput);

      //////////////////////////////
      // log if there were errors //
      //////////////////////////////
      List<String> errors = insertOutput.getRecords().stream().flatMap(r -> r.getErrors().stream()).toList();
      if(CollectionUtils.nullSafeHasContents(errors))
      {
         LOG.warn("Errors in insertAction", logPair("tableName", table.getName()), logPair("errorCount", errors.size()), errors.size() < 10 ? logPair("errors", errors) : logPair("first10Errors", errors.subList(0, 10)));
      }

      //////////////////////////////////////////////////
      // insert any associations in the input records //
      //////////////////////////////////////////////////
      manageAssociations(table, insertOutput.getRecords(), insertInput.getTransaction());

      //////////////////
      // do the audit //
      //////////////////
      if(insertInput.getOmitDmlAudit())
      {
         LOG.debug("Requested to omit DML audit");
      }
      else
      {
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(insertInput).withRecordList(insertOutput.getRecords()));
      }

      /////////////////////////////////////////////////////////////
      // finally, run the pre-insert customizer, if there is one //
      /////////////////////////////////////////////////////////////
      Optional<AbstractPostInsertCustomizer> postInsertCustomizer = QCodeLoader.getTableCustomizer(AbstractPostInsertCustomizer.class, table, TableCustomizers.POST_INSERT_RECORD.getRole());
      if(postInsertCustomizer.isPresent())
      {
         try
         {
            postInsertCustomizer.get().setInsertInput(insertInput);
            insertOutput.setRecords(postInsertCustomizer.get().apply(insertOutput.getRecords()));
         }
         catch(Exception e)
         {
            for(QRecord record : insertOutput.getRecords())
            {
               record.addWarning("An error occurred after the insert: " + e.getMessage());
            }
         }
      }

      return insertOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateRequiredFields(InsertInput insertInput)
   {
      QTableMetaData table = insertInput.getTable();
      Set<QFieldMetaData> requiredFields = table.getFields().values().stream()
         .filter(f -> f.getIsRequired())
         .collect(Collectors.toSet());

      if(!requiredFields.isEmpty())
      {
         for(QRecord record : insertInput.getRecords())
         {
            for(QFieldMetaData requiredField : requiredFields)
            {
               if(record.getValue(requiredField.getName()) == null || (requiredField.getType().isStringLike() && record.getValueString(requiredField.getName()).trim().equals("")))
               {
                  record.addError("Missing value in required field: " + requiredField.getLabel());
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void manageAssociations(QTableMetaData table, List<QRecord> insertedRecords, QBackendTransaction transaction) throws QException
   {
      for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
      {
         // e.g., order -> orderLine
         QJoinMetaData join = QContext.getQInstance().getJoin(association.getJoinName()); // todo ... ever need to flip?
         // just assume this, at least for now... if(BooleanUtils.isTrue(association.getDoInserts()))

         List<QRecord> nextLevelInserts = new ArrayList<>();
         for(QRecord record : insertedRecords)
         {
            if(record.getAssociatedRecords() != null && record.getAssociatedRecords().containsKey(association.getName()))
            {
               for(QRecord associatedRecord : CollectionUtils.nonNullList(record.getAssociatedRecords().get(association.getName())))
               {
                  for(JoinOn joinOn : join.getJoinOns())
                  {
                     associatedRecord.setValue(joinOn.getRightField(), record.getValue(joinOn.getLeftField()));
                  }
                  nextLevelInserts.add(associatedRecord);
               }
            }
         }

         InsertInput nextLevelInsertInput = new InsertInput();
         nextLevelInsertInput.setTransaction(transaction);
         nextLevelInsertInput.setTableName(association.getAssociatedTableName());
         nextLevelInsertInput.setRecords(nextLevelInserts);
         InsertOutput nextLevelInsertOutput = new InsertAction().execute(nextLevelInsertInput);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setErrorsIfUniqueKeyErrors(InsertInput insertInput, QTableMetaData table) throws QException
   {
      if(CollectionUtils.nullSafeHasContents(table.getUniqueKeys()))
      {
         Map<UniqueKey, Set<List<Serializable>>> keysInThisList = new HashMap<>();
         if(insertInput.getSkipUniqueKeyCheck())
         {
            LOG.debug("Skipping unique key check in " + insertInput.getTableName() + " insert.");
            return;
         }

         ////////////////////////////////////////////
         // check for any pre-existing unique keys //
         ////////////////////////////////////////////
         Map<UniqueKey, Set<List<Serializable>>> existingKeys = new HashMap<>();
         List<UniqueKey>                         uniqueKeys   = CollectionUtils.nonNullList(table.getUniqueKeys());
         for(UniqueKey uniqueKey : uniqueKeys)
         {
            existingKeys.put(uniqueKey, UniqueKeyHelper.getExistingKeys(insertInput.getTransaction(), table, insertInput.getRecords(), uniqueKey).keySet());
         }

         /////////////////////////////////////
         // make sure this map is populated //
         /////////////////////////////////////
         uniqueKeys.forEach(uk -> keysInThisList.computeIfAbsent(uk, x -> new HashSet<>()));

         for(QRecord record : insertInput.getRecords())
         {
            //////////////////////////////////////////////////////////
            // check if this record violates any of the unique keys //
            //////////////////////////////////////////////////////////
            boolean foundDupe = false;
            for(UniqueKey uniqueKey : uniqueKeys)
            {
               Optional<List<Serializable>> keyValues = UniqueKeyHelper.getKeyValues(table, uniqueKey, record);
               if(keyValues.isPresent() && (existingKeys.get(uniqueKey).contains(keyValues.get()) || keysInThisList.get(uniqueKey).contains(keyValues.get())))
               {
                  record.addError("Another record already exists with this " + uniqueKey.getDescription(table));
                  foundDupe = true;
                  break;
               }
            }

            ///////////////////////////////////////////////////////////////////////////////
            // if this record doesn't violate any uk's, then we can add it to the output //
            ///////////////////////////////////////////////////////////////////////////////
            if(!foundDupe)
            {
               for(UniqueKey uniqueKey : uniqueKeys)
               {
                  Optional<List<Serializable>> keyValues = UniqueKeyHelper.getKeyValues(table, uniqueKey, record);
                  keyValues.ifPresent(kv -> keysInThisList.get(uniqueKey).add(kv));
               }
            }
         }
      }
   }



   /*******************************************************************************
    ** If the table being inserted into uses an automation-status field, populate it now.
    *******************************************************************************/
   private void setAutomationStatusField(InsertInput insertInput)
   {
      RecordAutomationStatusUpdater.setAutomationStatusInRecords(insertInput.getSession(), insertInput.getTable(), insertInput.getRecords(), AutomationStatus.PENDING_INSERT_AUTOMATIONS);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QBackendModuleInterface getBackendModuleInterface(InsertInput insertInput) throws QException
   {
      ActionHelper.validateSession(insertInput);
      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(insertInput.getBackend());
      return (qModule);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendTransaction openTransaction(InsertInput insertInput) throws QException
   {
      QBackendModuleInterface qModule = getBackendModuleInterface(insertInput);
      return (qModule.getInsertInterface().openTransaction(insertInput));
   }

}
