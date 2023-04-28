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
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


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
      SELECT
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void validateSecurityFields(QTableMetaData table, List<QRecord> records, Action action) throws QException
   {
      List<RecordSecurityLock> locksToCheck = getRecordSecurityLocks(table);
      if(CollectionUtils.nullSafeIsEmpty(locksToCheck))
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
            //////////////////////////////////////////////////////////////////////////////////
            // handle the value being in the table we're inserting/updating (e.g., no join) //
            //////////////////////////////////////////////////////////////////////////////////
            QFieldMetaData field = table.getField(recordSecurityLock.getFieldName());

            for(QRecord record : records)
            {
               if(action.equals(Action.UPDATE) && !record.getValues().containsKey(field.getName()))
               {
                  /////////////////////////////////////////////////////////////////////////
                  // if not updating the security field, then no error can come from it! //
                  /////////////////////////////////////////////////////////////////////////
                  continue;
               }

               Serializable recordSecurityValue = record.getValue(field.getName());
               validateRecordSecurityValue(table, record, recordSecurityLock, recordSecurityValue, field.getType(), action);
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
                        validateRecordSecurityValue(table, inputRecord, recordSecurityLock, recordSecurityValue, field.getType(), action);
                     }
                  }
                  else
                  {
                     for(QRecord inputRecord : inputRecords)
                     {
                        if(RecordSecurityLock.NullValueBehavior.DENY.equals(recordSecurityLock.getNullValueBehavior()))
                        {
                           inputRecord.addError("You do not have permission to " + action.name().toLowerCase() + " this record - the referenced " + leftMostJoinTable.getLabel() + " was not found.");
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
   private static List<RecordSecurityLock> getRecordSecurityLocks(QTableMetaData table)
   {
      List<RecordSecurityLock> recordSecurityLocks = table.getRecordSecurityLocks();
      List<RecordSecurityLock> locksToCheck        = new ArrayList<>();

      ////////////////////////////////////////
      // if there are no locks, just return //
      ////////////////////////////////////////
      if(CollectionUtils.nullSafeIsEmpty(recordSecurityLocks))
      {
         return (null);
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // decide if any locks need checked - where one may not need checked if it has an all-access key, and the user has all-access //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(RecordSecurityLock recordSecurityLock : recordSecurityLocks)
      {
         QSecurityKeyType securityKeyType = QContext.getQInstance().getSecurityKeyType(recordSecurityLock.getSecurityKeyType());
         if(StringUtils.hasContent(securityKeyType.getAllAccessKeyName()) && QContext.getQSession().hasSecurityKeyValue(securityKeyType.getAllAccessKeyName(), true, QFieldType.BOOLEAN))
         {
            LOG.trace("Session has " + securityKeyType.getAllAccessKeyName() + " - not checking this lock.");
         }
         else
         {
            locksToCheck.add(recordSecurityLock);
         }
      }

      return (locksToCheck);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void validateRecordSecurityValue(QTableMetaData table, QRecord record, RecordSecurityLock recordSecurityLock, Serializable recordSecurityValue, QFieldType fieldType, Action action)
   {
      if(recordSecurityValue == null)
      {
         /////////////////////////////////////////////////////////////////
         // handle null values - error if the NullValueBehavior is DENY //
         /////////////////////////////////////////////////////////////////
         if(RecordSecurityLock.NullValueBehavior.DENY.equals(recordSecurityLock.getNullValueBehavior()))
         {
            String lockLabel = CollectionUtils.nullSafeHasContents(recordSecurityLock.getJoinNameChain()) ? recordSecurityLock.getSecurityKeyType() : table.getField(recordSecurityLock.getFieldName()).getLabel();
            record.addError("You do not have permission to " + action.name().toLowerCase() + " a record without a value in the field: " + lockLabel);
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
               record.addError("You do not have permission to " + action.name().toLowerCase() + " this record.");
            }
            else
            {
               QFieldMetaData field = table.getField(recordSecurityLock.getFieldName());
               record.addError("You do not have permission to " + action.name().toLowerCase() + " a record with a value of " + recordSecurityValue + " in the field: " + field.getLabel());
            }
         }
      }
   }

}
