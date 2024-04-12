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

package com.kingsrook.qqq.backend.core.processes.implementations.garbagecollector;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;


/*******************************************************************************
 **
 *******************************************************************************/
public class GarbageCollectorExtractStep extends ExtractViaQueryStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected QQueryFilter getQueryFilter(RunBackendStepInput runBackendStepInput) throws QException
   {
      //////////////////////////////////////////////////////////////////////////////////////////
      // in case the process was executed via a frontend, and the user specified a limitDate, //
      // then put that date in the defaultQueryFilter, rather than the default                //
      //////////////////////////////////////////////////////////////////////////////////////////
      Instant limitDate = ValueUtils.getValueAsInstant(runBackendStepInput.getValue("limitDate"));
      if(limitDate != null)
      {
         QQueryFilter defaultQueryFilter = (QQueryFilter) runBackendStepInput.getValue("defaultQueryFilter");
         defaultQueryFilter.getCriteria().get(0).setValues(ListBuilder.of(limitDate));
      }

      return super.getQueryFilter(runBackendStepInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected void customizeInputPreQuery(QueryInput queryInput)
   {
      queryInput.withQueryHint(QueryInput.QueryHint.POTENTIALLY_LARGE_NUMBER_OF_RESULTS);
   }

}
