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
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class TablesCustomPossibleValueProvider implements QCustomPossibleValueProvider<String>
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QPossibleValue<String> getPossibleValue(Serializable idValue)
   {
      QTableMetaData table = QContext.getQInstance().getTable(ValueUtils.getValueAsString(idValue));
      if(table != null && !table.getIsHidden())
      {
         PermissionCheckResult permissionCheckResult = PermissionsHelper.getPermissionCheckResult(new QueryInput(table.getName()), table);
         if(PermissionCheckResult.ALLOW.equals(permissionCheckResult))
         {
            return (new QPossibleValue<>(table.getName(), table.getLabel()));
         }
      }

      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QPossibleValue<String>> search(SearchPossibleValueSourceInput input) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////
      // build all of the possible values (note, will be filtered by user's permissions) //
      /////////////////////////////////////////////////////////////////////////////////////
      List<QPossibleValue<String>> allPossibleValues = new ArrayList<>();
      for(QTableMetaData table : QContext.getQInstance().getTables().values())
      {
         QPossibleValue<String> possibleValue = getPossibleValue(table.getName());
         if(possibleValue != null)
         {
            allPossibleValues.add(possibleValue);
         }
      }

      return completeCustomPVSSearch(input, allPossibleValues);
   }

}
