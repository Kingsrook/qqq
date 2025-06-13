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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.audits.DMLAuditAction;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationStatusUpdater;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPreInsertCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
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
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.DuplicateKeyBadInputStatusMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.QWarningMessage;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
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
   public QRecord executeForRecord(InsertInput insertInput) throws QException
   {
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      return (insertOutput.getRecords().get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QRecord> executeForRecords(InsertInput insertInput) throws QException
   {
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      return (insertOutput.getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertOutput execute(InsertInput insertInput) throws QException
   {
      ActionHelper.validateSession(insertInput);

      if(!StringUtils.hasContent(insertInput.getTableName()))
      {
         throw (new QException("Table name was not specified in insert input"));
      }

      QTableMetaData table = insertInput.getTable();

      if(table == null)
      {
         throw (new QException("Error:  Undefined table: " + insertInput.getTableName()));
      }

      setAutomationStatusField(insertInput);

      /////////////////////////////
      // run standard validators //
      /////////////////////////////
      performValidations(insertInput, false, false);

      //////////////////////////////////////////////////////
      // use the backend module to actually do the insert //
      //////////////////////////////////////////////////////
      InsertOutput insertOutput = runInsertInBackend(insertInput);

      if(insertOutput.getRecords() == null)
      {
         ////////////////////////////////////////////////////////////////////////////////////
         // in case the module failed to set record in the output, put an empty list there //
         // to avoid so many downstream NPE's                                              //
         ////////////////////////////////////////////////////////////////////////////////////
         insertOutput.setRecords(new ArrayList<>());
      }

      //////////////////////////////
      // log if there were errors //
      //////////////////////////////
      List<String> errors = insertOutput.getRecords().stream().flatMap(r -> r.getErrors().stream().map(Object::toString)).toList();
      if(CollectionUtils.nullSafeHasContents(errors))
      {
         LOG.info("Errors in insertAction", logPair("tableName", table.getName()), logPair("errorCount", errors.size()), errors.size() < 10 ? logPair("errors", errors) : logPair("first10Errors", errors.subList(0, 10)));
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
         new DMLAuditAction().execute(new DMLAuditInput()
            .withTableActionInput(insertInput)
            .withAuditContext(insertInput.getAuditContext())
            .withRecordList(insertOutput.getRecords()));
      }

      ////////////////////////////////////////////////////////////////
      // finally, run the post-insert customizers, if there are any //
      ////////////////////////////////////////////////////////////////
      runPostInsertCustomizers(insertInput, table, insertOutput);

      return insertOutput;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void runPostInsertCustomizers(InsertInput insertInput, QTableMetaData table, InsertOutput insertOutput)
   {
      Optional<TableCustomizerInterface> postInsertCustomizer = QCodeLoader.getTableCustomizer(table, TableCustomizers.POST_INSERT_RECORD.getRole());
      if(postInsertCustomizer.isPresent())
      {
         try
         {
            insertOutput.setRecords(postInsertCustomizer.get().postInsert(insertInput, insertOutput.getRecords()));
         }
         catch(Exception e)
         {
            for(QRecord record : insertOutput.getRecords())
            {
               record.addWarning(new QWarningMessage("An error occurred after the insert: " + e.getMessage()));
            }
         }
      }

      ///////////////////////////////////////////////
      // run all of the instance-level customizers //
      ///////////////////////////////////////////////
      List<QCodeReference> tableCustomizerCodes = QContext.getQInstance().getTableCustomizers(TableCustomizers.POST_INSERT_RECORD);
      for(QCodeReference tableCustomizerCode : tableCustomizerCodes)
      {
         try
         {
            TableCustomizerInterface tableCustomizer = QCodeLoader.getAdHoc(TableCustomizerInterface.class, tableCustomizerCode);
            insertOutput.setRecords(tableCustomizer.postInsert(insertInput, insertOutput.getRecords()));
         }
         catch(Exception e)
         {
            for(QRecord record : insertOutput.getRecords())
            {
               record.addWarning(new QWarningMessage("An error occurred after the insert: " + e.getMessage()));
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private InsertOutput runInsertInBackend(InsertInput insertInput) throws QException
   {
      ///////////////////////////////////
      // exit early if 0 input records //
      ///////////////////////////////////
      if(CollectionUtils.nullSafeIsEmpty(insertInput.getRecords()))
      {
         LOG.debug("Insert request called with 0 records.  Returning with no-op", logPair("tableName", insertInput.getTableName()));
         InsertOutput rs = new InsertOutput();
         rs.setRecords(new ArrayList<>());
         return (rs);
      }

      //////////////////////////////////////////////////////
      // load the backend module and its insert interface //
      //////////////////////////////////////////////////////
      QBackendModuleInterface qModule         = getBackendModuleInterface(insertInput.getBackend());
      InsertInterface         insertInterface = qModule.getInsertInterface();

      ////////////////////////////////////
      // have the backend do the insert //
      ////////////////////////////////////
      InsertOutput insertOutput = insertInterface.execute(insertInput);
      return insertOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void performValidations(InsertInput insertInput, boolean isPreview, boolean didAlreadyRunCustomizer) throws QException
   {
      if(CollectionUtils.nullSafeIsEmpty(insertInput.getRecords()))
      {
         return;
      }

      QTableMetaData table = insertInput.getTable();

      ///////////////////////////////////////////////////////////////////
      // load the pre-insert customizer and set it up, if there is one //
      // then we'll run it based on its WhenToRun value                //
      // note - if we already ran it, then don't re-run it!            //
      ///////////////////////////////////////////////////////////////////
      Optional<TableCustomizerInterface> preInsertCustomizer = didAlreadyRunCustomizer ? Optional.empty() : QCodeLoader.getTableCustomizer(table, TableCustomizers.PRE_INSERT_RECORD.getRole());
      runPreInsertCustomizerIfItIsTime(insertInput, isPreview, preInsertCustomizer, AbstractPreInsertCustomizer.WhenToRun.BEFORE_ALL_VALIDATIONS);

      setDefaultValuesInRecords(table, insertInput.getRecords());

      ValueBehaviorApplier.applyFieldBehaviors(ValueBehaviorApplier.Action.INSERT, QContext.getQInstance(), table, insertInput.getRecords(), null);

      runPreInsertCustomizerIfItIsTime(insertInput, isPreview, preInsertCustomizer, AbstractPreInsertCustomizer.WhenToRun.BEFORE_UNIQUE_KEY_CHECKS);
      setErrorsIfUniqueKeyErrors(insertInput, table);

      runPreInsertCustomizerIfItIsTime(insertInput, isPreview, preInsertCustomizer, AbstractPreInsertCustomizer.WhenToRun.BEFORE_REQUIRED_FIELD_CHECKS);
      if(insertInput.getInputSource().shouldValidateRequiredFields())
      {
         validateRequiredFields(insertInput);
      }

      runPreInsertCustomizerIfItIsTime(insertInput, isPreview, preInsertCustomizer, AbstractPreInsertCustomizer.WhenToRun.BEFORE_SECURITY_CHECKS);
      ValidateRecordSecurityLockHelper.validateSecurityFields(insertInput.getTable(), insertInput.getRecords(), ValidateRecordSecurityLockHelper.Action.INSERT, insertInput.getTransaction());

      runPreInsertCustomizerIfItIsTime(insertInput, isPreview, preInsertCustomizer, AbstractPreInsertCustomizer.WhenToRun.AFTER_ALL_VALIDATIONS);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setDefaultValuesInRecords(QTableMetaData table, List<QRecord> records)
   {
      ////////////////////////////////////////////////////////////////////////////////////////////////
      // for all fields in the table - if any have a default value, then look at all input records, //
      // and if they have null value, then apply the default                                        //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      for(QFieldMetaData field : table.getFields().values())
      {
         if(field.getDefaultValue() != null)
         {
            for(QRecord record : records)
            {
               if(record.getValue(field.getName()) == null)
               {
                  record.setValue(field.getName(), field.getDefaultValue());
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runPreInsertCustomizerIfItIsTime(InsertInput insertInput, boolean isPreview, Optional<TableCustomizerInterface> preInsertCustomizer, AbstractPreInsertCustomizer.WhenToRun whenToRun) throws QException
   {
      if(preInsertCustomizer.isPresent())
      {
         if(whenToRun.equals(preInsertCustomizer.get().whenToRunPreInsert(insertInput, isPreview)))
         {
            insertInput.setRecords(preInsertCustomizer.get().preInsert(insertInput, insertInput.getRecords(), isPreview));
         }
      }

      ///////////////////////////////////////////////
      // run all of the instance-level customizers //
      ///////////////////////////////////////////////
      List<QCodeReference> tableCustomizerCodes = QContext.getQInstance().getTableCustomizers(TableCustomizers.PRE_INSERT_RECORD);
      for(QCodeReference tableCustomizerCode : tableCustomizerCodes)
      {
         TableCustomizerInterface tableCustomizer = QCodeLoader.getAdHoc(TableCustomizerInterface.class, tableCustomizerCode);
         if(whenToRun.equals(tableCustomizer.whenToRunPreInsert(insertInput, isPreview)))
         {
            insertInput.setRecords(tableCustomizer.preInsert(insertInput, insertInput.getRecords(), isPreview));
         }
      }
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
                  record.addError(new BadInputStatusMessage("Missing value in required field: " + Objects.requireNonNullElse(requiredField.getLabel(), requiredField.getName())));
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
            if(CollectionUtils.nullSafeHasContents(record.getErrors()))
            {
               continue;
            }

            if(record.getAssociatedRecords() != null && record.getAssociatedRecords().containsKey(association.getName()))
            {
               for(QRecord associatedRecord : CollectionUtils.nonNullList(record.getAssociatedRecords().get(association.getName())))
               {
                  for(JoinOn joinOn : join.getJoinOns())
                  {
                     QFieldType type = table.getField(joinOn.getLeftField()).getType();
                     associatedRecord.setValue(joinOn.getRightField(), ValueUtils.getValueAsFieldType(type, record.getValue(joinOn.getLeftField())));
                  }
                  nextLevelInserts.add(associatedRecord);
               }
            }
         }

         if(CollectionUtils.nullSafeHasContents(nextLevelInserts))
         {
            InsertInput nextLevelInsertInput = new InsertInput();
            nextLevelInsertInput.setTransaction(transaction);
            nextLevelInsertInput.setTableName(association.getAssociatedTableName());
            nextLevelInsertInput.setRecords(nextLevelInserts);
            InsertOutput nextLevelInsertOutput = new InsertAction().execute(nextLevelInsertInput);
         }
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
                  record.addError(new DuplicateKeyBadInputStatusMessage("Another record already exists with this " + uniqueKey.getDescription(table)));
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
      RecordAutomationStatusUpdater.setAutomationStatusInRecords(insertInput.getTable(), insertInput.getRecords(), AutomationStatus.PENDING_INSERT_AUTOMATIONS, insertInput.getTransaction(), null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QBackendModuleInterface getBackendModuleInterface(QBackendMetaData backend) throws QException
   {
      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(backend);
      return (qModule);
   }

}
