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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.util.HashMap;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.BuildScriptLogAndScriptLogLineExecutionLogger;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.TestScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.TestScriptOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Class for running a test of a script - e.g., maybe before it is saved.
 *******************************************************************************/
public class TestScriptAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public void run(TestScriptInput input, TestScriptOutput output) throws QException
   {
      QTableMetaData table = input.getTable();

      ExecuteCodeInput executeCodeInput = new ExecuteCodeInput(input.getInstance());
      executeCodeInput.setSession(input.getSession());
      executeCodeInput.setInput(new HashMap<>(input.getInputValues()));
      executeCodeInput.setContext(new HashMap<>());
      // todo! if(input.getOutputObject() != null)
      // todo! {
      // todo!    executeCodeInput.getContext().put("output", input.getOutputObject());
      // todo! }
      executeCodeInput.setCodeReference(new QCodeReference().withInlineCode(input.getCode()).withCodeType(QCodeType.JAVA_SCRIPT)); // todo - code type as attribute of script!!
      executeCodeInput.setExecutionLogger(new BuildScriptLogAndScriptLogLineExecutionLogger());
      ExecuteCodeOutput executeCodeOutput = new ExecuteCodeOutput();
      new ExecuteCodeAction().run(executeCodeInput, executeCodeOutput);

      // todo! output.setOutput(executeCodeOutput.getOutput());
   }
}
