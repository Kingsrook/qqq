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
 ** custom actions before an update takes place.
 **
 ** It's important for implementations to be aware of the isPreview field, which
 ** is set to true when the code is running to give users advice, e.g., on a review
 ** screen - vs. being false when the action is ACTUALLY happening.  So, if you're doing
 ** things like storing data, you don't want to do that if isPreview is true!!
 **
 ** General implementation would be, to iterate over the records (the inputs to
 ** the update action), and look at their values:
 ** - possibly adding Errors (`addError`) or Warnings (`addWarning`) to the records
 ** - possibly manipulating values (`setValue`)
 ** - possibly throwing an exception - if you really don't want the update operation to continue.
 ** - doing "whatever else" you may want to do.
 ** - returning the list of records (can be the input list) that you want to go on to the backend implementation class.
 **
 ** Note that the full updateInput is available as a field in this class, and the
 ** "old records" (e.g., with values freshly fetched from the backend) will be
 ** available (if the backend supports it) - both as a list (`getOldRecordList`)
 ** and as a memoized (by this class) map of primaryKey to record (`getOldRecordMap`).
 *******************************************************************************/
public abstract class AbstractPreUpdateCustomizer implements TableCustomizerInterface
{
   protected UpdateInput   updateInput;
   protected List<QRecord> oldRecordList;
   protected boolean       isPreview = false;

   private Map<Serializable, QRecord> oldRecordMap = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> preUpdate(UpdateInput updateInput, List<QRecord> records, boolean isPreview, Optional<List<QRecord>> oldRecordList) throws QException
   {
      this.updateInput = updateInput;
      this.isPreview = isPreview;
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
         for(QRecord qRecord : oldRecordList)
         {
            oldRecordMap.put(qRecord.getValue(updateInput.getTable().getPrimaryKeyField()), qRecord);
         }
      }

      return (oldRecordMap);
   }



   /*******************************************************************************
    ** Getter for isPreview
    *******************************************************************************/
   public boolean getIsPreview()
   {
      return (this.isPreview);
   }



   /*******************************************************************************
    ** Setter for isPreview
    *******************************************************************************/
   public void setIsPreview(boolean isPreview)
   {
      this.isPreview = isPreview;
   }



   /*******************************************************************************
    ** Fluent setter for isPreview
    *******************************************************************************/
   public AbstractPreUpdateCustomizer withIsPreview(boolean isPreview)
   {
      this.isPreview = isPreview;
      return (this);
   }

}
