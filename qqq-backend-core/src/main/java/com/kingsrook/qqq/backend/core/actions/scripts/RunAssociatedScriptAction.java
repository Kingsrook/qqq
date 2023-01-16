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
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.StoreScriptLogAndScriptLogLineExecutionLogger;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAssociatedScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAssociatedScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevision;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunAssociatedScriptAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public void run(RunAssociatedScriptInput input, RunAssociatedScriptOutput output) throws QException
   {
      ActionHelper.validateSession(input);

      Serializable scriptId = getScriptId(input);
      if(scriptId == null)
      {
         throw (new QNotFoundException("The input record [" + input.getCodeReference().getRecordTable() + "][" + input.getCodeReference().getRecordPrimaryKey()
            + "] does not have a script specified for [" + input.getCodeReference().getFieldName() + "]"));
      }

      Script script = getScript(input, scriptId);
      if(script.getCurrentScriptRevisionId() == null)
      {
         throw (new QNotFoundException("The script for record [" + input.getCodeReference().getRecordTable() + "][" + input.getCodeReference().getRecordPrimaryKey()
            + "] (scriptId=" + scriptId + ") does not have a current version."));
      }

      ScriptRevision scriptRevision = getCurrentScriptRevision(input, script.getCurrentScriptRevisionId());

      ExecuteCodeInput executeCodeInput = new ExecuteCodeInput();
      executeCodeInput.setInput(new HashMap<>(input.getInputValues()));
      executeCodeInput.setContext(new HashMap<>());
      if(input.getOutputObject() != null)
      {
         executeCodeInput.getContext().put("output", input.getOutputObject());
      }
      executeCodeInput.setCodeReference(new QCodeReference().withInlineCode(scriptRevision.getContents()).withCodeType(QCodeType.JAVA_SCRIPT)); // todo - code type as attribute of script!!
      executeCodeInput.setExecutionLogger(new StoreScriptLogAndScriptLogLineExecutionLogger(scriptRevision.getScriptId(), scriptRevision.getId()));
      ExecuteCodeOutput executeCodeOutput = new ExecuteCodeOutput();
      new ExecuteCodeAction().run(executeCodeInput, executeCodeOutput);

      output.setOutput(executeCodeOutput.getOutput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private ScriptRevision getCurrentScriptRevision(RunAssociatedScriptInput input, Serializable scriptRevisionId) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName("scriptRevision");
      getInput.setPrimaryKey(scriptRevisionId);
      GetOutput getOutput = new GetAction().execute(getInput);
      if(getOutput.getRecord() == null)
      {
         throw (new QNotFoundException("The current revision of the script for record [" + input.getCodeReference().getRecordTable() + "][" + input.getCodeReference().getRecordPrimaryKey() + "]["
            + input.getCodeReference().getFieldName() + "] (scriptRevisionId=" + scriptRevisionId + ") was not found."));
      }

      return (new ScriptRevision(getOutput.getRecord()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Script getScript(RunAssociatedScriptInput input, Serializable scriptId) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName("script");
      getInput.setPrimaryKey(scriptId);
      GetOutput getOutput = new GetAction().execute(getInput);

      if(getOutput.getRecord() == null)
      {
         throw (new QNotFoundException("The script for record [" + input.getCodeReference().getRecordTable() + "][" + input.getCodeReference().getRecordPrimaryKey() + "]["
            + input.getCodeReference().getFieldName() + "] (script id=" + scriptId + ") was not found."));
      }

      return (new Script(getOutput.getRecord()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Serializable getScriptId(RunAssociatedScriptInput input) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName(input.getCodeReference().getRecordTable());
      getInput.setPrimaryKey(input.getCodeReference().getRecordPrimaryKey());
      GetOutput getOutput = new GetAction().execute(getInput);
      if(getOutput.getRecord() == null)
      {
         throw (new QNotFoundException("The requested record [" + input.getCodeReference().getRecordTable() + "][" + input.getCodeReference().getRecordPrimaryKey() + "] was not found."));
      }

      return (getOutput.getRecord().getValue(input.getCodeReference().getFieldName()));
   }

}
