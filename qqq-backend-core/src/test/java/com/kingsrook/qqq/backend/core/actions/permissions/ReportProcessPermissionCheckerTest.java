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
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit test for ReportProcessPermissionChecker
 *******************************************************************************/
class ReportProcessPermissionCheckerTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws Exception
   {
      QInstance       qInstance       = TestUtils.defineInstance();
      RunProcessInput runProcessInput = new RunProcessInput(qInstance);
      runProcessInput.addValue("reportName", TestUtils.REPORT_NAME_SHAPES_PERSON);

      qInstance.getReport(TestUtils.REPORT_NAME_SHAPES_PERSON)
         .withPermissionRules(new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION));

      QProcessMetaData process = new QProcessMetaData()
         .withName("testProcess");
      qInstance.addProcess(process);

      new QInstanceValidator().validate(qInstance);

      ///////////////////////////////////////////////////////
      // without permission in our session, we should fail //
      ///////////////////////////////////////////////////////
      runProcessInput.setSession(new QSession());
      assertThrows(QPermissionDeniedException.class, () -> new ReportProcessPermissionChecker().checkPermissionsThrowing(runProcessInput, process));

      /////////////////////////////////////////////////////////////////////////
      // add the permission - assert that we have access (e.g., don't throw) //
      /////////////////////////////////////////////////////////////////////////
      runProcessInput.setSession(new QSession().withPermission(TestUtils.REPORT_NAME_SHAPES_PERSON + ".hasAccess"));
      new ReportProcessPermissionChecker().checkPermissionsThrowing(runProcessInput, process);
   }

}