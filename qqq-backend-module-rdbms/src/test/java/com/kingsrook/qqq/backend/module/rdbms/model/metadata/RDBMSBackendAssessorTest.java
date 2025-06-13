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

package com.kingsrook.qqq.backend.module.rdbms.model.metadata;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.instances.assessment.QInstanceAssessor;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSActionTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/*******************************************************************************
 ** Unit test for RDBMSBackendAssessor 
 *******************************************************************************/
class RDBMSBackendAssessorTest extends RDBMSActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSuccess() throws Exception
   {
      TestUtils.primeTestDatabase("prime-test-database.sql");
      QInstanceAssessor assessor = new QInstanceAssessor(QContext.getQInstance());
      assessor.assess();
      assessor.printSummary();
      assertEquals(0, assessor.getErrors().size());
      assertEquals(0, assessor.getWarnings().size());
      assertEquals(0, assessor.getExitCode());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testIssues() throws Exception
   {
      //////////////////////////////
      // don't prime the database //
      //////////////////////////////
      // TestUtils.primeTestDatabase("prime-test-database.sql");
      QInstanceAssessor assessor = new QInstanceAssessor(QContext.getQInstance());
      assessor.assess();
      assessor.printSummary();
      assertNotEquals(0, assessor.getErrors().size());
      assertNotEquals(0, assessor.getExitCode());
   }

}