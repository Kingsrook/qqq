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
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithName;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;


/*******************************************************************************
 * For special cases of applying permissions, an application can implement a
 * custom permission checker via this interface - and place it on a table, process,
 * report, app, or widget via
 * {@link QPermissionRules#withCustomPermissionChecker(QCodeReference)}.
 *******************************************************************************/
public interface CustomPermissionChecker
{
   QLogger LOG = QLogger.getLogger(CustomPermissionChecker.class);

   /*******************************************************************************
    * This is the primary method of the interface, which is called when it's time
    * to check if a user (current session) has permission.
    *
    * @param actionInput generally the input data container for the action being
    *                    performed - e.g., an InsertInput or RunProcessInput
    * @param metaDataWithPermissionRules the table/process/etc that is being requested
    *                                    by a user.
    * @throws QPermissionDeniedException to indicate the user does not have permission.
    * The method should return without exception to indicate permission granted,
    *******************************************************************************/
   void checkPermissionsThrowing(AbstractActionInput actionInput, MetaDataWithPermissionRules metaDataWithPermissionRules) throws QPermissionDeniedException;


   /***************************************************************************
    * To assist with {@link PermissionsHelper#getAllAvailablePermissions(QInstance)}
    * this method indicates that this implementation of this interface has implemented
    * {@link #buildAvailablePermission(QPermissionRules, PermissionSubType, String, MetaDataWithName, String)}
    *
    * @return in the default implementation, false is returned.
    ***************************************************************************/
   default boolean handlesBuildAvailablePermission()
   {
      return (false);
   }


   /***************************************************************************
    * To assist with {@link PermissionsHelper#getAllAvailablePermissions(QInstance)}
    * this method lets the application return a specialized {@link AvailablePermission}
    * object, e.g., with the name that the permission name that the custom checker
    * will use.
    *
    * @return an {@link AvailablePermission} object to add to the set of all
    * available permissions.  If null, then no permission is added to that result.
    * @see #buildBaseAvailablePermission(QPermissionRules, PermissionSubType, MetaDataWithName, String)
    ***************************************************************************/
   default AvailablePermission buildAvailablePermission(QPermissionRules rules, PermissionSubType permissionSubType, String baseName, MetaDataWithName metaDataWithName, String objectType)
   {
      if(handlesBuildAvailablePermission())
      {
         LOG.warn("An instance of CustomPermissionChecker stated that it handlesBuildAddEffectiveAvailablePermission, but did not implement buildAvailablePermission (or else it called super)");
      }

      return (null);
   }


   /***************************************************************************
    * Helper method for implementors of buildAvailablePermission - that creates
    * an {@link AvailablePermission} with all fields set *other* than `name`
    * (e.g., the name of the permission).
    ***************************************************************************/
   default AvailablePermission buildBaseAvailablePermission(QPermissionRules rules, PermissionSubType permissionSubType, MetaDataWithName metaDataWithName, String objectType)
   {
      PermissionSubType effectivePermissionSubType = PermissionsHelper.getEffectivePermissionSubType(rules, permissionSubType);
      if(effectivePermissionSubType == null)
      {
         return (null);
      }

      return (new AvailablePermission()
         .withObjectName(metaDataWithName.getLabel())
         .withObjectType(effectivePermissionSubType.toString())
         .withPermissionType(effectivePermissionSubType.toString())
         .withObjectType(objectType));
   }

}
