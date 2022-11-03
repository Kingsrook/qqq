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

package com.kingsrook.qqq.backend.core.model.actions.tables.update;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 * Output for an update action
 *
 *******************************************************************************/
public class UpdateOutput extends AbstractActionOutput
{
   private List<QRecord> records;



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> getRecords()
   {
      return records;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setRecords(List<QRecord> records)
   {
      this.records = records;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addRecord(QRecord record)
   {
      if(this.records == null)
      {
         this.records = new ArrayList<>();
      }
      this.records.add(record);
   }
}
