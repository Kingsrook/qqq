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

package com.kingsrook.qqq.backend.core.actions.permissions;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for UseTablePermissionCustomPermissionChecker 
 *******************************************************************************/
class UseTablePermissionCustomPermissionCheckerTest extends BaseTest
{
   String tableName   = PermissionsHelperTest.TABLE_NAME;
   String processName = PermissionsHelperTest.PROCESS_NAME;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableHasAccessLevel()
   {
      RunProcessInput actionInput = new RunProcessInput().withProcessName(processName);

      ////////////////////////////////////////////////////////////////////////////////////////////////
      // give the table hasAccess level permission, and a process based on read access to the table //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      QInstance instance = PermissionsHelperTest.newQInstance();
      instance.getTable(tableName).setPermissionRules(new QPermissionRules()
         .withLevel(PermissionLevel.HAS_ACCESS_PERMISSION));
      instance.getProcess(processName).setPermissionRules(new QPermissionRules()
         .withCustomPermissionChecker(UseTablePermissionCustomPermissionChecker.build(tableName, TablePermissionSubType.READ)));
      PermissionsHelperTest.enrich(instance);

      QContext.setQSession(new QSession());
      assertFalse(PermissionsHelper.hasProcessPermission(actionInput, processName));

      QContext.setQSession(new QSession().withPermissions(tableName + ".hasAccess"));
      assertTrue(PermissionsHelper.hasProcessPermission(actionInput, processName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableNotProtected()
   {
      RunProcessInput actionInput = new RunProcessInput().withProcessName(processName);

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // give the table notProtected level permission, and a process based on read access to the table //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      QInstance instance = PermissionsHelperTest.newQInstance();
      instance.getTable(tableName).setPermissionRules(new QPermissionRules()
         .withLevel(PermissionLevel.NOT_PROTECTED));
      instance.getProcess(processName).setPermissionRules(new QPermissionRules()
         .withCustomPermissionChecker(UseTablePermissionCustomPermissionChecker.build(tableName, TablePermissionSubType.READ)));
      PermissionsHelperTest.enrich(instance);

      QContext.setQSession(new QSession());
      assertTrue(PermissionsHelper.hasProcessPermission(actionInput, processName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableReadWriteLevel()
   {
      RunProcessInput actionInput = new RunProcessInput().withProcessName(processName);

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // give the table read/write level permission, and a process based on insert access to the table //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      QInstance instance = PermissionsHelperTest.newQInstance();
      instance.getTable(tableName).setPermissionRules(new QPermissionRules()
         .withLevel(PermissionLevel.READ_WRITE_PERMISSIONS));
      instance.getProcess(processName).setPermissionRules(new QPermissionRules()
         .withCustomPermissionChecker(UseTablePermissionCustomPermissionChecker.build(tableName, TablePermissionSubType.INSERT)));
      PermissionsHelperTest.enrich(instance);

      QContext.setQSession(new QSession());
      assertFalse(PermissionsHelper.hasProcessPermission(actionInput, processName));

      QContext.setQSession(new QSession().withPermissions(tableName + ".read"));
      assertFalse(PermissionsHelper.hasProcessPermission(actionInput, processName));

      QContext.setQSession(new QSession().withPermissions(tableName + ".write"));
      assertTrue(PermissionsHelper.hasProcessPermission(actionInput, processName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableReadInsertEditDeleteLevel()
   {
      RunProcessInput actionInput = new RunProcessInput().withProcessName(processName);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // give the table read/insert/edit/delete level permission, and a process based on delete access to the table //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QInstance instance = PermissionsHelperTest.newQInstance();
      instance.getTable(tableName).setPermissionRules(new QPermissionRules()
         .withLevel(PermissionLevel.READ_INSERT_EDIT_DELETE_PERMISSIONS));
      instance.getProcess(processName).setPermissionRules(new QPermissionRules()
         .withCustomPermissionChecker(UseTablePermissionCustomPermissionChecker.build(tableName, TablePermissionSubType.DELETE)));
      PermissionsHelperTest.enrich(instance);

      QContext.setQSession(new QSession());
      assertFalse(PermissionsHelper.hasProcessPermission(actionInput, processName));

      QContext.setQSession(new QSession().withPermissions(tableName + ".read"));
      assertFalse(PermissionsHelper.hasProcessPermission(actionInput, processName));

      QContext.setQSession(new QSession().withPermissions(tableName + ".insert"));
      assertFalse(PermissionsHelper.hasProcessPermission(actionInput, processName));

      QContext.setQSession(new QSession().withPermissions(tableName + ".delete"));
      assertTrue(PermissionsHelper.hasProcessPermission(actionInput, processName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableWithCustomPermissionToo()
   {
      RunProcessInput actionInput = new RunProcessInput().withProcessName(processName);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // give the table read/insert/edit/delete level permission, and a process based on delete access to the table //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QInstance instance = PermissionsHelperTest.newQInstance();
      instance.getTable(tableName).setPermissionRules(new QPermissionRules()
         .withLevel(PermissionLevel.HAS_ACCESS_PERMISSION)
         .withCustomPermissionChecker(UseOtherPermissionNameCustomPermissionChecker.build("somePermission")));
      instance.getProcess(processName).setPermissionRules(new QPermissionRules()
         .withCustomPermissionChecker(UseTablePermissionCustomPermissionChecker.build(tableName, TablePermissionSubType.EDIT)));
      PermissionsHelperTest.enrich(instance);

      QContext.setQSession(new QSession());
      assertFalse(PermissionsHelper.hasProcessPermission(actionInput, processName));

      QContext.setQSession(new QSession().withPermissions("somePermission"));
      assertTrue(PermissionsHelper.hasProcessPermission(actionInput, processName));
   }

}