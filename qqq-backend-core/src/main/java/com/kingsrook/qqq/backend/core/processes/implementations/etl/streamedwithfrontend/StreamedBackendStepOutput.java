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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Subclass of RunBackendStepOutput, meant for use in the pseudo-steps used by
 ** the Streamed-ETL-with-frontend processes - where the Record list is not the
 ** full process's record list - rather - is just a page at a time -- so this class
 ** overrides the getRecords and setRecords method, to just work with that page.
 *******************************************************************************/
public class StreamedBackendStepOutput extends RunBackendStepOutput
{
   private List<QRecord> outputRecords = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public StreamedBackendStepOutput(RunBackendStepOutput runBackendStepOutput)
   {
      super();
      setValues(runBackendStepOutput.getValues());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setRecords(List<QRecord> records)
   {
      this.outputRecords = records;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> getRecords()
   {
      return (outputRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void addRecord(QRecord record)
   {
      if(this.outputRecords == null)
      {
         this.outputRecords = new ArrayList<>();
      }
      this.outputRecords.add(record);
   }
}
