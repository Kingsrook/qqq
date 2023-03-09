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


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Subclass of RunBackendStepInput, meant for use in the postRun of the transform/load
 ** steps of a Streamed-ETL-with-frontend processes - where the Record list is not the
 ** full process's record list - rather - is just a preview (e.g., first n).
 ** overrides the getRecords and setRecords method.
 **
 *******************************************************************************/
public class BackendStepPostRunInput extends RunBackendStepInput
{
   private List<QRecord> previewRecordList;



   /*******************************************************************************
    **
    *******************************************************************************/
   public BackendStepPostRunInput(RunBackendStepInput runBackendStepInput)
   {
      super();
      runBackendStepInput.cloneFieldsInto(this);
      previewRecordList = runBackendStepInput.getRecords();
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
