/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.automation;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.automation.polling.PollingAutomationPerTableRunner;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.automation.QAutomationProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.TablesPossibleValueSourceMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Process to manually run table automations, for a table.
 **
 ** Useful, maybe, for an e2e test.  Or, if you don't want jobs to be running,
 ** but want to run automations by-hand, for some reason.
 **
 ** In the future, this class could take a param to only do inserts or updates.
 **
 ** Also, right now, only records that are Pending automations will be run -
 ** again, that could be changed, presumably (take a list of records, always run, etc...)
 *******************************************************************************/
public class RunTableAutomationsProcessStep implements BackendStep, MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "RunTableAutomationsProcess";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData processMetaData = new QProcessMetaData()
         .withName(NAME)
         .withStepList(List.of(
            new QFrontendStepMetaData()
               .withName("input")
               .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
               .withFormField(new QFieldMetaData("tableName", QFieldType.STRING).withIsRequired(true).withPossibleValueSourceName(TablesPossibleValueSourceMetaDataProvider.NAME))
               .withFormField(new QFieldMetaData("automationProviderName", QFieldType.STRING)),
            new QBackendStepMetaData()
               .withName("run")
               .withCode(new QCodeReference(getClass())),
            new QFrontendStepMetaData()
               .withName("output")
               .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
               .withViewField(new QFieldMetaData("ok", QFieldType.STRING))
         ));

      return (processMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      ////////////////////////////////////////////////////////////////////
      // get tableName param (since this process is not table-specific) //
      ////////////////////////////////////////////////////////////////////
      String tableName = runBackendStepInput.getValueString("tableName");
      if(!StringUtils.hasContent(tableName))
      {
         throw (new QException("Missing required input value: tableName"));
      }

      if(!QContext.getQInstance().getTables().containsKey(tableName))
      {
         throw (new QException("Unrecognized table name: " + tableName));
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////
      // get the automation provider name to use - either as the only-one-in-instance, or via param //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      String automationProviderName = runBackendStepInput.getValueString("automationProviderName");
      if(!StringUtils.hasContent(automationProviderName))
      {
         Map<String, QAutomationProviderMetaData> automationProviders = CollectionUtils.nonNullMap(qInstance.getAutomationProviders());
         if(automationProviders.size() == 1)
         {
            automationProviderName = automationProviders.keySet().iterator().next();
         }
         else
         {
            throw (new QException("Missing required input value: automationProviderName (and there is not exactly 1 in the active instance)"));
         }
      }

      /////////////////////////////////////////////
      // run automations for the requested table //
      /////////////////////////////////////////////
      List<PollingAutomationPerTableRunner.TableActionsInterface> tableActions = PollingAutomationPerTableRunner.getTableActions(qInstance, automationProviderName);
      for(PollingAutomationPerTableRunner.TableActionsInterface tableAction : tableActions)
      {
         if(tableName.equals(tableAction.tableName()))
         {
            PollingAutomationPerTableRunner pollingAutomationPerTableRunner = new PollingAutomationPerTableRunner(qInstance, automationProviderName, () -> QContext.getQSession(), tableAction);
            pollingAutomationPerTableRunner.processTableInsertOrUpdate(qInstance.getTable(tableAction.tableName()), tableAction.status());
         }
      }

      runBackendStepOutput.addValue("ok", "true");
   }

}
