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

package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;


/*******************************************************************************
 ** Only the fields which exist in the record's values map will be updated.
 ** Note the difference between a field being in the value map, with a null value,
 ** vs. not being in the map.  If the field (its key) is in the value map, with a
 ** null value, then the field will be updated to NULL.  But if it's not in the
 ** map, then it'll be ignored.  This would be to do a PATCH type operation, vs a
 ** PUT.  See https://rapidapi.com/blog/put-vs-patch/
 *******************************************************************************/
public class RDBMSUpdateAction extends AbstractRDBMSAction implements UpdateInterface
{
   private static final QLogger LOG = QLogger.getLogger(RDBMSUpdateAction.class);

   private int statusCounter = 0;



   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateOutput execute(UpdateInput updateInput) throws QException
   {
      UpdateOutput rs = new UpdateOutput();

      if(CollectionUtils.nullSafeIsEmpty(updateInput.getRecords()))
      {
         LOG.debug("Update request called with 0 records.  Returning with no-op");
         rs.setRecords(new ArrayList<>());
         return (rs);
      }

      QTableMetaData table = updateInput.getTable();
      Instant        now   = Instant.now();

      List<QRecord> outputRecords = new ArrayList<>();
      rs.setRecords(outputRecords);

      /////////////////////////////////////////////////////////////////////////////////////////////
      // we want to do batch updates.  But, since we only update the columns that                //
      // are present in each record, it means we may have different update SQL for each          //
      // record.  So, we will first "hash" up the records by their list of fields being updated. //
      /////////////////////////////////////////////////////////////////////////////////////////////
      ListingHash<List<String>, QRecord> recordsByFieldBeingUpdated = new ListingHash<>();
      for(QRecord record : updateInput.getRecords())
      {
         ////////////////////////////////////////////
         // todo .. better (not a hard-coded name) //
         ////////////////////////////////////////////
         setValueIfTableHasField(record, table, "modifyDate", now);

         List<String> updatableFields = table.getFields().values().stream()
            .map(QFieldMetaData::getName)
            // todo - intent here is to avoid non-updateable fields - but this
            //  should be like based on field.isUpdatable once that attribute exists
            .filter(name -> !name.equals("id"))
            .filter(name -> record.getValues().containsKey(name))
            .toList();
         recordsByFieldBeingUpdated.add(updatableFields, record);

         //////////////////////////////////////////////////////////////////////////////
         // go ahead and put the record into the output list at this point in time,  //
         // so that the output list's order matches the input list order             //
         // note that if we want to capture updated values (like modify dates), then //
         // we may want a map of primary key to output record, for easy updating.    //
         //////////////////////////////////////////////////////////////////////////////
         QRecord outputRecord = new QRecord(record);
         outputRecords.add(outputRecord);
      }

      try
      {
         Connection connection;
         boolean    needToCloseConnection = false;
         if(updateInput.getTransaction() != null && updateInput.getTransaction() instanceof RDBMSTransaction rdbmsTransaction)
         {
            LOG.debug("Using connection from updateInput [" + rdbmsTransaction.getConnection() + "]");
            connection = rdbmsTransaction.getConnection();
         }
         else
         {
            connection = getConnection(updateInput);
            needToCloseConnection = true;
         }

         try
         {
            /////////////////////////////////////////////////////////////////////////////////////////////
            // process each distinct list of fields being updated (e.g., each different SQL statement) //
            /////////////////////////////////////////////////////////////////////////////////////////////
            for(List<String> fieldsBeingUpdated : recordsByFieldBeingUpdated.keySet())
            {
               updateRecordsWithMatchingListOfFields(updateInput, connection, table, recordsByFieldBeingUpdated.get(fieldsBeingUpdated), fieldsBeingUpdated);
            }
         }
         finally
         {
            if(needToCloseConnection)
            {
               connection.close();
            }
         }

         return rs;
      }
      catch(Exception e)
      {
         // todo - how to communicate errors??? outputRecord.setErrors(new ArrayList<>(List.of(e)));
         throw new QException("Error executing update: " + e.getMessage(), e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void updateRecordsWithMatchingListOfFields(UpdateInput updateInput, Connection connection, QTableMetaData table, List<QRecord> recordList, List<String> fieldsBeingUpdated) throws SQLException
   {
      ////////////////////////////////////////////////////////////////////////////////
      // check for an optimization - if all of the records have the same values for //
      // all fields being updated, just do 1 update, with an IN list on the ids.    //
      ////////////////////////////////////////////////////////////////////////////////
      boolean allAreTheSame;
      if(updateInput.getAreAllValuesBeingUpdatedTheSame() != null)
      {
         allAreTheSame = updateInput.getAreAllValuesBeingUpdatedTheSame();
      }
      else
      {
         allAreTheSame = areAllValuesBeingUpdatedTheSame(recordList, fieldsBeingUpdated);
      }

      if(allAreTheSame)
      {
         updateRecordsWithMatchingValuesAndFields(updateInput, connection, table, recordList, fieldsBeingUpdated);
         return;
      }

      String sql = writeUpdateSQLPrefix(table, fieldsBeingUpdated) + " = ?";

      // todo sql customization? - let each table have custom sql and/or param list

      ////////////////////////////////////////////////////////
      // build the list of list of values, from the records //
      ////////////////////////////////////////////////////////
      List<List<Serializable>> values = new ArrayList<>();
      for(QRecord record : recordList)
      {
         List<Serializable> rowValues = new ArrayList<>();
         values.add(rowValues);

         for(String fieldName : fieldsBeingUpdated)
         {
            Serializable value = record.getValue(fieldName);
            value = scrubValue(table.getField(fieldName), value, false);
            rowValues.add(value);
         }
         rowValues.add(record.getValue(table.getPrimaryKeyField()));
      }

      ////////////////////////////////////////////////////////////////////////////////
      // let query manager do the batch updates - note that it will internally page //
      ////////////////////////////////////////////////////////////////////////////////
      QueryManager.executeBatchUpdate(connection, sql, values);
      incrementStatus(updateInput, recordList.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String writeUpdateSQLPrefix(QTableMetaData table, List<String> fieldsBeingUpdated)
   {
      String columns = fieldsBeingUpdated.stream()
         .map(f -> this.getColumnName(table.getField(f)) + " = ?")
         .collect(Collectors.joining(", "));

      String tableName = escapeIdentifier(getTableName(table));
      return ("UPDATE " + tableName
         + " SET " + columns
         + " WHERE " + getColumnName(table.getField(table.getPrimaryKeyField())) + " ");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void updateRecordsWithMatchingValuesAndFields(UpdateInput updateInput, Connection connection, QTableMetaData table, List<QRecord> recordList, List<String> fieldsBeingUpdated) throws SQLException
   {
      for(List<QRecord> page : CollectionUtils.getPages(recordList, QueryManager.PAGE_SIZE))
      {
         String sql = writeUpdateSQLPrefix(table, fieldsBeingUpdated) + " IN (" + StringUtils.join(",", Collections.nCopies(page.size(), "?")) + ")";

         // todo sql customization? - let each table have custom sql and/or param list

         ////////////////////////////////////////////////////////////////
         // values in the update clause can come from the first record //
         ////////////////////////////////////////////////////////////////
         QRecord      record0 = page.get(0);
         List<Object> params  = new ArrayList<>();
         for(String fieldName : fieldsBeingUpdated)
         {
            Serializable value = record0.getValue(fieldName);
            value = scrubValue(table.getField(fieldName), value, false);
            params.add(value);
         }

         //////////////////////////////////////////////////////////////////////
         // values in the where clause (in list) are the id from each record //
         //////////////////////////////////////////////////////////////////////
         for(QRecord record : page)
         {
            params.add(record.getValue(table.getPrimaryKeyField()));
         }

         /////////////////////////////////////
         // let query manager do the update //
         /////////////////////////////////////
         QueryManager.executeUpdate(connection, sql, params);
         incrementStatus(updateInput, page.size());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean areAllValuesBeingUpdatedTheSame(List<QRecord> recordList, List<String> fieldsBeingUpdated)
   {
      if(recordList.size() == 1)
      {
         return (true);
      }

      QRecord record0 = recordList.get(0);
      for(int i = 1; i < recordList.size(); i++)
      {
         QRecord record = recordList.get(i);
         for(String fieldName : fieldsBeingUpdated)
         {
            if(!Objects.equals(record0.getValue(fieldName), record.getValue(fieldName)))
            {
               return (false);
            }
         }
      }

      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void incrementStatus(UpdateInput updateInput, int amount)
   {
      statusCounter += amount;
      updateInput.getAsyncJobCallback().updateStatus(statusCounter, updateInput.getRecords().size());
   }

}
