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
 ** custom actions after a delete takes place.
 **
 ** General implementation would be, to iterate over the records (ones which didn't
 ** have a delete error), and look at their values:
 ** - possibly adding Errors (`addError`) or Warnings (`addWarning`) to the records?
 ** - possibly throwing an exception - though doing so won't stop the delete, and instead
 **   will just set a warning on all of the deleted records...
 ** - doing "whatever else" you may want to do.
 ** - returning the list of records (can be the input list) that you want to go back
 **   to the caller - this is how errors and warnings are propagated .
 **
 ** Note that the full deleteInput is available as a field in this class.
 **
 ** A future enhancement here may be to take (as fields in this class) the list of
 ** records that the delete action marked in error - the user might want to do
 ** something special with them (idk, try some other way to delete them?)
 *******************************************************************************/
public abstract class AbstractPostDeleteCustomizer implements TableCustomizerInterface
{
   protected DeleteInput deleteInput;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> postDelete(DeleteInput deleteInput, List<QRecord> records) throws QException
   {
      this.deleteInput = deleteInput;
      return apply(records);
   }



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
}
