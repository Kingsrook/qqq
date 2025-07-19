/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadTableStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for BulkInsertPrepareFileUploadStepTest
 *******************************************************************************/
public class BulkInsertPrepareFileUploadStepTest extends BaseTest
{
   private BulkInsertPrepareFileUploadStep step;
   private RunBackendStepInput             input;
   private RunBackendStepOutput            output;



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void setup()
   {
      step = new BulkInsertPrepareFileUploadStep();
      input = new RunBackendStepInput();
      output = new RunBackendStepOutput();

      input.setProcessName("person.bulkInsertWithFile");
      input.addValue("tableName", "person");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRun_bulkInsert_setsExpectedOutput() throws QException
   {
      step.run(input, output);

      assertNotNull(output.getValue("tableStructure"));
      assertTrue(output.getValue("tableStructure") instanceof BulkLoadTableStructure);
      assertEquals(false, output.getValue("isBulkEdit"));
      assertNotNull(output.getValue("upload.html"));
      assertTrue(output.getValue("upload.html").toString().contains("File Upload Instructions"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRun_bulkEdit_setsExpectedOutput() throws QException
   {
      input.setProcessName("person.bulkEditWithFile");

      step.run(input, output);

      assertEquals(true, output.getValue("isBulkEdit"));
      assertNotNull(output.getValue("upload.html"));
      String html = output.getValue("upload.html").toString();
      assertTrue(html.contains("edit in the"));
      assertTrue(html.contains("Person.csv"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRun_stepBackClearsFile() throws QException
   {
      output.getProcessState().setIsStepBack(true);
      output.addValue("theFile", "something");

      step.run(input, output);

      assertNull(output.getValue("theFile"));
   }
}
