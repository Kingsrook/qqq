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

package com.kingsrook.qqq.backend.core.processes.implementations.basepull;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;


/*******************************************************************************
 ** Version of ExtractViaQueryStep that knows how to set up a basepull query.
 *******************************************************************************/
public class ExtractViaBasepullQueryStep extends ExtractViaQueryStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   protected QQueryFilter getQueryFilter(RunBackendStepInput runBackendStepInput) throws QException
   {
      //////////////////////////////////////////////////////////////
      // get input query filter or if not found, create a new one //
      //////////////////////////////////////////////////////////////
      QQueryFilter queryFilter = new QQueryFilter();
      try
      {
         queryFilter = super.getQueryFilter(runBackendStepInput);
         return (queryFilter);
      }
      catch(QException qe)
      {
         ///////////////////////////////////////////////////////////////////////////////////////
         // if we catch here, assume that is because there was no default filter, continue on //
         ///////////////////////////////////////////////////////////////////////////////////////
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // build up a query filter that is against the source table for the given source table timestamp //
      // field, finding any records that need processed.                                               //
      // query will be for:  timestamp > lastRun AND timestamp <= thisRun.                             //
      // then thisRun will be stored, so the next run shouldn't find any records from thisRun.         //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      queryFilter.addCriteria(new QFilterCriteria()
         .withFieldName(runBackendStepInput.getValueString(RunProcessAction.BASEPULL_TIMESTAMP_FIELD))
         .withOperator(QCriteriaOperator.GREATER_THAN)
         .withValues(List.of(getLastRunTimeString(runBackendStepInput))));

      queryFilter.addCriteria(new QFilterCriteria()
         .withFieldName(runBackendStepInput.getValueString(RunProcessAction.BASEPULL_TIMESTAMP_FIELD))
         .withOperator(QCriteriaOperator.LESS_THAN_OR_EQUALS)
         .withValues(List.of(getThisRunTimeString(runBackendStepInput))));

      queryFilter.addOrderBy(new QFilterOrderBy(runBackendStepInput.getValueString(RunProcessAction.BASEPULL_TIMESTAMP_FIELD)));

      /////////////////////////////////////////////////////////////////////////////////////
      // put a flag in the process's values, to note that we did use the timestamp field //
      // this will later be checked to see if we should update the timestamp too.        //
      /////////////////////////////////////////////////////////////////////////////////////
      runBackendStepInput.addValue(RunProcessAction.BASEPULL_DID_QUERY_USING_TIMESTAMP_FIELD, true);

      return (queryFilter);
   }



   /*******************************************************************************
    ** Let a subclass know if getQueryFilter will use the "default filter" (e.g., from
    ** our base class, which would come from values passed in to the process), or if
    ** the BasePull Query would be used (e.g., for a scheduled job).
    *******************************************************************************/
   protected boolean willTheBasePullQueryBeUsed(RunBackendStepInput runBackendStepInput)
   {
      try
      {
         super.getQueryFilter(runBackendStepInput);

         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // if super.getQueryFilter returned - then - there's a default query to use (e.g., a user selecting rows on a screen). //
         // this means we won't use the BasePull query, so return a false here.                                                 //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         return (false);
      }
      catch(QException qe)
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // if we catch here, assume that is because there was no default filter - in which case - we'll use the BasePull Query //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         return (true);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String getLastRunTimeString(RunBackendStepInput runBackendStepInput) throws QException
   {
      return (runBackendStepInput.getBasepullLastRunTime().toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String getThisRunTimeString(RunBackendStepInput runBackendStepInput) throws QException
   {
      return (runBackendStepInput.getValueInstant(RunProcessAction.BASEPULL_THIS_RUNTIME_KEY).toString());
   }
}
