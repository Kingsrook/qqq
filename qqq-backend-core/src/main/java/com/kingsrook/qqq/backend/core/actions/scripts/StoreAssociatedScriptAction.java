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
import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.StoreAssociatedScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.StoreAssociatedScriptOutput;
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
import com.kingsrook.qqq.backend.core.model.metadata.tables.AssociatedScript;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Action to store a new version of a script, associated with a record.
 **
 ** If there's never been a script assigned to the record (for the specified field),
 ** then a new Script record is first inserted.
 **
 ** The script referenced by the record is always updated to point at the new
 ** scriptRevision record that is inserted.
 **
 *******************************************************************************/
public class StoreAssociatedScriptAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public void run(StoreAssociatedScriptInput input, StoreAssociatedScriptOutput output) throws QException
   {
      ActionHelper.validateSession(input);

      QTableMetaData             table               = input.getTable();
      Optional<AssociatedScript> optAssociatedScript = table.getAssociatedScripts().stream().filter(as -> as.getFieldName().equals(input.getFieldName())).findFirst();
      if(optAssociatedScript.isEmpty())
      {
         throw (new QException("Field to update associated script for is not an associated script field."));
      }
      AssociatedScript associatedScript = optAssociatedScript.get();

      /////////////////////////////////////////////////////////////
      // get the record that the script is to be associated with //
      /////////////////////////////////////////////////////////////
      QRecord associatedRecord;
      {
         GetInput getInput = new GetInput();
         getInput.setTableName(input.getTableName());
         getInput.setPrimaryKey(input.getRecordPrimaryKey());
         getInput.setShouldGenerateDisplayValues(true);
         GetOutput getOutput = new GetAction().execute(getInput);
         associatedRecord = getOutput.getRecord();
      }
      if(associatedRecord == null)
      {
         throw (new QException("Record to associated with script was not found."));
      }

      //////////////////////////////////////////////////////////////////
      // check if there's currently a script referenced by the record //
      //////////////////////////////////////////////////////////////////
      Serializable existingScriptId = associatedRecord.getValueString(input.getFieldName());
      QRecord      script;
      Integer      nextSequenceNo   = 1;
      if(existingScriptId == null)
      {
         ////////////////////////////////////////////////////////////////////
         // get the script type - that'll be part of the new script's name //
         ////////////////////////////////////////////////////////////////////
         GetInput getInput = new GetInput();
         getInput.setTableName("scriptType");
         getInput.setPrimaryKey(associatedScript.getScriptTypeId());
         getInput.setShouldGenerateDisplayValues(true);
         GetOutput getOutput  = new GetAction().execute(getInput);
         QRecord   scriptType = getOutput.getRecord();
         if(scriptType == null)
         {
            throw (new QException("Script type [" + associatedScript.getScriptTypeId() + "] was not found."));
         }

         /////////////////////////
         // insert a new script //
         /////////////////////////
         script = new QRecord();
         script.setValue("scriptTypeId", associatedScript.getScriptTypeId());
         script.setValue("name", associatedRecord.getRecordLabel() + " - " + scriptType.getRecordLabel());
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName("script");
         insertInput.setRecords(List.of(script));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         script = insertOutput.getRecords().get(0);

         /////////////////////////////////////////////////////////////
         // update the associated record to point at the new script //
         /////////////////////////////////////////////////////////////
         UpdateInput updateInput = new UpdateInput();
         updateInput.setTableName(input.getTableName());
         updateInput.setRecords(List.of(new QRecord()
            .withValue(table.getPrimaryKeyField(), associatedRecord.getValue(table.getPrimaryKeyField()))
            .withValue(input.getFieldName(), script.getValue("id"))
         ));
         new UpdateAction().execute(updateInput);
      }
      else
      {
         ////////////////////////////////////////
         // get the existing script, to update //
         ////////////////////////////////////////
         GetInput getInput = new GetInput();
         getInput.setTableName("script");
         getInput.setPrimaryKey(existingScriptId);
         GetOutput getOutput = new GetAction().execute(getInput);
         script = getOutput.getRecord();

         QueryInput queryInput = new QueryInput();
         queryInput.setTableName("scriptRevision");
         queryInput.setFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("scriptId", QCriteriaOperator.EQUALS, List.of(script.getValue("id"))))
            .withOrderBy(new QFilterOrderBy("sequenceNo", false))
         );
         queryInput.setLimit(1);
         QueryOutput queryOutput = new QueryAction().execute(queryInput);
         if(!queryOutput.getRecords().isEmpty())
         {
            nextSequenceNo = queryOutput.getRecords().get(0).getValueInteger("sequenceNo") + 1;
         }
      }

      //////////////////////////////////
      // insert a new script revision //
      //////////////////////////////////
      String commitMessage = input.getCommitMessage();
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
         .withValue("contents", input.getCode())
         .withValue("commitMessage", commitMessage)
         .withValue("sequenceNo", nextSequenceNo);

      try
      {
         scriptRevision.setValue("author", input.getSession().getUser().getFullName());
      }
      catch(Exception e)
      {
         scriptRevision.setValue("author", "Unknown");
      }

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName("scriptRevision");
      insertInput.setRecords(List.of(scriptRevision));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      scriptRevision = insertOutput.getRecords().get(0);

      ////////////////////////////////////////////////////
      // update the script to point at the new revision //
      ////////////////////////////////////////////////////
      script.setValue("currentScriptRevisionId", scriptRevision.getValue("id"));
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName("script");
      updateInput.setRecords(List.of(script));
      new UpdateAction().execute(updateInput);

      output.setScriptId(script.getValueInteger("id"));
      output.setScriptName(script.getValueString("name"));
      output.setScriptRevisionId(scriptRevision.getValueInteger("id"));
      output.setScriptRevisionSequenceNo(scriptRevision.getValueInteger("sequenceNo"));
   }
}
