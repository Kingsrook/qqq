/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionCheckResult;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.values.BasicCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** possible-value source provider for the `QQQ Table` PVS - a list of all tables
 ** in an application/qInstance (that you have permission to see)
 *******************************************************************************/
public class QQQTableCustomPossibleValueProvider extends BasicCustomPossibleValueProvider<QRecord, Integer>
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected QPossibleValue<Integer> makePossibleValue(QRecord sourceObject)
   {
      return (new QPossibleValue<>(sourceObject.getValueInteger("id"), sourceObject.getValueString("label")));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected QRecord getSourceObject(Serializable id) throws QException
   {
      QRecord qqqTableRecord = GetAction.execute(QQQTable.TABLE_NAME, id);
      if(qqqTableRecord == null)
      {
         return (null);
      }

      QTableMetaData table = QContext.getQInstance().getTable(qqqTableRecord.getValueString("name"));
      return isTableAllowed(table) ? qqqTableRecord : null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected List<QRecord> getAllSourceObjects() throws QException
   {
      List<QRecord>      records = QueryAction.execute(QQQTable.TABLE_NAME, null);
      ArrayList<QRecord> rs      = new ArrayList<>();
      for(QRecord record : records)
      {
         QTableMetaData table = QContext.getQInstance().getTable(record.getValueString("name"));
         if(isTableAllowed(table))
         {
            rs.add(record);
         }
      }

      return rs;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private boolean isTableAllowed(QTableMetaData table)
   {
      if(table == null)
      {
         return (false);
      }

      if(table.getIsHidden())
      {
         return (false);
      }

      PermissionCheckResult permissionCheckResult = PermissionsHelper.getPermissionCheckResult(new QueryInput(table.getName()), table);
      if(!PermissionCheckResult.ALLOW.equals(permissionCheckResult))
      {
         return (false);
      }

      return (true);
   }

}
