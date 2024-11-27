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
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;


/*******************************************************************************
 ** Specialized error for records, for bulk-load use-cases, where we want to
 ** report back info to the user about the field & value.
 *******************************************************************************/
public class BulkLoadValueTypeError extends BadInputStatusMessage
{
   private final String       fieldLabel;
   private final String       fieldName;
   private final Serializable value;
   private final QFieldType   type;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BulkLoadValueTypeError(String fieldName, Serializable value, QFieldType type, String fieldLabel)
   {
      super("Value [" + value + "] for field [" + fieldLabel + "] could not be converted to type [" + type + "]");
      this.fieldName = fieldName;
      this.value = value;
      this.type = type;
      this.fieldLabel = fieldLabel;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public String getMessageToUseAsProcessSummaryRollupKey()
   {
      return ("Cannot convert value for field [" + fieldLabel + "] to type [" + type.getMixedCaseLabel() + "]");
   }



   /*******************************************************************************
    ** Getter for value
    **
    *******************************************************************************/
   public Serializable getValue()
   {
      return value;
   }
}
