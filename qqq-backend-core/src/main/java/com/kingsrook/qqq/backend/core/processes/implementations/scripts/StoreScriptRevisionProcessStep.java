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


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevisionFile;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Action to store a new version (revision) of a script.
 **
 ** The script referenced by the record is always updated to point at the new
 ** scriptRevision record that is inserted.
 **
 *******************************************************************************/
public class StoreScriptRevisionProcessStep implements BackendStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      InsertAction insertAction = new InsertAction();
      InsertInput  insertInput  = new InsertInput();
      insertInput.setTableName("scriptRevision");
      QBackendTransaction transaction = insertAction.openTransaction(insertInput);
      insertInput.setTransaction(transaction);

      try
      {
         ActionHelper.validateSession(input);

         //////////////////////////////////////////////////////////////////
         // check if there's currently a script referenced by the record //
         //////////////////////////////////////////////////////////////////
         Integer scriptId       = input.getValueInteger("scriptId");
         Integer nextSequenceNo = 1;

         ////////////////////////////////////////
         // get the existing script, to update //
         ////////////////////////////////////////
         GetInput getInput = new GetInput();
         getInput.setTableName("script");
         getInput.setPrimaryKey(scriptId);
         getInput.setTransaction(transaction);
         GetOutput getOutput = new GetAction().execute(getInput);
         QRecord   script    = getOutput.getRecord();

         QueryInput queryInput = new QueryInput();
         queryInput.setTableName("scriptRevision");
         queryInput.setFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("scriptId", QCriteriaOperator.EQUALS, List.of(script.getValue("id"))))
            .withOrderBy(new QFilterOrderBy("sequenceNo", false))
            .withLimit(1));
         queryInput.setTransaction(transaction);
         QueryOutput queryOutput = new QueryAction().execute(queryInput);
         if(!queryOutput.getRecords().isEmpty())
         {
            nextSequenceNo = queryOutput.getRecords().get(0).getValueInteger("sequenceNo") + 1;
         }

         //////////////////////////////////
         // insert a new script revision //
         //////////////////////////////////
         String commitMessage = input.getValueString("commitMessage");
         if(!StringUtils.hasContent(commitMessage))
         {
            if(nextSequenceNo == 1)
            {
               commitMessage = "Initial version";
            }
            else
            {
               commitMessage = "No commit message given";
            }
         }

         QRecord scriptRevision = new QRecord()
            .withValue("scriptId", script.getValue("id"))
            .withValue("apiName", input.getValueString("apiName"))
            .withValue("apiVersion", input.getValueString("apiVersion"))
            .withValue("commitMessage", commitMessage)
            .withValue("sequenceNo", nextSequenceNo);

         if(input.getValue("contents") != null)
         {
            scriptRevision.withValue("contents", input.getValueString("contents"));
         }

         try
         {
            scriptRevision.setValue("author", input.getSession().getUser().getFullName());
         }
         catch(Exception e)
         {
            scriptRevision.setValue("author", "Unknown");
         }

         insertInput.setRecords(List.of(scriptRevision));
         InsertOutput insertOutput = insertAction.execute(insertInput);
         scriptRevision = insertOutput.getRecords().get(0);
         Integer scriptRevisionId = scriptRevision.getValueInteger("id");

         //////////////////////////////////////////////////////////////////////////////////////////
         // if there's a list of file contents (instead of just a single string), store them all //
         //////////////////////////////////////////////////////////////////////////////////////////
         @SuppressWarnings("unchecked")
         List<QRecord> fileContents = (List<QRecord>) input.getValue("fileContents");
         if(CollectionUtils.nullSafeHasContents(fileContents))
         {
            List<QRecord> scriptRevisionRecords = fileContents.stream().map(r -> new ScriptRevisionFile()
               .withScriptRevisionId(scriptRevisionId)
               .withFileName(r.getValueString("fileName"))
               .withContents(r.getValueString("contents"))
               .toQRecord()).toList();

            InsertInput scriptRevisionFileInsertInput = new InsertInput();
            scriptRevisionFileInsertInput.setTableName(ScriptRevisionFile.TABLE_NAME);
            scriptRevisionFileInsertInput.setRecords(scriptRevisionRecords);
            scriptRevisionFileInsertInput.setTransaction(transaction);
            new InsertAction().execute(scriptRevisionFileInsertInput);
         }

         ////////////////////////////////////////////////////
         // update the script to point at the new revision //
         ////////////////////////////////////////////////////
         script.setValue("currentScriptRevisionId", scriptRevision.getValue("id"));
         UpdateInput updateInput = new UpdateInput();
         updateInput.setTableName("script");
         updateInput.setRecords(List.of(script));
         updateInput.setTransaction(transaction);
         new UpdateAction().execute(updateInput);

         transaction.commit();

         output.addValue("scriptId", script.getValueInteger("id"));
         output.addValue("scriptName", script.getValueString("name"));
         output.addValue("scriptRevisionId", scriptRevisionId);
         output.addValue("scriptRevisionSequenceNo", scriptRevision.getValueInteger("sequenceNo"));
      }
      catch(Exception e)
      {
         transaction.rollback();
      }
      finally
      {
         transaction.close();
      }
   }

}
