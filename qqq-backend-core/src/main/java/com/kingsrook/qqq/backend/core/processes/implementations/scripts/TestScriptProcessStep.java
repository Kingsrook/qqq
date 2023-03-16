/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.scripts;


import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.scripts.RunAdHocRecordScriptAction;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.BuildScriptLogAndScriptLogLineExecutionLogger;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.AdHocScriptCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevision;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptType;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Action to test a script!
 **
 *******************************************************************************/
public class TestScriptProcessStep implements BackendStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      try
      {
         ActionHelper.validateSession(input);

         ////////////////
         // get inputs //
         ////////////////
         Integer scriptId = input.getValueInteger("scriptId");
         String  code     = input.getValueString("code");

         ScriptRevision scriptRevision = new ScriptRevision();
         scriptRevision.setScriptId(scriptId);
         scriptRevision.setContents(code);

         BuildScriptLogAndScriptLogLineExecutionLogger executionLogger = new BuildScriptLogAndScriptLogLineExecutionLogger(null, null);

         /////////////////////////////////////////////////////////////////
         // lookup the script - figure out how to proceed based on type //
         /////////////////////////////////////////////////////////////////
         QRecord script         = getScript(scriptId);
         String  scriptTypeName = getScriptTypeName(script);

         if(ScriptsMetaDataProvider.SCRIPT_TYPE_NAME_RECORD.equals(scriptTypeName))
         {
            String         tableName = script.getValueString("tableName");
            QTableMetaData table     = QContext.getQInstance().getTable(tableName);
            if(table == null)
            {
               throw (new QException("Could not find table [" + tableName + "] for script"));
            }

            String recordPrimaryKeyList = input.getValueString("recordPrimaryKeyList");
            if(!StringUtils.hasContent(recordPrimaryKeyList))
            {
               throw (new QException("Record primary key list was not given."));
            }

            QueryInput queryInput = new QueryInput();
            queryInput.setTableName(tableName);
            queryInput.setFilter(new QQueryFilter(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, recordPrimaryKeyList.split(","))));
            QueryOutput queryOutput = new QueryAction().execute(queryInput);
            if(CollectionUtils.nullSafeIsEmpty(queryOutput.getRecords()))
            {
               throw (new QException("No records were found by the given primary keys."));
            }

            RunAdHocRecordScriptInput runAdHocRecordScriptInput = new RunAdHocRecordScriptInput();
            runAdHocRecordScriptInput.setRecordList(queryOutput.getRecords());
            runAdHocRecordScriptInput.setLogger(executionLogger);
            runAdHocRecordScriptInput.setCodeReference(new AdHocScriptCodeReference().withScriptRevisionRecord(scriptRevision.toQRecord()));
            RunAdHocRecordScriptOutput runAdHocRecordScriptOutput = new RunAdHocRecordScriptOutput();
            new RunAdHocRecordScriptAction().run(runAdHocRecordScriptInput, runAdHocRecordScriptOutput);

            /////////////////////////////////////////////
            // if there was an exception, send it back //
            /////////////////////////////////////////////
            runAdHocRecordScriptOutput.getException().ifPresent(e -> output.addValue("exception", e));
         }
         else
         {
            throw new QException("This process does not know how to test a script of type: " + scriptTypeName);
         }

         output.addValue("scriptLogLines", new ArrayList<>(executionLogger.getScriptLogLines()));
      }
      catch(Exception e)
      {
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // is this the kind of exception meant here?  or is it more for one thrown by the script execution?  or are those the same?? //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         output.addValue("exception", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord getScript(Integer scriptId) throws QException
   {
      GetInput getScriptInput = new GetInput();
      getScriptInput.setTableName(Script.TABLE_NAME);
      getScriptInput.setPrimaryKey(scriptId);
      GetOutput getScriptOutput = new GetAction().execute(getScriptInput);
      if(getScriptOutput.getRecord() == null)
      {
         throw (new QException("Script was not found by id " + scriptId));
      }

      return (getScriptOutput.getRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getScriptTypeName(QRecord script) throws QException
   {
      GetInput getScriptTypeInput = new GetInput();
      getScriptTypeInput.setTableName(ScriptType.TABLE_NAME);
      getScriptTypeInput.setPrimaryKey(script.getValueInteger("scriptTypeId"));
      GetOutput getScriptTypeOutput = new GetAction().execute(getScriptTypeInput);
      if(getScriptTypeOutput.getRecord() == null)
      {
         throw (new QException("Script Type was not found for script " + script.getValue("id")));
      }

      return (getScriptTypeOutput.getRecord().getValueString("name"));
   }

}
