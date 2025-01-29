/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping;


import java.io.Serializable;


/*******************************************************************************
 ** Specialized error for records, for bulk-load use-cases, where we want to
 ** report back info to the user about the field & value.
 *******************************************************************************/
public class BulkLoadPossibleValueError extends AbstractBulkLoadRollableValueError
{
   private final String       fieldLabel;
   private final Serializable value;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BulkLoadPossibleValueError(String fieldName, Serializable value, String fieldLabel)
   {
      super("Value [" + value + "] for field [" + fieldLabel + "] is not a valid option");
      this.value = value;
      this.fieldLabel = fieldLabel;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String getMessageToUseAsProcessSummaryRollupKey()
   {
      return ("Unrecognized value for field [" + fieldLabel + "]");
   }



   /*******************************************************************************
    ** Getter for value
    **
    *******************************************************************************/
   @Override
   public Serializable getValue()
   {
      return value;
   }
}
