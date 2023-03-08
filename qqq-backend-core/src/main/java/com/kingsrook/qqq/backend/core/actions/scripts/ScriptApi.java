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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Object made available to scripts for access to qqq api (e.g., query, insert,
 ** etc, plus object constructors).
 *******************************************************************************/
public class ScriptApi implements Serializable
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryInput newQueryInput()
   {
      return (new QueryInput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QQueryFilter newQueryFilter()
   {
      return (new QQueryFilter());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterCriteria newFilterCriteria()
   {
      return (new QFilterCriteria());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFilterOrderBy newFilterOrderBy()
   {
      return (new QFilterOrderBy());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord newRecord()
   {
      return (new QRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> query(String tableName, QQueryFilter filter) throws QException
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(tableName);
      queryInput.setFilter(filter);
      PermissionsHelper.checkTablePermissionThrowing(queryInput, TablePermissionSubType.READ);
      return (new QueryAction().execute(queryInput).getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> query(QueryInput queryInput) throws QException
   {
      PermissionsHelper.checkTablePermissionThrowing(queryInput, TablePermissionSubType.READ);
      return (new QueryAction().execute(queryInput).getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void insert(String tableName, QRecord record) throws QException
   {
      insert(tableName, List.of(record));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void insert(String tableName, List<QRecord> recordList) throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(tableName);
      insertInput.setRecords(recordList);
      PermissionsHelper.checkTablePermissionThrowing(insertInput, TablePermissionSubType.INSERT);
      new InsertAction().execute(insertInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void update(String tableName, QRecord record) throws QException
   {
      update(tableName, List.of(record));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void update(String tableName, List<QRecord> recordList) throws QException
   {
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(tableName);
      updateInput.setRecords(recordList);
      PermissionsHelper.checkTablePermissionThrowing(updateInput, TablePermissionSubType.EDIT);
      new UpdateAction().execute(updateInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void delete(String tableName, Serializable primaryKey) throws QException
   {
      delete(tableName, List.of(primaryKey));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void delete(String tableName, QRecord record) throws QException
   {
      delete(tableName, List.of(record));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void delete(String tableName, List<?> recordOrPrimaryKeyList) throws QException
   {
      QTableMetaData table       = QContext.getQInstance().getTable(tableName);
      DeleteInput    deleteInput = new DeleteInput();
      deleteInput.setTableName(tableName);
      List<Serializable> primaryKeyList = new ArrayList<>();
      for(Object o : recordOrPrimaryKeyList)
      {
         if(o instanceof QRecord qRecord)
         {
            primaryKeyList.add(qRecord.getValue(table.getPrimaryKeyField()));
         }
         else
         {
            primaryKeyList.add((Serializable) o);
         }
      }
      deleteInput.setPrimaryKeys(primaryKeyList);
      PermissionsHelper.checkTablePermissionThrowing(deleteInput, TablePermissionSubType.DELETE);
      new DeleteAction().execute(deleteInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void delete(String tableName, QQueryFilter filter) throws QException
   {
      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setTableName(tableName);
      deleteInput.setQueryFilter(filter);
      PermissionsHelper.checkTablePermissionThrowing(deleteInput, TablePermissionSubType.DELETE);
      new DeleteAction().execute(deleteInput);
   }

}
