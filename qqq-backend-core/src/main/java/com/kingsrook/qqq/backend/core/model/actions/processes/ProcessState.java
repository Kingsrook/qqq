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

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class ProcessState implements Serializable
{
   private List<QRecord>             records      = new ArrayList<>();
   private Map<String, Serializable> values       = new HashMap<>();
   private List<String>              stepList     = new ArrayList<>();
   private Optional<String>          nextStepName = Optional.empty();

   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // maybe, remove this altogether - just let the frontend compute & send if needed... but how does it know last version...? //
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   private List<QFrontendStepMetaData> updatedFrontendStepList = null;



   /*******************************************************************************
    ** Getter for records
    **
    *******************************************************************************/
   public List<QRecord> getRecords()
   {
      return records;
   }



   /*******************************************************************************
    ** Setter for records
    **
    *******************************************************************************/
   public void setRecords(List<QRecord> records)
   {
      this.records = records;
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Getter for nextStepName
    **
    *******************************************************************************/
   public Optional<String> getNextStepName()
   {
      return nextStepName;
   }



   /*******************************************************************************
    ** Setter for nextStepName
    **
    *******************************************************************************/
   public void setNextStepName(String nextStepName)
   {
      this.nextStepName = Optional.of(nextStepName);
   }



   /*******************************************************************************
    ** clear out the value of nextStepName (set the Optional to empty)
    **
    *******************************************************************************/
   public void clearNextStepName()
   {
      this.nextStepName = Optional.empty();
   }



   /*******************************************************************************
    ** Getter for stepList
    **
    *******************************************************************************/
   public List<String> getStepList()
   {
      return stepList;
   }



   /*******************************************************************************
    ** Setter for stepList
    **
    *******************************************************************************/
   public void setStepList(List<String> stepList)
   {
      this.stepList = stepList;
   }



   /*******************************************************************************
    ** Getter for updatedFrontendStepList
    *******************************************************************************/
   public List<QFrontendStepMetaData> getUpdatedFrontendStepList()
   {
      return (this.updatedFrontendStepList);
   }



   /*******************************************************************************
    ** Setter for updatedFrontendStepList
    *******************************************************************************/
   public void setUpdatedFrontendStepList(List<QFrontendStepMetaData> updatedFrontendStepList)
   {
      this.updatedFrontendStepList = updatedFrontendStepList;
   }



   /*******************************************************************************
    ** Fluent setter for updatedFrontendStepList
    *******************************************************************************/
   public ProcessState withUpdatedFrontendStepList(List<QFrontendStepMetaData> updatedFrontendStepList)
   {
      this.updatedFrontendStepList = updatedFrontendStepList;
      return (this);
   }

}
