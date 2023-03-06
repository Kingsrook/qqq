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

package com.kingsrook.qqq.backend.core.actions.automation;


import com.kingsrook.qqq.backend.core.actions.scripts.RunAdHocRecordScriptAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.automation.RecordAutomationInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.AdHocScriptCodeReference;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevision;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptType;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunRecordScriptsAutomationHandler extends RecordAutomationHandler
{
   private static final QLogger LOG = QLogger.getLogger(RunRecordScriptsAutomationHandler.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void execute(RecordAutomationInput recordAutomationInput) throws QException
   {
      String     tableName  = recordAutomationInput.getTableName();
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(ScriptRevision.TABLE_NAME);
      queryInput.setFilter(new QQueryFilter(
         new QFilterCriteria("script.tableName", QCriteriaOperator.EQUALS, tableName),
         new QFilterCriteria("scriptType.name", QCriteriaOperator.EQUALS, "Record Script") // todo... no.  something about post-insert/update?
      ));
      queryInput.withQueryJoin(new QueryJoin(Script.TABLE_NAME).withBaseTableOrAlias(ScriptRevision.TABLE_NAME).withJoinMetaData(QContext.getQInstance().getJoin("currentScriptRevision")));
      queryInput.withQueryJoin(new QueryJoin(ScriptType.TABLE_NAME).withBaseTableOrAlias(Script.TABLE_NAME));

      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      for(QRecord scriptRevision : CollectionUtils.nonNullList(queryOutput.getRecords()))
      {
         // todo - refresh the records if more than 1 script

         LOG.info("Running script against records", logPair("scriptRevisionId", scriptRevision.getValue("id")), logPair("scriptId", scriptRevision.getValue("scriptIdd")));
         RunAdHocRecordScriptInput input = new RunAdHocRecordScriptInput();
         input.setCodeReference(new AdHocScriptCodeReference().withScriptRevisionRecord(scriptRevision));
         input.setTableName(tableName);
         input.setRecordList(recordAutomationInput.getRecordList());
         RunAdHocRecordScriptOutput output = new RunAdHocRecordScriptOutput();
         new RunAdHocRecordScriptAction().run(input, output);
      }

   }

}
