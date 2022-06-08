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

package com.kingsrook.qqq.backend.core.model.data;


import java.util.List;


/*******************************************************************************
 ** Wrapper on a QRecord, to add status information after an action took place.
 ** e.g., any errors that occurred.
 **
 ** TODO - expand?
 **
 *******************************************************************************/
public class QRecordWithStatus extends QRecord
{
   private List<Exception> errors;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecordWithStatus()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecordWithStatus(QRecord record)
   {
      super.setTableName(record.getTableName());
      super.setValues(record.getValues());
   }



   /*******************************************************************************
    ** Getter for errors
    **
    *******************************************************************************/
   public List<Exception> getErrors()
   {
      return errors;
   }



   /*******************************************************************************
    ** Setter for errors
    **
    *******************************************************************************/
   public void setErrors(List<Exception> errors)
   {
      this.errors = errors;
   }
}
