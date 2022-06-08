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

package com.kingsrook.qqq.backend.core.model.etl;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class QDataBatch
{
   private String identity; // e.g., a full path to a file
   private List<QRecord> records;



   /*******************************************************************************
    ** Getter for identity
    **
    *******************************************************************************/
   public String getIdentity()
   {
      return identity;
   }



   /*******************************************************************************
    ** Setter for identity
    **
    *******************************************************************************/
   public void setIdentity(String identity)
   {
      this.identity = identity;
   }



   /*******************************************************************************
    ** Fluent setter for identity
    **
    *******************************************************************************/
   public QDataBatch withIdentity(String identity)
   {
      this.identity = identity;
      return (this);
   }



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
    ** Setter for records
    **
    *******************************************************************************/
   public QDataBatch withRecords(List<QRecord> records)
   {
      this.records = records;
      return (this);
   }

}
