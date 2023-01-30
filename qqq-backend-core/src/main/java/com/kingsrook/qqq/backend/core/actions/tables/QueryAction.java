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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPostQueryCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.reporting.BufferedRecordPipe;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 ** Action to run a query against a table.
 **
 *******************************************************************************/
public class QueryAction
{
   private static final QLogger LOG = QLogger.getLogger(QueryAction.class);

   private Optional<AbstractPostQueryCustomizer> postQueryRecordCustomizer;

   private QueryInput               queryInput;
   private QPossibleValueTranslator qPossibleValueTranslator;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutput execute(QueryInput queryInput) throws QException
   {
      ActionHelper.validateSession(queryInput);

      postQueryRecordCustomizer = QCodeLoader.getTableCustomizer(AbstractPostQueryCustomizer.class, queryInput.getTable(), TableCustomizers.POST_QUERY_RECORD.getRole());
      this.queryInput = queryInput;

      if(queryInput.getRecordPipe() != null)
      {
         queryInput.getRecordPipe().setPostRecordActions(this::postRecordActions);
      }

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  qModule                  = qBackendModuleDispatcher.getQBackendModule(queryInput.getBackend());
      // todo pre-customization - just get to modify the request?
      QueryOutput queryOutput = qModule.getQueryInterface().execute(queryInput);
      // todo post-customization - can do whatever w/ the result if you want

      if(queryInput.getRecordPipe() instanceof BufferedRecordPipe bufferedRecordPipe)
      {
         bufferedRecordPipe.finalFlush();
      }

      if(queryInput.getRecordPipe() == null)
      {
         postRecordActions(queryOutput.getRecords());
      }

      return queryOutput;
   }



   /*******************************************************************************
    ** Run the necessary actions on a list of records (which must be a mutable list - e.g.,
    ** not one created via List.of()).  This may include setting display values,
    ** translating possible values, and running post-record customizations.
    *******************************************************************************/
   public void postRecordActions(List<QRecord> records)
   {
      if(this.postQueryRecordCustomizer.isPresent())
      {
         records = postQueryRecordCustomizer.get().apply(records);
      }

      if(queryInput.getShouldTranslatePossibleValues())
      {
         if(qPossibleValueTranslator == null)
         {
            qPossibleValueTranslator = new QPossibleValueTranslator(queryInput.getInstance(), queryInput.getSession());
         }
         qPossibleValueTranslator.translatePossibleValuesInRecords(queryInput.getTable(), records, queryInput.getQueryJoins(), queryInput.getFieldsToTranslatePossibleValues());
      }

      if(queryInput.getShouldGenerateDisplayValues())
      {
         QValueFormatter.setDisplayValuesInRecords(queryInput.getTable(), records);
      }
   }
}
