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

package com.kingsrook.qqq.backend.core.processes.implementations.savedviews;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.savedviews.SavedView;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Process used by the saved view dialogs
 *******************************************************************************/
public class QuerySavedViewProcess implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(QuerySavedViewProcess.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessMetaData getProcessMetaData()
   {
      return (new QProcessMetaData()
         .withName("querySavedView")
         .withStepList(List.of(
            new QBackendStepMetaData()
               .withCode(new QCodeReference(QuerySavedViewProcess.class))
               .withName("query")
         )));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ActionHelper.validateSession(runBackendStepInput);
      Integer savedViewId = runBackendStepInput.getValueInteger("id");

      try
      {
         if(savedViewId != null)
         {
            GetInput input = new GetInput();
            input.setTableName(SavedView.TABLE_NAME);
            input.setPrimaryKey(savedViewId);

            GetOutput output = new GetAction().execute(input);
            if(output.getRecord() == null)
            {
               throw (new QNotFoundException("The requested view was not found."));
            }

            runBackendStepOutput.addRecord(output.getRecord());
            runBackendStepOutput.addValue("savedView", output.getRecord());
            runBackendStepOutput.addValue("savedViewList", (Serializable) List.of(output.getRecord()));
         }
         else
         {
            String tableName = runBackendStepInput.getValueString("tableName");

            QueryInput input = new QueryInput();
            input.setTableName(SavedView.TABLE_NAME);
            input.setFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("tableName", QCriteriaOperator.EQUALS, tableName))
               .withOrderBy(new QFilterOrderBy("label")));

            QueryOutput output = new QueryAction().execute(input);
            runBackendStepOutput.setRecords(output.getRecords());
            runBackendStepOutput.addValue("savedViewList", (Serializable) output.getRecords());
         }
      }
      catch(QNotFoundException qnfe)
      {
         LOG.info("View not found", logPair("savedViewId", savedViewId));
         throw (qnfe);
      }
      catch(Exception e)
      {
         LOG.warn("Error querying for saved views", e);
         throw (e);
      }
   }
}
