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
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Abstract class that a table can specify an implementation of, to provide
 ** custom actions before an insert takes place.
 **
 ** General implementation would be, to iterate over the records (the inputs to
 ** the insert action), and look at their values:
 ** - possibly adding Errors (`addError`) or Warnings (`addWarning`) to the records
 ** - possibly manipulating values (`setValue`)
 ** - possibly throwing an exception - if you really don't want the insert operation to continue.
 ** - doing "whatever else" you may want to do.
 ** - returning the list of records (can be the input list) that you want to go on to the backend implementation class.
 **
 ** Note that the full insertInput is available as a field in this class.
 *******************************************************************************/
public abstract class AbstractPreInsertCustomizer
{
   protected InsertInput insertInput;



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract List<QRecord> apply(List<QRecord> records);



   /*******************************************************************************
    ** Getter for insertInput
    **
    *******************************************************************************/
   public InsertInput getInsertInput()
   {
      return insertInput;
   }



   /*******************************************************************************
    ** Setter for insertInput
    **
    *******************************************************************************/
   public void setInsertInput(InsertInput insertInput)
   {
      this.insertInput = insertInput;
   }
}
