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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.QCodeExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.ScriptExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.StoreScriptLogAndScriptLogLineExecutionLogger;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.AdHocScriptCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevision;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunAdHocRecordScriptAction
{
   private static final QLogger LOG = QLogger.getLogger(RunAdHocRecordScriptAction.class);

   private Map<Integer, ScriptRevision> scriptRevisionCacheByScriptRevisionId = new HashMap<>();
   private Map<Integer, ScriptRevision> scriptRevisionCacheByScriptId         = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public void run(RunAdHocRecordScriptInput input, RunAdHocRecordScriptOutput output) throws QException
   {
      ActionHelper.validateSession(input);

      /////////////////////////
      // figure out the code //
      /////////////////////////
      ScriptRevision scriptRevision = getScriptRevision(input);
      if(scriptRevision == null)
      {
         throw (new QException("Script revision was not found."));
      }

      ////////////////////////////
      // figure out the records //
      ////////////////////////////
      QTableMetaData table = QContext.getQInstance().getTable(input.getTableName());
      if(CollectionUtils.nullSafeIsEmpty(input.getRecordList()))
      {
         QueryInput queryInput = new QueryInput();
         queryInput.setTableName(input.getTableName());
         queryInput.setFilter(new QQueryFilter(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, input.getRecordPrimaryKeyList())));
         QueryOutput queryOutput = new QueryAction().execute(queryInput);
         input.setRecordList(queryOutput.getRecords());
      }

      if(CollectionUtils.nullSafeIsEmpty(input.getRecordList()))
      {
         ////////////////////////////////////////
         // just return if nothing found?  idk //
         ////////////////////////////////////////
         LOG.info("No records supplied as input (or found via primary keys); exiting with noop");
         return;
      }

      /////////////
      // run it! //
      /////////////
      ExecuteCodeInput executeCodeInput = new ExecuteCodeInput();
      executeCodeInput.setInput(new HashMap<>(Objects.requireNonNullElseGet(input.getInputValues(), HashMap::new)));
      executeCodeInput.getInput().put("records", new ArrayList<>(input.getRecordList()));
      executeCodeInput.setContext(new HashMap<>());
      if(input.getOutputObject() != null)
      {
         executeCodeInput.getContext().put("output", input.getOutputObject());
      }

      if(input.getScriptUtils() != null)
      {
         executeCodeInput.getContext().put("scriptUtils", input.getScriptUtils());
      }

      executeCodeInput.getContext().put("api", new ScriptApi());

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
      AdHocScriptCodeReference codeReference = input.getCodeReference();
      if(codeReference.getScriptRevisionRecord() != null)
      {
         return (new ScriptRevision(codeReference.getScriptRevisionRecord()));
      }

      if(codeReference.getScriptRevisionId() != null)
      {
         if(!scriptRevisionCacheByScriptRevisionId.containsKey(codeReference.getScriptRevisionId()))
         {
            GetInput getInput = new GetInput();
            getInput.setTableName(ScriptRevision.TABLE_NAME);
            getInput.setPrimaryKey(codeReference.getScriptRevisionId());
            GetOutput getOutput = new GetAction().execute(getInput);
            if(getOutput.getRecord() != null)
            {
               scriptRevisionCacheByScriptRevisionId.put(codeReference.getScriptRevisionId(), new ScriptRevision(getOutput.getRecord()));
            }
            else
            {
               scriptRevisionCacheByScriptRevisionId.put(codeReference.getScriptRevisionId(), null);
            }
         }

         return (scriptRevisionCacheByScriptRevisionId.get(codeReference.getScriptRevisionId()));
      }

      if(codeReference.getScriptId() != null)
      {
         if(!scriptRevisionCacheByScriptId.containsKey(codeReference.getScriptId()))
         {
            QueryInput queryInput = new QueryInput();
            queryInput.setTableName(ScriptRevision.TABLE_NAME);
            queryInput.setFilter(new QQueryFilter(new QFilterCriteria("script.id", QCriteriaOperator.EQUALS, codeReference.getScriptId())));
            queryInput.withQueryJoin(new QueryJoin(Script.TABLE_NAME).withBaseTableOrAlias(ScriptRevision.TABLE_NAME).withJoinMetaData(QContext.getQInstance().getJoin("currentScriptRevision")));
            QueryOutput queryOutput = new QueryAction().execute(queryInput);

            if(CollectionUtils.nullSafeHasContents(queryOutput.getRecords()))
            {
               scriptRevisionCacheByScriptId.put(codeReference.getScriptId(), new ScriptRevision(queryOutput.getRecords().get(0)));
            }
            else
            {
               scriptRevisionCacheByScriptId.put(codeReference.getScriptId(), null);
            }
         }

         return (scriptRevisionCacheByScriptId.get(codeReference.getScriptId()));
      }

      throw (new QException("Code reference did not contain a scriptRevision, scriptRevisionId, or scriptId"));
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
