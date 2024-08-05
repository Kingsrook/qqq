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

package com.kingsrook.qqq.backend.module.api.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.module.api.BaseTest;
import com.kingsrook.qqq.backend.module.api.TestUtils;
import com.kingsrook.qqq.backend.module.api.model.OutboundAPILog;
import com.kingsrook.qqq.backend.module.api.model.OutboundAPILogHeader;
import com.kingsrook.qqq.backend.module.api.model.OutboundAPILogMetaDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for MigrateOutboundAPILog process
 *******************************************************************************/
class MigrateOutboundAPILogProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws QException
   {
      MemoryRecordStore.getInstance().reset();
      OutboundAPILogMetaDataProvider.defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, null);
      OutboundAPILogMetaDataProvider.defineNewVersion(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, null);
      OutboundAPILogMetaDataProvider.defineMigrationProcesses(QContext.getQInstance(), OutboundAPILog.TABLE_NAME);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      new InsertAction().execute(new InsertInput(OutboundAPILog.TABLE_NAME).withRecordEntity(new OutboundAPILog()
         .withMethod("POST")
         .withUrl("www.google.com")
         .withRequestBody("please")
         .withResponseBody("you're welcome")
         .withStatusCode(201)
      ));

      RunProcessInput input = new RunProcessInput();
      input.setProcessName("migrateOutboundApiLogToHeaderChildProcess");
      input.setCallback(QProcessCallbackFactory.forFilter(new QQueryFilter()));
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);

      List<OutboundAPILogHeader> outboundApiLogHeaderList = new QueryAction().execute(new QueryInput(OutboundAPILogHeader.TABLE_NAME).withIncludeAssociations(true)).getRecordEntities(OutboundAPILogHeader.class);
      assertEquals(1, outboundApiLogHeaderList.size());
      assertEquals("POST", outboundApiLogHeaderList.get(0).getMethod());
      assertEquals(201, outboundApiLogHeaderList.get(0).getStatusCode());
      assertEquals(1, outboundApiLogHeaderList.get(0).getOutboundAPILogRequestList().size());
      assertEquals("please", outboundApiLogHeaderList.get(0).getOutboundAPILogRequestList().get(0).getRequestBody());
      assertEquals(1, outboundApiLogHeaderList.get(0).getOutboundAPILogResponseList().size());
      assertEquals("you're welcome", outboundApiLogHeaderList.get(0).getOutboundAPILogResponseList().get(0).getResponseBody());
   }

}