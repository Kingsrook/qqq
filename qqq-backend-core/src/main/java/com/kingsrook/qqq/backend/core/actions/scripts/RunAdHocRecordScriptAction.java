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
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.QCodeExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.ScriptExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.StoreScriptLogAndScriptLogLineExecutionLogger;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevision;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunAdHocRecordScriptAction
{
   // todo!  private Map<AssociatedScriptCodeReference, ScriptRevision> scriptRevisionCache = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public void run(RunAdHocRecordScriptInput input, RunAdHocRecordScriptOutput output) throws QException
   {
      ActionHelper.validateSession(input);

      ScriptRevision scriptRevision = getScriptRevision(input);

      GetInput getInput = new GetInput();
      getInput.setTableName(input.getTableName());
      getInput.setPrimaryKey(input.getRecordPrimaryKey());
      GetOutput getOutput = new GetAction().execute(getInput);
      QRecord   record    = getOutput.getRecord();
      // todo err if not found

      ExecuteCodeInput executeCodeInput = new ExecuteCodeInput();
      executeCodeInput.setInput(new HashMap<>(Objects.requireNonNullElseGet(input.getInputValues(), HashMap::new)));
      executeCodeInput.getInput().put("record", record);
      executeCodeInput.setContext(new HashMap<>());
      if(input.getOutputObject() != null)
      {
         executeCodeInput.getContext().put("output", input.getOutputObject());
      }

      if(input.getScriptUtils() != null)
      {
         executeCodeInput.getContext().put("scriptUtils", input.getScriptUtils());
      }
      else
      {
         executeCodeInput.getContext().put("scriptUtils", new ScriptApiUtils());
      }

      executeCodeInput.setCodeReference(new QCodeReference().withInlineCode(scriptRevision.getContents()).withCodeType(QCodeType.JAVA_SCRIPT)); // todo - code type as attribute of script!!

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // let caller supply a logger, or by default use StoreScriptLogAndScriptLogLineExecutionLogger //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      QCodeExecutionLoggerInterface executionLogger = Objects.requireNonNullElseGet(input.getLogger(), () -> new StoreScriptLogAndScriptLogLineExecutionLogger(scriptRevision.getScriptId(), scriptRevision.getId()));
      executeCodeInput.setExecutionLogger(executionLogger);
      if(executionLogger instanceof ScriptExecutionLoggerInterface scriptExecutionLoggerInterface)
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////
         // if logger is aware of scripts (as opposed to a generic CodeExecution logger), give it the ids. //
         ////////////////////////////////////////////////////////////////////////////////////////////////////
         scriptExecutionLoggerInterface.setScriptId(scriptRevision.getScriptId());
         scriptExecutionLoggerInterface.setScriptRevisionId(scriptRevision.getId());
      }

      ExecuteCodeOutput executeCodeOutput = new ExecuteCodeOutput();
      new ExecuteCodeAction().run(executeCodeInput, executeCodeOutput);

      output.setOutput(executeCodeOutput.getOutput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private ScriptRevision getScriptRevision(RunAdHocRecordScriptInput input) throws QException
   {
      // todo if(!scriptRevisionCache.containsKey(input.getCodeReference()))
      {
         Serializable scriptId = input.getCodeReference().getScriptId();
         /*
         if(scriptId == null)
         {
            throw (new QNotFoundException("The input record [" + input.getCodeReference().getScriptId() + "][" + input.getCodeReference().getRecordPrimaryKey()
               + "] does not have a script specified for [" + input.getCodeReference().getFieldName() + "]"));
         }

          */

         Script script = getScript(input, scriptId);
         /* todo
         if(script.getCurrentScriptRevisionId() == null)
         {
            throw (new QNotFoundException("The script for record [" + input.getCodeReference().getRecordTable() + "][" + input.getCodeReference().getRecordPrimaryKey()
               + "] (scriptId=" + scriptId + ") does not have a current version."));
         }

          */

         ScriptRevision scriptRevision = getCurrentScriptRevision(input, script.getCurrentScriptRevisionId());
         // scriptRevisionCache.put(input.getCodeReference(), scriptRevision);
         return scriptRevision;
      }

      // return scriptRevisionCache.get(input.getCodeReference());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private ScriptRevision getCurrentScriptRevision(RunAdHocRecordScriptInput input, Serializable scriptRevisionId) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName("scriptRevision");
      getInput.setPrimaryKey(scriptRevisionId);
      GetOutput getOutput = new GetAction().execute(getInput);
      if(getOutput.getRecord() == null)
      {
         /* todo
         throw (new QNotFoundException("The current revision of the script for record [" + input.getCodeReference().getRecordTable() + "][" + input.getCodeReference().getRecordPrimaryKey() + "]["
            + input.getCodeReference().getFieldName() + "] (scriptRevisionId=" + scriptRevisionId + ") was not found."));

          */
         throw (new IllegalStateException("todo"));
      }

      return (new ScriptRevision(getOutput.getRecord()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Script getScript(RunAdHocRecordScriptInput input, Serializable scriptId) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName("script");
      getInput.setPrimaryKey(scriptId);
      GetOutput getOutput = new GetAction().execute(getInput);

      if(getOutput.getRecord() == null)
      {
         /*
         throw (new QNotFoundException("The script for record [" + input.getCodeReference().getRecordTable() + "][" + input.getCodeReference().getRecordPrimaryKey() + "]["
            + input.getCodeReference().getFieldName() + "] (script id=" + scriptId + ") was not found."));

          */
         throw (new IllegalStateException("todo"));
      }

      return (new Script(getOutput.getRecord()));
   }

}
