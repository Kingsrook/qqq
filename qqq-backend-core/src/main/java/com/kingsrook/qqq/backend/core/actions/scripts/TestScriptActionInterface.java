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


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.BuildScriptLogAndScriptLogLineExecutionLogger;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.TestScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.TestScriptOutput;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevision;


/*******************************************************************************
 ** Interface to be implemented by script-running actions, if they want to allow
 ** themselves to be used for user-testing of their script.
 *******************************************************************************/
public interface TestScriptActionInterface
{
   /*******************************************************************************
    ** Called to adapt or translate data from the TestScriptInput (which would just
    ** have a map of name-value pairs) to the actual input object(s) used by the script.
    **
    ** Note - such a method may want or need to put an "output" object into the
    ** executeCodeInput's context map.
    *******************************************************************************/
   void setupTestScriptInput(TestScriptInput testScriptInput, ExecuteCodeInput executeCodeInput) throws QException;


   /*******************************************************************************
    ** Called to adapt or translate the output object of the script execution to
    ** something suitable for returning to the caller.
    **
    ** Default implementation may always be suitable?
    *******************************************************************************/
   default Serializable processTestScriptOutput(ExecuteCodeOutput executeCodeOutput)
   {
      return (executeCodeOutput.getOutput());
   }


   /*******************************************************************************
    ** Define the list of input fields for testing the script.  The names of these
    ** fields will end up as keys in the setupTestScriptInput method's testScriptInput object.
    *******************************************************************************/
   List<QFieldMetaData> getTestInputFields();


   /*******************************************************************************
    ** Define the list of output fields when testing the script.  The output object
    ** returned from processTestScriptOutput should have keys that match these field names.
    *******************************************************************************/
   List<QFieldMetaData> getTestOutputFields();

   /*******************************************************************************
    ** Execute a test script.
    *******************************************************************************/
   default void execute(TestScriptInput input, TestScriptOutput output) throws QException
   {
      ExecuteCodeInput executeCodeInput = new ExecuteCodeInput();
      executeCodeInput.setContext(new HashMap<>());

      executeCodeInput.setCodeReference(input.getCodeReference());
      BuildScriptLogAndScriptLogLineExecutionLogger executionLogger = new BuildScriptLogAndScriptLogLineExecutionLogger(null, null);
      executeCodeInput.setExecutionLogger(executionLogger);

      try
      {
         setupTestScriptInput(input, executeCodeInput);

         ScriptRevision scriptRevision = new ScriptRevision().withApiName(input.getApiName()).withApiVersion(input.getApiVersion());

         if(this instanceof AssociatedScriptContextPrimerInterface associatedScriptContextPrimerInterface)
         {
            associatedScriptContextPrimerInterface.primeContext(executeCodeInput, scriptRevision);
         }

         ExecuteCodeOutput executeCodeOutput = new ExecuteCodeOutput();

         ExecuteCodeAction.addApiUtilityToContext(executeCodeInput.getContext(), scriptRevision);

         new ExecuteCodeAction().run(executeCodeInput, executeCodeOutput);
         output.setOutputObject(processTestScriptOutput(executeCodeOutput));
      }
      catch(Exception e)
      {
         output.setException(e);
      }

      output.setScriptLog(executionLogger.getScriptLog());
      output.setScriptLogLines(executionLogger.getScriptLogLines());
   }

}
