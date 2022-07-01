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

package com.kingsrook.qqq.backend.module.rdbms.model.metadata;


import com.kingsrook.qqq.backend.core.model.metadata.QTableBackendDetails;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendModule;


/*******************************************************************************
 ** Extension of QTableBackendDetails, with details specific to an RDBMS table.
 *******************************************************************************/
public class RDBMSTableBackendDetails extends QTableBackendDetails
{
   private String tableName;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public RDBMSTableBackendDetails()
   {
      super();
      setBackendType(RDBMSBackendModule.class);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent Setter for tableName
    **
    *******************************************************************************/
   public RDBMSTableBackendDetails withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }

}
