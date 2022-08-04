/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed;


import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QKeyBasedFieldMapping;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for BasicETLProcess
 *******************************************************************************/
class StreamedETLProcessTest
{

   /*******************************************************************************
    ** Simplest happy path
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      RunProcessInput request = new RunProcessInput(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setProcessName(StreamedETLProcess.PROCESS_NAME);
      request.addValue(StreamedETLProcess.FIELD_SOURCE_TABLE, TestUtils.defineTablePerson().getName());
      request.addValue(StreamedETLProcess.FIELD_DESTINATION_TABLE, TestUtils.definePersonFileTable().getName());
      request.addValue(StreamedETLProcess.FIELD_MAPPING_JSON, "");

      RunProcessOutput result = new RunProcessAction().execute(request);
      assertNotNull(result);
      assertTrue(result.getRecords().stream().allMatch(r -> r.getValues().containsKey("id")), "records should have an id, set by the process");
      assertTrue(result.getException().isEmpty());
   }



   /*******************************************************************************
    ** Basic example of doing a mapping transformation
    *******************************************************************************/
   @Test
   public void testMappingTransformation() throws QException
   {
      RunProcessInput request = new RunProcessInput(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setProcessName(StreamedETLProcess.PROCESS_NAME);
      request.addValue(StreamedETLProcess.FIELD_SOURCE_TABLE, TestUtils.definePersonFileTable().getName());
      request.addValue(StreamedETLProcess.FIELD_DESTINATION_TABLE, TestUtils.defineTableIdAndNameOnly().getName());

      ///////////////////////////////////////////////////////////////////////////////////////
      // define our mapping from destination-table field names to source-table field names //
      ///////////////////////////////////////////////////////////////////////////////////////
      QKeyBasedFieldMapping mapping = new QKeyBasedFieldMapping().withMapping("name", "firstName");
      // request.addValue(StreamedETLProcess.FIELD_MAPPING_JSON, JsonUtils.toJson(mapping.getMapping()));
      request.addValue(StreamedETLProcess.FIELD_MAPPING_JSON, JsonUtils.toJson(mapping));

      RunProcessOutput result = new RunProcessAction().execute(request);
      assertNotNull(result);
      assertTrue(result.getRecords().stream().allMatch(r -> r.getValues().containsKey("id")), "records should have an id, set by the process");
      assertTrue(result.getException().isEmpty());
   }

}