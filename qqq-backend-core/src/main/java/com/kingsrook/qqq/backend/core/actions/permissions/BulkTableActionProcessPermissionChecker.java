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

package com.kingsrook.qqq.backend.core.actions.permissions;


import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithPermissionRules;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkTableActionProcessPermissionChecker implements CustomPermissionChecker
{
   private static final QLogger LOG = QLogger.getLogger(BulkTableActionProcessPermissionChecker.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void checkPermissionsThrowing(AbstractActionInput actionInput, MetaDataWithPermissionRules metaDataWithPermissionRules) throws QPermissionDeniedException
   {
      String processName = metaDataWithPermissionRules.getName();
      if(processName != null && processName.indexOf('.') > -1)
      {
         String[] parts          = processName.split("\\.", 2);
         String   tableName      = parts[0];
         String   bulkActionName = parts[1];

         AbstractTableActionInput tableActionInput = new AbstractTableActionInput();
         tableActionInput.setTableName(tableName);

         switch(bulkActionName)
         {
            case "bulkInsert" -> PermissionsHelper.checkTablePermissionThrowing(tableActionInput, TablePermissionSubType.INSERT);
            case "bulkEdit" -> PermissionsHelper.checkTablePermissionThrowing(tableActionInput, TablePermissionSubType.EDIT);
            case "bulkDelete" -> PermissionsHelper.checkTablePermissionThrowing(tableActionInput, TablePermissionSubType.DELETE);
            default -> LOG.warn("Unexpected bulk action name when checking permissions for process: " + processName);
         }
      }
   }

}
