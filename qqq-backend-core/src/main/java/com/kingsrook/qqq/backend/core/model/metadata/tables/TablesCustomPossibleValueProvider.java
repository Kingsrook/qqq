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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionCheckResult;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.values.BasicCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** possible-value source provider for the `Tables` PVS - a list of all tables
 ** in an application/qInstance.
 *******************************************************************************/
public class TablesCustomPossibleValueProvider extends BasicCustomPossibleValueProvider<QTableMetaData, String>
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected QPossibleValue<String> makePossibleValue(QTableMetaData sourceObject)
   {
      return (new QPossibleValue<>(sourceObject.getName(), sourceObject.getLabel()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected QTableMetaData getSourceObject(Serializable id)
   {
      QTableMetaData table = QContext.getQInstance().getTable(ValueUtils.getValueAsString(id));
      return isTableAllowed(table) ? table : null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected List<QTableMetaData> getAllSourceObjects()
   {
      ArrayList<QTableMetaData> rs = new ArrayList<>();
      for(QTableMetaData table : QContext.getQInstance().getTables().values())
      {
         if(isTableAllowed(table))
         {
            rs.add(table);
         }
      }
      return rs;
   }


   /***************************************************************************
    **
    ***************************************************************************/
   protected boolean isTableAllowed(QTableMetaData table)
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
