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

package com.kingsrook.qqq.backend.core.model.session;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;


/*******************************************************************************
 ** One-liner we can use to get a QQQUser record, or just its id (which we often want).
 ** Will insert the record if it wasn't already there.
 ** Also uses in-memory cache table, so rather cheap for normal use-case.
 *******************************************************************************/
public class QQQUserAccessor
{
   private static final QLogger LOG = QLogger.getLogger(QQQUserAccessor.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QRecord getQQQUserRecord(QUser qUser) throws QException
   {
      if(qUser == null)
      {
         LOG.info("Null qUser input");
         return (null);
      }

      /////////////////////////////
      // look in the cache table //
      /////////////////////////////
      GetInput getInput = new GetInput();
      getInput.setTableName(QQQUserMetaDataProvider.QQQ_USER_CACHE_TABLE_NAME);
      getInput.setUniqueKey(MapBuilder.of("idReference", qUser.getIdReference()));
      GetOutput getOutput = new GetAction().execute(getInput);

      ////////////////////////
      // upon cache miss... //
      ////////////////////////
      if(getOutput.getRecord() == null)
      {
         ///////////////////////////////////////////////////////
         // insert the record (into the table, not the cache) //
         ///////////////////////////////////////////////////////
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(QQQUser.TABLE_NAME);
         insertInput.setRecords(List.of(new QRecord().withValue("idReference", qUser.getIdReference()).withValue("name", qUser.getFullName())));
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
   public static QRecord getQQQUserRecord(Integer id) throws QException
   {
      /////////////////////////////
      // look in the cache table //
      /////////////////////////////
      GetInput getInput = new GetInput();
      getInput.setTableName(QQQUserMetaDataProvider.QQQ_USER_CACHE_TABLE_NAME);
      getInput.setPrimaryKey(id);
      GetOutput getOutput = new GetAction().execute(getInput);

      ////////////////////////
      // upon cache miss... //
      ////////////////////////
      if(getOutput.getRecord() == null)
      {
         GetInput sourceGetInput = new GetInput();
         sourceGetInput.setTableName(QQQUser.TABLE_NAME);
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
   public static Integer getUserId(QUser user) throws QException
   {
      return (getQQQUserRecord(user).getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getUserName(Integer id) throws QException
   {
      return (getQQQUserRecord(id).getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer getCurrentUserId() throws QException
   {
      QSession qSession = QContext.getQSession();
      if(qSession != null)
      {
         return getUserId(qSession.getUser());
      }

      return null;
   }
}
