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
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.UniqueKeyHelper;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.DMLAuditInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
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

      Optional<AbstractPostInsertCustomizer> postInsertCustomizer = QCodeLoader.getTableCustomizer(AbstractPostInsertCustomizer.class, table, TableCustomizers.POST_INSERT_RECORD.getRole());
      setAutomationStatusField(insertInput);

      QBackendModuleInterface qModule = getBackendModuleInterface(insertInput);
      // todo pre-customization - just get to modify the request?

      ValueBehaviorApplier.applyFieldBehaviors(insertInput.getInstance(), table, insertInput.getRecords());
      setErrorsIfUniqueKeyErrors(insertInput, table);
      validateRequiredFields(insertInput);
      validateSecurityFields(insertInput);

      InsertOutput insertOutput = qModule.getInsertInterface().execute(insertInput);
      List<String> errors       = insertOutput.getRecords().stream().flatMap(r -> r.getErrors().stream()).toList();
      if(CollectionUtils.nullSafeHasContents(errors))
      {
         LOG.warn("Errors in insertAction", logPair("tableName", table.getName()), logPair("errorCount", errors.size()), errors.size() < 10 ? logPair("errors", errors) : logPair("first10Errors", errors.subList(0, 10)));
      }

      manageAssociations(table, insertOutput.getRecords());

      // todo post-customization - can do whatever w/ the result if you want

      new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(insertInput).withRecordList(insertOutput.getRecords()));

      if(postInsertCustomizer.isPresent())
      {
         postInsertCustomizer.get().setInsertInput(insertInput);
         insertOutput.setRecords(postInsertCustomizer.get().apply(insertOutput.getRecords()));
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
   private void validateSecurityFields(InsertInput insertInput) throws QException
   {
      QTableMetaData           table               = insertInput.getTable();
      List<RecordSecurityLock> recordSecurityLocks = table.getRecordSecurityLocks();
      List<RecordSecurityLock> locksToCheck        = new ArrayList<>();

      ////////////////////////////////////////
      // if there are no locks, just return //
      ////////////////////////////////////////
      if(CollectionUtils.nullSafeIsEmpty(recordSecurityLocks))
      {
         return;
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // decide if any locks need checked - where one may not need checked if it has an all-access key, and the user has all-access //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(RecordSecurityLock recordSecurityLock : recordSecurityLocks)
      {
         QSecurityKeyType securityKeyType = QContext.getQInstance().getSecurityKeyType(recordSecurityLock.getSecurityKeyType());
         if(StringUtils.hasContent(securityKeyType.getAllAccessKeyName()) && QContext.getQSession().hasSecurityKeyValue(securityKeyType.getAllAccessKeyName(), true, QFieldType.BOOLEAN))
         {
            LOG.debug("Session has " + securityKeyType.getAllAccessKeyName() + " - not checking this lock.");
         }
         else
         {
            locksToCheck.add(recordSecurityLock);
         }
      }

      /////////////////////////////////////////////////
      // if there are no locks to check, just return //
      /////////////////////////////////////////////////
      if(locksToCheck.isEmpty())
      {
         return;
      }

      ////////////////////////////////
      // actually check lock values //
      ////////////////////////////////
      for(RecordSecurityLock recordSecurityLock : locksToCheck)
      {
         if(CollectionUtils.nullSafeIsEmpty(recordSecurityLock.getJoinNameChain()))
         {
            for(QRecord record : insertInput.getRecords())
            {
               /////////////////////////////////////////////////////////////////////////
               // handle the value being in the table we're inserting (e.g., no join) //
               /////////////////////////////////////////////////////////////////////////
               QFieldMetaData field               = table.getField(recordSecurityLock.getFieldName());
               Serializable   recordSecurityValue = record.getValue(recordSecurityLock.getFieldName());
               validateRecordSecurityValue(table, record, recordSecurityLock, recordSecurityValue, field.getType());
            }
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // else look for the joined record - if it isn't found, assume a fail - else validate security value if found //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            QJoinMetaData  leftMostJoin      = QContext.getQInstance().getJoin(recordSecurityLock.getJoinNameChain().get(0));
            QJoinMetaData  rightMostJoin     = QContext.getQInstance().getJoin(recordSecurityLock.getJoinNameChain().get(recordSecurityLock.getJoinNameChain().size() - 1));
            QTableMetaData leftMostJoinTable = QContext.getQInstance().getTable(leftMostJoin.getLeftTable());

            for(List<QRecord> inputRecordPage : CollectionUtils.getPages(insertInput.getRecords(), 500))
            {
               ////////////////////////////////////////////////////////////////////////////////////////////////
               // set up a query for joined records                                                          //
               // query will be like (fkey1=? and fkey2=?) OR (fkey1=? and fkey2=?) OR (fkey1=? and fkey2=?) //
               ////////////////////////////////////////////////////////////////////////////////////////////////
               QueryInput queryInput = new QueryInput();
               queryInput.setTableName(leftMostJoin.getLeftTable());
               QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR);
               queryInput.setFilter(filter);

               for(String joinName : recordSecurityLock.getJoinNameChain())
               {
                  ///////////////////////////////////////
                  // we don't need the right-most join //
                  ///////////////////////////////////////
                  if(!joinName.equals(rightMostJoin.getName()))
                  {
                     queryInput.withQueryJoin(new QueryJoin().withJoinMetaData(QContext.getQInstance().getJoin(joinName)).withSelect(true));
                  }
               }

               ///////////////////////////////////////////////////////////////////////////////////////////////////
               // foreach input record (in this page), put it in a listing hash, with key = list of join-values //
               // e.g., (17,47)=(QRecord1), (18,48)=(QRecord2,QRecord3)                                         //
               // also build up the query's sub-filters here (only adding them if they're unique).              //
               // e.g., 2 order-lines referencing the same orderId don't need to be added to the query twice    //
               ///////////////////////////////////////////////////////////////////////////////////////////////////
               ListingHash<List<Serializable>, QRecord> inputRecordMapByJoinFields = new ListingHash<>();
               for(QRecord inputRecord : inputRecordPage)
               {
                  List<Serializable> inputRecordJoinValues = new ArrayList<>();
                  QQueryFilter       subFilter             = new QQueryFilter();

                  for(JoinOn joinOn : rightMostJoin.getJoinOns())
                  {
                     Serializable inputRecordValue = inputRecord.getValue(joinOn.getRightField());
                     inputRecordJoinValues.add(inputRecordValue);

                     subFilter.addCriteria(inputRecordValue == null
                        ? new QFilterCriteria(rightMostJoin.getLeftTable() + "." + joinOn.getLeftField(), QCriteriaOperator.IS_BLANK)
                        : new QFilterCriteria(rightMostJoin.getLeftTable() + "." + joinOn.getLeftField(), QCriteriaOperator.EQUALS, inputRecordValue));
                  }

                  if(!inputRecordMapByJoinFields.containsKey(inputRecordJoinValues))
                  {
                     ////////////////////////////////////////////////////////////////////////////////
                     // only add this sub-filter if it's for a list of keys we haven't seen before //
                     ////////////////////////////////////////////////////////////////////////////////
                     filter.addSubFilter(subFilter);
                  }

                  inputRecordMapByJoinFields.add(inputRecordJoinValues, inputRecord);
               }

               //////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // execute the query for joined records - then put them in a map with keys corresponding to the join values //
               // e.g., (17,47)=(JoinRecord), (18,48)=(JoinRecord)                                                         //
               //////////////////////////////////////////////////////////////////////////////////////////////////////////////
               QueryOutput                      queryOutput               = new QueryAction().execute(queryInput);
               Map<List<Serializable>, QRecord> joinRecordMapByJoinFields = new HashMap<>();
               for(QRecord joinRecord : queryOutput.getRecords())
               {
                  List<Serializable> joinRecordValues = new ArrayList<>();
                  for(JoinOn joinOn : rightMostJoin.getJoinOns())
                  {
                     Serializable joinValue = joinRecord.getValue(rightMostJoin.getLeftTable() + "." + joinOn.getLeftField());
                     if(joinValue == null && joinRecord.getValues().keySet().stream().anyMatch(n -> !n.contains(".")))
                     {
                        joinValue = joinRecord.getValue(joinOn.getLeftField());
                     }
                     joinRecordValues.add(joinValue);
                  }

                  joinRecordMapByJoinFields.put(joinRecordValues, joinRecord);
               }

               //////////////////////////////////////////////////////////////////////////////////////////////////
               // now for each input record, look for its joinRecord - if it isn't found, then this insert     //
               // isn't allowed.  if it is found, then validate its value matches this session's security keys //
               //////////////////////////////////////////////////////////////////////////////////////////////////
               for(Map.Entry<List<Serializable>, List<QRecord>> entry : inputRecordMapByJoinFields.entrySet())
               {
                  List<Serializable> inputRecordJoinValues = entry.getKey();
                  List<QRecord>      inputRecords          = entry.getValue();
                  if(joinRecordMapByJoinFields.containsKey(inputRecordJoinValues))
                  {
                     QRecord joinRecord = joinRecordMapByJoinFields.get(inputRecordJoinValues);

                     String         fieldName           = recordSecurityLock.getFieldName().replaceFirst(".*\\.", "");
                     QFieldMetaData field               = leftMostJoinTable.getField(fieldName);
                     Serializable   recordSecurityValue = joinRecord.getValue(fieldName);
                     if(recordSecurityValue == null && joinRecord.getValues().keySet().stream().anyMatch(n -> n.contains(".")))
                     {
                        recordSecurityValue = joinRecord.getValue(recordSecurityLock.getFieldName());
                     }

                     for(QRecord inputRecord : inputRecords)
                     {
                        validateRecordSecurityValue(table, inputRecord, recordSecurityLock, recordSecurityValue, field.getType());
                     }
                  }
                  else
                  {
                     for(QRecord inputRecord : inputRecords)
                     {
                        if(RecordSecurityLock.NullValueBehavior.DENY.equals(recordSecurityLock.getNullValueBehavior()))
                        {
                           inputRecord.addError("You do not have permission to insert this record - the referenced " + leftMostJoinTable.getLabel() + " was not found.");
                        }
                     }
                  }
               }
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void validateRecordSecurityValue(QTableMetaData table, QRecord record, RecordSecurityLock recordSecurityLock, Serializable recordSecurityValue, QFieldType fieldType)
   {
      if(recordSecurityValue == null)
      {
         /////////////////////////////////////////////////////////////////
         // handle null values - error if the NullValueBehavior is DENY //
         /////////////////////////////////////////////////////////////////
         if(RecordSecurityLock.NullValueBehavior.DENY.equals(recordSecurityLock.getNullValueBehavior()))
         {
            String lockLabel = CollectionUtils.nullSafeHasContents(recordSecurityLock.getJoinNameChain()) ? recordSecurityLock.getSecurityKeyType() : table.getField(recordSecurityLock.getFieldName()).getLabel();
            record.addError("You do not have permission to insert a record without a value in the field: " + lockLabel);
         }
      }
      else
      {
         if(!QContext.getQSession().hasSecurityKeyValue(recordSecurityLock.getSecurityKeyType(), recordSecurityValue, fieldType))
         {
            if(CollectionUtils.nullSafeHasContents(recordSecurityLock.getJoinNameChain()))
            {
               ///////////////////////////////////////////////////////////////////////////////////////////////
               // avoid telling the user a value from a foreign record that they didn't pass in themselves. //
               ///////////////////////////////////////////////////////////////////////////////////////////////
               record.addError("You do not have permission to insert this record.");
            }
            else
            {
               QFieldMetaData field = table.getField(recordSecurityLock.getFieldName());
               record.addError("You do not have permission to insert a record with a value of " + recordSecurityValue + " in the field: " + field.getLabel());
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void manageAssociations(QTableMetaData table, List<QRecord> insertedRecords) throws QException
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
            existingKeys.put(uniqueKey, UniqueKeyHelper.getExistingKeys(insertInput, insertInput.getTransaction(), table, insertInput.getRecords(), uniqueKey));
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
