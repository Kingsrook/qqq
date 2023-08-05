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

package com.kingsrook.qqq.backend.core.model.tables;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;


/*******************************************************************************
 ** One-liner we can use to get a QQQTable record, or just its id (which we often want).
 ** Will insert the record if it wasn't already there.
 ** Also uses in-memory cache table, so rather cheap for normal use-case.
 *******************************************************************************/
public class QQQTableAccessor
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static QRecord getQQQTableRecord(String tableName) throws QException
   {
      /////////////////////////////
      // look in the cache table //
      /////////////////////////////
      GetInput getInput = new GetInput();
      getInput.setTableName(QQQTablesMetaDataProvider.QQQ_TABLE_CACHE_TABLE_NAME);
      getInput.setUniqueKey(MapBuilder.of("name", tableName));
      GetOutput getOutput = new GetAction().execute(getInput);

      ////////////////////////
      // upon cache miss... //
      ////////////////////////
      if(getOutput.getRecord() == null)
      {
         ///////////////////////////////////////////////////////
         // insert the record (into the table, not the cache) //
         ///////////////////////////////////////////////////////
         QTableMetaData tableMetaData = QContext.getQInstance().getTable(tableName);
         InsertInput    insertInput   = new InsertInput();
         insertInput.setTableName(QQQTable.TABLE_NAME);
         insertInput.setRecords(List.of(new QRecord().withValue("name", tableName).withValue("label", tableMetaData.getLabel())));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);

         ///////////////////////////////////
         // repeat the get from the cache //
         ///////////////////////////////////
         getOutput = new GetAction().execute(getInput);
      }

      return getOutput.getRecord();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QRecord getQQQTableRecord(Integer id) throws QException
   {
      /////////////////////////////
      // look in the cache table //
      /////////////////////////////
      GetInput getInput = new GetInput();
      getInput.setTableName(QQQTablesMetaDataProvider.QQQ_TABLE_CACHE_TABLE_NAME);
      getInput.setPrimaryKey(id);
      GetOutput getOutput = new GetAction().execute(getInput);

      ////////////////////////
      // upon cache miss... //
      ////////////////////////
      if(getOutput.getRecord() == null)
      {
         GetInput sourceGetInput = new GetInput();
         sourceGetInput.setTableName(QQQTable.TABLE_NAME);
         sourceGetInput.setPrimaryKey(id);
         GetOutput sourceGetOutput = new GetAction().execute(sourceGetInput);

         ///////////////////////////////////
         // repeat the get from the cache //
         ///////////////////////////////////
         getOutput = new GetAction().execute(sourceGetInput);
      }

      return getOutput.getRecord();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer getQQQTableId(String tableName) throws QException
   {
      return (getQQQTableRecord(tableName).getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getQQQTableName(Integer id) throws QException
   {
      return (getQQQTableRecord(id).getValueString("name"));
   }

}
