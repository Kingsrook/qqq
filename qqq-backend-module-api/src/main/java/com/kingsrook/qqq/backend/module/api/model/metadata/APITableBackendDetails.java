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

package com.kingsrook.qqq.backend.module.api.model.metadata;


import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import com.kingsrook.qqq.backend.module.api.APIBackendModule;


/*******************************************************************************
 ** Extension of QTableBackendDetails, with details specific to an API table.
 *******************************************************************************/
public class APITableBackendDetails extends QTableBackendDetails
{
   private String tablePath;
   private String tableWrapperObjectName;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public APITableBackendDetails()
   {
      super();
      setBackendType(APIBackendModule.class);
   }



   /*******************************************************************************
    ** Getter for tablePath
    **
    *******************************************************************************/
   public String getTablePath()
   {
      return tablePath;
   }



   /*******************************************************************************
    ** Setter for tablePath
    **
    *******************************************************************************/
   public void setTablePath(String tablePath)
   {
      this.tablePath = tablePath;
   }



   /*******************************************************************************
    ** Fluent Setter for tablePath
    **
    *******************************************************************************/
   public APITableBackendDetails withTablePath(String tablePath)
   {
      this.tablePath = tablePath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableWrapperObjectName
    **
    *******************************************************************************/
   public String getTableWrapperObjectName()
   {
      return tableWrapperObjectName;
   }



   /*******************************************************************************
    ** Setter for tableWrapperObjectName
    **
    *******************************************************************************/
   public void setTableWrapperObjectName(String tableWrapperObjectName)
   {
      this.tableWrapperObjectName = tableWrapperObjectName;
   }



   /*******************************************************************************
    ** Fluent setter for tableWrapperObjectName
    **
    *******************************************************************************/
   public APITableBackendDetails withTableWrapperObjectName(String tableWrapperObjectName)
   {
      this.tableWrapperObjectName = tableWrapperObjectName;
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
      APITableBackendDetails clone = (APITableBackendDetails) abstractClone;
      clone.tablePath = tablePath;
      clone.tableWrapperObjectName = tableWrapperObjectName;
      return (clone);
   }

}
