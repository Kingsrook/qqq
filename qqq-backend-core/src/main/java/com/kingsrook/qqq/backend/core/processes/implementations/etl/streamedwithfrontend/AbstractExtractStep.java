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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;


/*******************************************************************************
 ** Base class for the Extract logic of Streamed ETL processes.
 **
 ** These steps are invoked by both the "preview" and the "execute" steps of a
 ** StreamedETLWithFrontend process.
 **
 ** Key here, is that subclasses here should put records that they're "Extracting"
 ** into the recordPipe member.  That is to say, DO NOT use the recordList in
 ** the Step input/output objects.
 **
 ** Ideally, they'll also stop once they've hit the "limit" number of records
 ** (though if you keep going, the pipe will get terminated and the job will be
 ** cancelled, etc...).
 *******************************************************************************/
public abstract class AbstractExtractStep implements BackendStep
{
   private RecordPipe recordPipe;
   private Integer    limit;



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer doCount(RunBackendStepInput runBackendStepInput) throws QException
   {
      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setRecordPipe(RecordPipe recordPipe)
   {
      this.recordPipe = recordPipe;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public RecordPipe getRecordPipe()
   {
      return recordPipe;
   }



   /*******************************************************************************
    ** Getter for limit
    **
    *******************************************************************************/
   public Integer getLimit()
   {
      return limit;
   }



   /*******************************************************************************
    ** Setter for limit
    **
    *******************************************************************************/
   public void setLimit(Integer limit)
   {
      this.limit = limit;
   }

}
