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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.InputSource;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ProcessSummaryProviderInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkInsertLoadStep extends LoadViaInsertStep implements ProcessSummaryProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(BulkInsertLoadStep.class);

   private Serializable firstInsertedPrimaryKey = null;
   private Serializable lastInsertedPrimaryKey  = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected InputSource getInputSource()
   {
      return (QInputSource.USER);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      super.runOnePage(runBackendStepInput, runBackendStepOutput);

      QTableMetaData table = QContext.getQInstance().getTable(runBackendStepInput.getValueString("tableName"));

      List<QRecord> insertedRecords = runBackendStepOutput.getRecords();
      for(QRecord insertedRecord : insertedRecords)
      {
         if(CollectionUtils.nullSafeIsEmpty(insertedRecord.getErrors()))
         {
            if(firstInsertedPrimaryKey == null)
            {
               firstInsertedPrimaryKey = insertedRecord.getValue(table.getPrimaryKeyField());
            }

            lastInsertedPrimaryKey = insertedRecord.getValue(table.getPrimaryKeyField());
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> processSummary = getTransformStep().getProcessSummary(runBackendStepOutput, isForResultScreen);

      try
      {
         if(firstInsertedPrimaryKey != null)
         {
            QTableMetaData table = QContext.getQInstance().getTable(runBackendStepOutput.getValueString("tableName"));
            QFieldMetaData field = table.getField(table.getPrimaryKeyField());
            if(field.getType().isNumeric())
            {
               ProcessSummaryLine idsLine = new ProcessSummaryLine(Status.INFO, "Inserted " + field.getLabel() + " values between " + firstInsertedPrimaryKey + " and " + lastInsertedPrimaryKey);
               idsLine.setCount(null);
               processSummary.add(idsLine);
            }
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error adding inserted-keys process summary line", e);
      }

      return (processSummary);
   }
}
