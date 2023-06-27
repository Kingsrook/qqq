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
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.scripts.TestScriptActionInterface;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptType;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Action to load the details necessary to test a script.
 **
 *******************************************************************************/
public class LoadScriptTestDetailsProcessStep implements BackendStep
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

         Integer  scriptTypeId = input.getValueInteger("scriptTypeId");
         GetInput getInput     = new GetInput();
         getInput.setTableName(ScriptType.TABLE_NAME);
         getInput.setPrimaryKey(scriptTypeId);
         GetOutput  getOutput  = new GetAction().execute(getInput);
         ScriptType scriptType = new ScriptType(getOutput.getRecord());

         TestScriptActionInterface testScriptActionInterface = QCodeLoader.getAdHoc(TestScriptActionInterface.class, new QCodeReference(scriptType.getTestScriptInterfaceName(), QCodeType.JAVA));

         QInstanceEnricher qInstanceEnricher = new QInstanceEnricher(new QInstance());

         ArrayList<QFieldMetaData> inputFields = new ArrayList<>();
         for(QFieldMetaData testInputField : CollectionUtils.nonNullList(testScriptActionInterface.getTestInputFields()))
         {
            qInstanceEnricher.enrichField(testInputField);
            inputFields.add(testInputField);
         }

         ArrayList<QFieldMetaData> outputFields = new ArrayList<>();
         for(QFieldMetaData testOutputField : CollectionUtils.nonNullList(testScriptActionInterface.getTestOutputFields()))
         {
            qInstanceEnricher.enrichField(testOutputField);
            outputFields.add(testOutputField);
         }

         output.addValue("testInputFields", inputFields);
         output.addValue("testOutputFields", outputFields);
      }
      catch(Exception e)
      {
         output.addValue("exception", e);
      }
   }

}
