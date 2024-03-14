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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Abstract class that a table can specify an implementation of, to provide
 ** custom actions after an update takes place.
 **
 ** General implementation would be, to iterate over the records (the outputs of
 ** the update action), and look at their values:
 ** - possibly adding Errors (`addError`) or Warnings (`addWarning`) to the records?
 ** - possibly throwing an exception - though doing so won't stop the update, and instead
 **   will just set a warning on all of the updated records...
 ** - doing "whatever else" you may want to do.
 ** - returning the list of records (can be the input list) that you want to go back to the caller.
 **
 ** Note that the full updateInput is available as a field in this class, and the
 ** "old records" (e.g., with values freshly fetched from the backend) will be
 ** available (if the backend supports it) - both as a list (`getOldRecordList`)
 ** and as a memoized (by this class) map of primaryKey to record (`getOldRecordMap`).
 *******************************************************************************/
public abstract class AbstractPostUpdateCustomizer implements TableCustomizerInterface
{
   protected UpdateInput   updateInput;
   protected List<QRecord> oldRecordList;

   private Map<Serializable, QRecord> oldRecordMap = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> postUpdate(UpdateInput updateInput, List<QRecord> records, Optional<List<QRecord>> oldRecordList) throws QException
   {
      this.updateInput = updateInput;
      this.oldRecordList = oldRecordList.orElse(null);
      return apply(records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract List<QRecord> apply(List<QRecord> records) throws QException;



   /*******************************************************************************
    ** Getter for updateInput
    **
    *******************************************************************************/
   public UpdateInput getUpdateInput()
   {
      return updateInput;
   }



   /*******************************************************************************
    ** Setter for updateInput
    **
    *******************************************************************************/
   public void setUpdateInput(UpdateInput updateInput)
   {
      this.updateInput = updateInput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setOldRecordList(List<QRecord> oldRecordList)
   {
      this.oldRecordList = oldRecordList;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> getOldRecordList()
   {
      return oldRecordList;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected Map<Serializable, QRecord> getOldRecordMap()
   {
      if(oldRecordMap == null)
      {
         oldRecordMap = new HashMap<>();

         if(oldRecordList != null && updateInput != null)
         {
            for(QRecord qRecord : oldRecordList)
            {
               oldRecordMap.put(qRecord.getValue(updateInput.getTable().getPrimaryKeyField()), qRecord);
            }
         }
      }

      return (oldRecordMap);
   }

}
