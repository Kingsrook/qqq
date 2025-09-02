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

package com.kingsrook.qqq.backend.module.mongodb.model.metadata;


import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import com.kingsrook.qqq.backend.module.mongodb.MongoDBBackendModule;


/*******************************************************************************
 ** Extension of QTableBackendDetails, with details specific to a MongoDB table.
 *******************************************************************************/
public class MongoDBTableBackendDetails extends QTableBackendDetails
{
   private String tableName;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public MongoDBTableBackendDetails()
   {
      super();
      setBackendType(MongoDBBackendModule.class);
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
   public MongoDBTableBackendDetails withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /***************************************************************************
    * finish the cloning operation started in the base class. copy all state
    * from the subclass into the input clone (which can be safely casted to
    * the subclass's type, as it was obtained by super.clone())
    ***************************************************************************/
   @Override
   protected QTableBackendDetails finishClone(QTableBackendDetails abstractClone)
   {
      MongoDBTableBackendDetails clone = (MongoDBTableBackendDetails) abstractClone;
      clone.tableName = tableName;
      return (clone);
   }

}
