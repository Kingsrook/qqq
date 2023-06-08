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


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Abstract class that a table can specify an implementation of, to provide
 ** custom actions before a delete takes place.
 **
 ** It's important for implementations to be aware of the isPreview field, which
 ** is set to true when the code is running to give users advice, e.g., on a review
 ** screen - vs. being false when the action is ACTUALLY happening.  So, if you're doing
 ** things like storing data, you don't want to do that if isPreview is true!!
 **
 ** General implementation would be, to iterate over the records (which the DeleteAction
 ** would look up based on the inputs to the delete action), and look at their values:
 ** - possibly adding Errors (`addError`) or Warnings (`addWarning`) to the records
 ** - possibly throwing an exception - if you really don't want the delete operation to continue.
 ** - doing "whatever else" you may want to do.
 ** - returning the list of records (can be the input list) - this is how errors
 **   and warnings are propagated to the DeleteAction.  Note that any records with
 **   an error will NOT proceed to the backend's delete interface - but those with
 **   warnings will.
 **
 ** Note that the full deleteInput is available as a field in this class.
 **
 *******************************************************************************/
public abstract class AbstractPreDeleteCustomizer
{
   protected DeleteInput deleteInput;

   protected boolean isPreview = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract List<QRecord> apply(List<QRecord> records) throws QException;



   /*******************************************************************************
    ** Getter for deleteInput
    **
    *******************************************************************************/
   public DeleteInput getDeleteInput()
   {
      return deleteInput;
   }



   /*******************************************************************************
    ** Setter for deleteInput
    **
    *******************************************************************************/
   public void setDeleteInput(DeleteInput deleteInput)
   {
      this.deleteInput = deleteInput;
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
   public AbstractPreDeleteCustomizer withIsPreview(boolean isPreview)
   {
      this.isPreview = isPreview;
      return (this);
   }

}
