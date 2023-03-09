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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Subclass of RunBackendStepOutput, meant for use in the pseudo-steps used by
 ** the Streamed-ETL-with-frontend processes - where the Record list is not the
 ** full process's record list - rather - is just a preview of the records - e.g.,
 ** the first n.
 *******************************************************************************/
public class BackendStepPostRunOutput extends RunBackendStepOutput
{
   private List<QRecord> previewRecordList;



   /*******************************************************************************
    **
    *******************************************************************************/
   public BackendStepPostRunOutput(RunBackendStepOutput runBackendStepOutput)
   {
      super();
      setValues(runBackendStepOutput.getValues());
      previewRecordList = runBackendStepOutput.getRecords();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> getRecords()
   {
      throw (new IllegalStateException("Method getRecords should not be called in a post-run - as it is NOT a full record list.  Call getPreviewRecordList to get a subset of the process's output records."));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> getPreviewRecordList()
   {
      return (this.previewRecordList);
   }
}
