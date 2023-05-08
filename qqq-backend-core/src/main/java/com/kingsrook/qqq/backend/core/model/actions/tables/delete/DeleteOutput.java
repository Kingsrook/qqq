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

package com.kingsrook.qqq.backend.core.model.actions.tables.delete;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 * Output for a delete action
 *
 *******************************************************************************/
public class DeleteOutput extends AbstractActionOutput implements Serializable
{
   private int           deletedRecordCount = 0;
   private List<QRecord> recordsWithErrors;
   private List<QRecord> recordsWithWarnings;



   /*******************************************************************************
    ** Getter for deletedRecordCount
    **
    *******************************************************************************/
   public int getDeletedRecordCount()
   {
      return deletedRecordCount;
   }



   /*******************************************************************************
    ** Setter for deletedRecordCount
    **
    *******************************************************************************/
   public void setDeletedRecordCount(int deletedRecordCount)
   {
      this.deletedRecordCount = deletedRecordCount;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> getRecordsWithErrors()
   {
      return recordsWithErrors;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setRecordsWithErrors(List<QRecord> recordsWithErrors)
   {
      this.recordsWithErrors = recordsWithErrors;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addRecordWithError(QRecord recordWithError)
   {
      if(this.recordsWithErrors == null)
      {
         this.recordsWithErrors = new ArrayList<>();
      }
      this.recordsWithErrors.add(recordWithError);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addToDeletedRecordCount(int i)
   {
      deletedRecordCount += i;
   }



   /*******************************************************************************
    ** Getter for recordsWithWarnings
    *******************************************************************************/
   public List<QRecord> getRecordsWithWarnings()
   {
      return (this.recordsWithWarnings);
   }



   /*******************************************************************************
    ** Setter for recordsWithWarnings
    *******************************************************************************/
   public void setRecordsWithWarnings(List<QRecord> recordsWithWarnings)
   {
      this.recordsWithWarnings = recordsWithWarnings;
   }



   /*******************************************************************************
    ** Fluent setter for recordsWithWarnings
    *******************************************************************************/
   public DeleteOutput withRecordsWithWarnings(List<QRecord> recordsWithWarnings)
   {
      this.recordsWithWarnings = recordsWithWarnings;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addRecordWithWarning(QRecord recordWithWarning)
   {
      if(this.recordsWithWarnings == null)
      {
         this.recordsWithWarnings = new ArrayList<>();
      }
      this.recordsWithWarnings.add(recordWithWarning);
   }

}
