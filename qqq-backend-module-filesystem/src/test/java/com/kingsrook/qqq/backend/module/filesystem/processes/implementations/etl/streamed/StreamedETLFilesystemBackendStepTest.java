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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.etl.streamed;


import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemActionTest;
import com.kingsrook.qqq.backend.module.filesystem.processes.implementations.etl.basic.BasicETLCollectSourceFileNamesStep;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for StreamedETLFilesystemBackendStep
 *******************************************************************************/
class StreamedETLFilesystemBackendStepTest extends FilesystemActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void runFullProcess() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();

      RunProcessInput runProcessInput = new RunProcessInput(qInstance);
      runProcessInput.setSession(TestUtils.getMockSession());
      runProcessInput.setProcessName(TestUtils.PROCESS_NAME_STREAMED_ETL);

      RunProcessOutput output = new RunProcessAction().execute(runProcessInput);
      String sourceFilePaths = ValueUtils.getValueAsString(output.getValues().get(BasicETLCollectSourceFileNamesStep.FIELD_SOURCE_FILE_PATHS));
      assertThat(sourceFilePaths)
         .contains("FILE-1.csv")
         .contains("FILE-2.csv");
   }

}