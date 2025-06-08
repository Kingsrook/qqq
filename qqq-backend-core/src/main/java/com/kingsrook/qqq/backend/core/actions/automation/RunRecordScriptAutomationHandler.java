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


import java.io.Serializable;
import java.util.Map;
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
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunRecordScriptAutomationHandler implements RecordAutomationHandlerInterface
{
   private static final QLogger LOG = QLogger.getLogger(RunRecordScriptAutomationHandler.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void execute(RecordAutomationInput recordAutomationInput) throws QException
   {
      String                    tableName = recordAutomationInput.getTableName();
      Map<String, Serializable> values    = recordAutomationInput.getAction().getValues();
      Integer                   scriptId  = ValueUtils.getValueAsInteger(values.get("scriptId"));

      if(scriptId == null)
      {
         throw (new QException("ScriptId was not provided in values map for record automations on table: " + tableName));
      }

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(ScriptRevision.TABLE_NAME);
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria("scriptId", QCriteriaOperator.EQUALS, scriptId)));
      queryInput.withQueryJoin(new QueryJoin(Script.TABLE_NAME).withBaseTableOrAlias(ScriptRevision.TABLE_NAME).withJoinMetaData(QContext.getQInstance().getJoin(ScriptsMetaDataProvider.CURRENT_SCRIPT_REVISION_JOIN_NAME)));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      if(CollectionUtils.nullSafeIsEmpty(queryOutput.getRecords()))
      {
         throw (new QException("Could not find current revision for scriptId: " + scriptId + " on table " + tableName));
      }

      QRecord scriptRevision = queryOutput.getRecords().get(0);
      LOG.debug("Running script against records", logPair("scriptRevisionId", scriptRevision.getValue("id")), logPair("scriptId", scriptRevision.getValue("scriptIdd")));

      RunAdHocRecordScriptInput input = new RunAdHocRecordScriptInput();
      input.setCodeReference(new AdHocScriptCodeReference().withScriptRevisionRecord(scriptRevision));
      input.setTableName(tableName);
      input.setRecordList(recordAutomationInput.getRecordList());
      RunAdHocRecordScriptOutput output = new RunAdHocRecordScriptOutput();
      new RunAdHocRecordScriptAction().run(input, output);
   }

}
