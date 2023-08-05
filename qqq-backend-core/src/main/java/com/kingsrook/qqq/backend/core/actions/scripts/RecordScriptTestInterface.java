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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.BuildScriptLogAndScriptLogLineExecutionLogger;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.TestScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.TestScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.AdHocScriptCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.tables.QQQTableAccessor;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class RecordScriptTestInterface implements TestScriptActionInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setupTestScriptInput(TestScriptInput testScriptInput, ExecuteCodeInput executeCodeInput) throws QException
   {

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void execute(TestScriptInput input, TestScriptOutput output) throws QException
   {
      try
      {
         Serializable scriptId = input.getInputValues().get("scriptId");
         QRecord      script   = new GetAction().executeForRecord(new GetInput(Script.TABLE_NAME).withPrimaryKey(scriptId));

         //////////////////////////////////////////////
         // look up the records being tested against //
         //////////////////////////////////////////////
         String         tableName = QQQTableAccessor.getQQQTableName(script.getValueInteger("qqqTableId"));
         QTableMetaData table     = QContext.getQInstance().getTable(tableName);
         if(table == null)
         {
            throw (new QException("Could not find table [" + tableName + "] for script"));
         }

         String recordPrimaryKeyList = ValueUtils.getValueAsString(input.getInputValues().get("recordPrimaryKeyList"));
         if(!StringUtils.hasContent(recordPrimaryKeyList))
         {
            throw (new QException("Record primary key list was not given."));
         }

         QueryOutput queryOutput = new QueryAction().execute(new QueryInput(tableName)
            .withFilter(new QQueryFilter(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.IN, recordPrimaryKeyList.split(","))))
            .withIncludeAssociations(true));
         if(CollectionUtils.nullSafeIsEmpty(queryOutput.getRecords()))
         {
            throw (new QException("No records were found by the given primary keys."));
         }

         /////////////////////////////
         // set up & run the action //
         /////////////////////////////
         RunAdHocRecordScriptInput runAdHocRecordScriptInput = new RunAdHocRecordScriptInput();
         runAdHocRecordScriptInput.setRecordList(queryOutput.getRecords());

         BuildScriptLogAndScriptLogLineExecutionLogger executionLogger = new BuildScriptLogAndScriptLogLineExecutionLogger(null, null);
         runAdHocRecordScriptInput.setLogger(executionLogger);

         runAdHocRecordScriptInput.setTableName(tableName);
         runAdHocRecordScriptInput.setCodeReference((AdHocScriptCodeReference) input.getCodeReference());
         RunAdHocRecordScriptOutput runAdHocRecordScriptOutput = new RunAdHocRecordScriptOutput();
         new RunAdHocRecordScriptAction().run(runAdHocRecordScriptInput, runAdHocRecordScriptOutput);

         /////////////////////////////////
         // send outputs back to caller //
         /////////////////////////////////
         output.setScriptLog(executionLogger.getScriptLog());
         output.setScriptLogLines(executionLogger.getScriptLogLines());
         if(runAdHocRecordScriptOutput.getException().isPresent())
         {
            output.setException(runAdHocRecordScriptOutput.getException().get());
         }
      }
      catch(QException e)
      {
         output.setException(e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QFieldMetaData> getTestInputFields()
   {
      return (List.of(new QFieldMetaData("recordPrimaryKeyList", QFieldType.STRING).withLabel("Record Primary Key List")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QFieldMetaData> getTestOutputFields()
   {
      return (Collections.emptyList());
   }

}
