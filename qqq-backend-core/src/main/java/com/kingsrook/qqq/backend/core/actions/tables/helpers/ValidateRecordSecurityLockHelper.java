/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.tables.helpers;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
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
import com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.NullValueBehaviorUtil;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLockFilters;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.statusmessages.PermissionDeniedMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.QErrorMessage;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ValidateRecordSecurityLockHelper
{
   private static final QLogger LOG = QLogger.getLogger(ValidateRecordSecurityLockHelper.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum Action
   {
      INSERT,
      UPDATE,
      DELETE,
      SELECT
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void validateSecurityFields(QTableMetaData table, List<QRecord> records, Action action) throws QException
   {
      MultiRecordSecurityLock locksToCheck = getRecordSecurityLocks(table, action);
      if(locksToCheck == null || CollectionUtils.nullSafeIsEmpty(locksToCheck.getLocks()))
      {
         return;
      }

      //////////////////////////////////////////////////////////////////////////////////////////
      // we will be relying on primary keys being set in records - but (at least for inserts) //
      // we might not have pkeys - so make them up (and clear them out at the end)            //
      //////////////////////////////////////////////////////////////////////////////////////////
      Map<Serializable, QRecord> madeUpPrimaryKeys = makeUpPrimaryKeysIfNeeded(records, table);

      ////////////////////////////////
      // actually check lock values //
      ////////////////////////////////
      Map<Serializable, RecordWithErrors> errorRecords = new HashMap<>();
      evaluateRecordLocks(table, records, action, locksToCheck, errorRecords, new ArrayList<>());

      /////////////////////////////////
      // propagate errors to records //
      /////////////////////////////////
      for(RecordWithErrors recordWithErrors : errorRecords.values())
      {
         recordWithErrors.propagateErrorsToRecord(locksToCheck);
      }

      /////////////////////////////////
      // remove made-up primary keys //
      /////////////////////////////////
      String primaryKeyField = table.getPrimaryKeyField();
      for(QRecord record : madeUpPrimaryKeys.values())
      {
         record.setValue(primaryKeyField, null);
      }
   }



   /*******************************************************************************
    ** For a list of `records` from a `table`, and a given `action`, evaluate a
    ** `recordSecurityLock` (which may be a multi-lock) - populating the input map
    ** of `errorRecords` - key'ed by primary key value (real or made up), with
    ** error messages existing in a tree, with positions matching the multi-lock
    ** tree that we're navigating, as tracked by `treePosition`.
    **
    ** Recursively processes multi-locks (and the top-level call is always with a
    ** multi-lock - as the table's recordLocks list converted to an AND-multi-lock).
    **
    ** Of note - for the case of READ_WRITE locks, we're only evaluating the values
    ** on the record, to see if they're allowed for us to store (because if we didn't
    ** have the key, we wouldn't have been able to read the value (which is verified
    ** outside of here, in UpdateAction/DeleteAction).
    **
    ** BUT - WRITE locks - in their case, we read the record no matter what, and in
    ** here we need to verify we have a key that allows us to WRITE the record.
    *******************************************************************************/
   private static void evaluateRecordLocks(QTableMetaData table, List<QRecord> records, Action action, RecordSecurityLock recordSecurityLock, Map<Serializable, RecordWithErrors> errorRecords, List<Integer> treePosition) throws QException
   {
      if(recordSecurityLock instanceof MultiRecordSecurityLock multiRecordSecurityLock)
      {
         /////////////////////////////////////////////
         // for multi-locks, make recursive descent //
         /////////////////////////////////////////////
         int i = 0;
         for(RecordSecurityLock childLock : CollectionUtils.nonNullList(multiRecordSecurityLock.getLocks()))
         {
            treePosition.add(i);
            evaluateRecordLocks(table, records, action, childLock, errorRecords, treePosition);
            treePosition.remove(treePosition.size() - 1);
            i++;
         }

         return;
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if this lock has an all-access key, and the user has that key, then there can't be any errors here, so return early //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QSecurityKeyType securityKeyType = QContext.getQInstance().getSecurityKeyType(recordSecurityLock.getSecurityKeyType());
      if(StringUtils.hasContent(securityKeyType.getAllAccessKeyName()) && QContext.getQSession().hasSecurityKeyValue(securityKeyType.getAllAccessKeyName(), true, QFieldType.BOOLEAN))
      {
         return;
      }

      ////////////////////////////////
      // proceed w/ non-multi locks //
      ////////////////////////////////
      String primaryKeyField = table.getPrimaryKeyField();
      if(CollectionUtils.nullSafeIsEmpty(recordSecurityLock.getJoinNameChain()))
      {
         //////////////////////////////////////////////////////////////////////////////////
         // handle the value being in the table we're inserting/updating (e.g., no join) //
         //////////////////////////////////////////////////////////////////////////////////
         QFieldMetaData field = table.getField(recordSecurityLock.getFieldName());

         for(QRecord record : records)
         {
            if(action.equals(Action.UPDATE) && !record.getValues().containsKey(field.getName()) && RecordSecurityLock.LockScope.READ_AND_WRITE.equals(recordSecurityLock.getLockScope()))
            {
               /////////////////////////////////////////////////////////////////////////////////////////////////////////
               // if this is a read-write lock, then if we have the record, it means we were able to read the record. //
               // So if we're not updating the security field, then no error can come from it!                        //
               /////////////////////////////////////////////////////////////////////////////////////////////////////////
               continue;
            }

            Serializable        recordSecurityValue = record.getValue(field.getName());
            List<QErrorMessage> recordErrors        = validateRecordSecurityValue(table, recordSecurityLock, recordSecurityValue, field.getType(), action);
            if(CollectionUtils.nullSafeHasContents(recordErrors))
            {
               errorRecords.computeIfAbsent(record.getValue(primaryKeyField), (k) -> new RecordWithErrors(record)).addAll(recordErrors, treePosition);
            }
         }
      }
      else
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // else look for the joined record - if it isn't found, assume a fail - else validate security value if found //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         QJoinMetaData leftMostJoin  = QContext.getQInstance().getJoin(recordSecurityLock.getJoinNameChain().get(0));
         QJoinMetaData rightMostJoin = QContext.getQInstance().getJoin(recordSecurityLock.getJoinNameChain().get(recordSecurityLock.getJoinNameChain().size() - 1));

         ////////////////////////////////
         // todo probably, but more... //
         ////////////////////////////////
         // if(leftMostJoin.getLeftTable().equals(table.getName()))
         // {
         //    leftMostJoin = leftMostJoin.flip();
         // }

         QTableMetaData rightMostJoinTable = QContext.getQInstance().getTable(rightMostJoin.getRightTable());
         QTableMetaData leftMostJoinTable  = QContext.getQInstance().getTable(leftMostJoin.getLeftTable());

         for(List<QRecord> inputRecordPage : CollectionUtils.getPages(records, 500))
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

               boolean updatingAnyLockJoinFields = false;
               for(JoinOn joinOn : rightMostJoin.getJoinOns())
               {
                  QFieldType   type             = rightMostJoinTable.getField(joinOn.getRightField()).getType();
                  Serializable inputRecordValue = ValueUtils.getValueAsFieldType(type, inputRecord.getValue(joinOn.getRightField()));
                  inputRecordJoinValues.add(inputRecordValue);

                  // if we have a value in this field (and it's not the primary key), then it means we're updating part of the lock
                  if(inputRecordValue != null && !joinOn.getRightField().equals(table.getPrimaryKeyField()))
                  {
                     updatingAnyLockJoinFields = true;
                  }

                  subFilter.addCriteria(inputRecordValue == null
                     ? new QFilterCriteria(rightMostJoin.getLeftTable() + "." + joinOn.getLeftField(), QCriteriaOperator.IS_BLANK)
                     : new QFilterCriteria(rightMostJoin.getLeftTable() + "." + joinOn.getLeftField(), QCriteriaOperator.EQUALS, inputRecordValue));
               }

               //////////////////////////////////
               // todo maybe, some version of? //
               //////////////////////////////////
               // if(action.equals(Action.UPDATE) && !updatingAnyLockJoinFields && RecordSecurityLock.LockScope.READ_AND_WRITE.equals(recordSecurityLock.getLockScope()))
               // {
               //    /////////////////////////////////////////////////////////////////////////////////////////////////////////
               //    // if this is a read-write lock, then if we have the record, it means we were able to read the record. //
               //    // So if we're not updating the security field, then no error can come from it!                        //
               //    /////////////////////////////////////////////////////////////////////////////////////////////////////////
               //    continue;
               // }

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
                     List<QErrorMessage> recordErrors = validateRecordSecurityValue(table, recordSecurityLock, recordSecurityValue, field.getType(), action);
                     if(CollectionUtils.nullSafeHasContents(recordErrors))
                     {
                        errorRecords.computeIfAbsent(inputRecord.getValue(primaryKeyField), (k) -> new RecordWithErrors(inputRecord)).addAll(recordErrors, treePosition);
                     }
                  }
               }
               else
               {
                  for(QRecord inputRecord : inputRecords)
                  {
                     if(RecordSecurityLock.NullValueBehavior.DENY.equals(NullValueBehaviorUtil.getEffectiveNullValueBehavior(recordSecurityLock)))
                     {
                        PermissionDeniedMessage error = new PermissionDeniedMessage("You do not have permission to " + action.name().toLowerCase() + " this record - the referenced " + leftMostJoinTable.getLabel() + " was not found.");
                        errorRecords.computeIfAbsent(inputRecord.getValue(primaryKeyField), (k) -> new RecordWithErrors(inputRecord)).add(error, treePosition);
                     }
                  }
               }
            }
         }
      }
   }



   /*******************************************************************************
    ** for tracking errors, we use primary keys.  add "made up" ones to records
    ** if needed (e.g., insert use-case).
    *******************************************************************************/
   private static Map<Serializable, QRecord> makeUpPrimaryKeysIfNeeded(List<QRecord> records, QTableMetaData table)
   {
      String                     primaryKeyField   = table.getPrimaryKeyField();
      Map<Serializable, QRecord> madeUpPrimaryKeys = new HashMap<>();
      Integer                    madeUpPrimaryKey  = -1;
      for(QRecord record : records)
      {
         if(record.getValue(primaryKeyField) == null)
         {
            madeUpPrimaryKeys.put(madeUpPrimaryKey, record);
            record.setValue(primaryKeyField, madeUpPrimaryKey);
            madeUpPrimaryKey--;
         }
      }
      return madeUpPrimaryKeys;
   }



   /*******************************************************************************
    ** For a given table & action type, convert the table's record locks to a
    ** MultiRecordSecurityLock, with only the appropriate lock-scopes being included
    ** (e.g., read-locks for selects, write-locks for insert/update/delete).
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   static MultiRecordSecurityLock getRecordSecurityLocks(QTableMetaData table, Action action)
   {
      List<RecordSecurityLock> allLocksOnTable = CollectionUtils.nonNullList(table.getRecordSecurityLocks());
      MultiRecordSecurityLock locksOfType = switch(action)
      {
         case INSERT, UPDATE, DELETE -> RecordSecurityLockFilters.filterForWriteLockTree(allLocksOnTable);
         case SELECT -> RecordSecurityLockFilters.filterForReadLockTree(allLocksOnTable);
         default -> throw (new IllegalArgumentException("Unsupported action: " + action));
      };

      if(action.equals(Action.UPDATE))
      {
         ////////////////////////////////////////////////////////////////////////////
         // todo at some point this seemed right, but now it doesn't - needs work. //
         ////////////////////////////////////////////////////////////////////////////
         // ////////////////////////////////////////////////////////
         // // when doing an update, convert all OR's to AND's... //
         // ////////////////////////////////////////////////////////
         // updateOperators(locksOfType, MultiRecordSecurityLock.BooleanOperator.AND);
      }

      ////////////////////////////////////////
      // if there are no locks, just return //
      ////////////////////////////////////////
      if(locksOfType == null || CollectionUtils.nullSafeIsEmpty(locksOfType.getLocks()))
      {
         return (null);
      }

      return (locksOfType);
   }



   /*******************************************************************************
    ** for a full multi-lock tree, set all of the boolean operators to the specified one.
    *******************************************************************************/
   private static void updateOperators(MultiRecordSecurityLock multiLock, MultiRecordSecurityLock.BooleanOperator operator)
   {
      multiLock.setOperator(operator);
      for(RecordSecurityLock childLock : multiLock.getLocks())
      {
         if(childLock instanceof MultiRecordSecurityLock childMultiLock)
         {
            updateOperators(childMultiLock, operator);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QErrorMessage> validateRecordSecurityValue(QTableMetaData table, RecordSecurityLock recordSecurityLock, Serializable recordSecurityValue, QFieldType fieldType, Action action)
   {
      if(recordSecurityValue == null)
      {
         /////////////////////////////////////////////////////////////////
         // handle null values - error if the NullValueBehavior is DENY //
         /////////////////////////////////////////////////////////////////
         if(RecordSecurityLock.NullValueBehavior.DENY.equals(NullValueBehaviorUtil.getEffectiveNullValueBehavior(recordSecurityLock)))
         {
            String lockLabel = CollectionUtils.nullSafeHasContents(recordSecurityLock.getJoinNameChain()) ? recordSecurityLock.getSecurityKeyType() : table.getField(recordSecurityLock.getFieldName()).getLabel();
            return (List.of(new PermissionDeniedMessage("You do not have permission to " + action.name().toLowerCase() + " a record without a value in the field: " + lockLabel)));
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
               return (List.of(new PermissionDeniedMessage("You do not have permission to " + action.name().toLowerCase() + " this record.")));
            }
            else
            {
               QFieldMetaData field = table.getField(recordSecurityLock.getFieldName());
               return (List.of(new PermissionDeniedMessage("You do not have permission to " + action.name().toLowerCase() + " a record with a value of " + recordSecurityValue + " in the field: " + field.getLabel())));
            }
         }
      }
      return (Collections.emptyList());
   }



   /*******************************************************************************
    ** Class to track errors that we're associating with a record.
    **
    ** More complex than it first seems to be needed, because as we're evaluating
    ** locks, we might find some, but based on the boolean condition associated with
    ** them, they might not actually be record-level errors.
    **
    ** e.g., two locks with an OR relationship - as long as one passes, the record
    ** should have no errors.  And so-on through the tree of locks/multi-locks.
    **
    ** Stores the errors in a tree of ErrorTreeNode objects.
    **
    ** References into that tree are achieved via a List of Integer called "tree positions"
    ** where each entry in the list denotes the index of the tree node at that level.
    **
    ** e.g., given this tree:
    ** <pre>
    **   A      B
    **  / \    /|\
    ** C   D  E F G
    **     |
    **     H
    ** </pre>
    **
    ** The positions of each node would be:
    ** <pre>
    ** A: [0]
    ** B: [1]
    ** C: [0,0]
    ** D: [0,1]
    ** E: [1,0]
    ** F: [1,1]
    ** G: [1,2]
    ** H: [0,1,0]
    ** </pre>
    *******************************************************************************/
   static class RecordWithErrors
   {
      private QRecord       record;
      private ErrorTreeNode errorTree;



      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public RecordWithErrors(QRecord record)
      {
         this.record = record;
      }



      /*******************************************************************************
       ** add a list of errors, for a given list of tree positions
       *******************************************************************************/
      public void addAll(List<QErrorMessage> recordErrors, List<Integer> treePositions)
      {
         if(errorTree == null)
         {
            errorTree = new ErrorTreeNode();
         }

         ErrorTreeNode node = errorTree;
         for(Integer treePosition : treePositions)
         {
            if(node.children == null)
            {
               node.children = new ArrayList<>(treePosition);
            }

            while(treePosition >= node.children.size())
            {
               node.children.add(null);
            }

            if(node.children.get(treePosition) == null)
            {
               node.children.set(treePosition, new ErrorTreeNode());
            }

            node = node.children.get(treePosition);
         }

         if(node.errors == null)
         {
            node.errors = new ArrayList<>();
         }
         node.errors.addAll(recordErrors);
      }



      /*******************************************************************************
       ** add a single error to a given tree-position
       *******************************************************************************/
      public void add(QErrorMessage error, List<Integer> treePositions)
      {
         addAll(List.of(error), treePositions);
      }



      /*******************************************************************************
       ** after the tree of errors has been built - walk a lock-tree (locksToCheck)
       ** and resolve boolean operations, to get a final list of errors (possibly empty)
       ** to put on the record.
       *******************************************************************************/
      public void propagateErrorsToRecord(MultiRecordSecurityLock locksToCheck)
      {
         List<QErrorMessage> errors = recursivePropagation(locksToCheck, new ArrayList<>());

         if(CollectionUtils.nullSafeHasContents(errors))
         {
            errors.forEach(e -> record.addError(e));
         }
      }



      /*******************************************************************************
       ** recursive implementation of the propagation method - e.g., walk tree applying
       ** boolean logic.
       *******************************************************************************/
      private List<QErrorMessage> recursivePropagation(MultiRecordSecurityLock locksToCheck, List<Integer> treePositions)
      {
         //////////////////////////////////////////////////////////////////
         // build a list of errors at this level (and deeper levels too) //
         //////////////////////////////////////////////////////////////////
         List<QErrorMessage> errorsFromThisLevel = new ArrayList<>();

         int i = 0;
         for(RecordSecurityLock lock : locksToCheck.getLocks())
         {
            List<QErrorMessage> errorsFromThisLock;

            treePositions.add(i);
            if(lock instanceof MultiRecordSecurityLock childMultiLock)
            {
               errorsFromThisLock = recursivePropagation(childMultiLock, treePositions);
            }
            else
            {
               errorsFromThisLock = getErrorsFromTree(treePositions);
            }

            errorsFromThisLevel.addAll(errorsFromThisLock);

            treePositions.remove(treePositions.size() - 1);
            i++;
         }

         if(MultiRecordSecurityLock.BooleanOperator.AND.equals(locksToCheck.getOperator()))
         {
            //////////////////////////////////////////////////////////////
            // for an AND - if there were ANY errors, then return them. //
            //////////////////////////////////////////////////////////////
            if(!errorsFromThisLevel.isEmpty())
            {
               return (errorsFromThisLevel);
            }
         }
         else // OR
         {
            //////////////////////////////////////////////////////////
            // for an OR - only return if ALL conditions had errors //
            //////////////////////////////////////////////////////////
            if(errorsFromThisLevel.size() == locksToCheck.getLocks().size())
            {
               return (errorsFromThisLevel); // todo something smarter?
            }
         }

         ///////////////////////////////////
         // else - no errors - empty list //
         ///////////////////////////////////
         return Collections.emptyList();
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      private List<QErrorMessage> getErrorsFromTree(List<Integer> treePositions)
      {
         ErrorTreeNode node = errorTree;

         for(Integer treePosition : treePositions)
         {
            if(node.children == null)
            {
               return Collections.emptyList();
            }

            if(treePosition >= node.children.size())
            {
               return Collections.emptyList();
            }

            if(node.children.get(treePosition) == null)
            {
               return Collections.emptyList();
            }

            node = node.children.get(treePosition);
         }

         if(node.errors == null)
         {
            return Collections.emptyList();
         }

         return node.errors;
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public String toString()
      {
         try
         {
            return JsonUtils.toPrettyJson(this);
         }
         catch(Exception e)
         {
            return "error in toString";
         }
      }
   }



   /*******************************************************************************
    ** tree node used by RecordWithErrors
    *******************************************************************************/
   static class ErrorTreeNode
   {
      private List<QErrorMessage>      errors;
      private ArrayList<ErrorTreeNode> children;



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public String toString()
      {
         try
         {
            return JsonUtils.toPrettyJson(this);
         }
         catch(Exception e)
         {
            return "error in toString";
         }
      }



      /*******************************************************************************
       ** Getter for errors - only here for Jackson/toString
       **
       *******************************************************************************/
      public List<QErrorMessage> getErrors()
      {
         return errors;
      }



      /*******************************************************************************
       ** Getter for children - only here for Jackson/toString
       **
       *******************************************************************************/
      public ArrayList<ErrorTreeNode> getChildren()
      {
         return children;
      }
   }

}
