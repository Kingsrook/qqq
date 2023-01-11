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

package com.kingsrook.qqq.backend.core.model.actions;


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Base class for input for any qqq action that works against a table.
 **
 *******************************************************************************/
public class AbstractTableActionInput extends AbstractActionInput
{
   private String tableName;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendMetaData getBackend()
   {
      return (instance.getBackendForTable(getTableName()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData getTable()
   {
      return (instance.getTable(getTableName()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractTableActionInput()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractTableActionInput(QInstance instance)
   {
      super(instance);
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
    ** Fluent setter for tableName
    *******************************************************************************/
   public AbstractTableActionInput withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for session
    *******************************************************************************/
   @Override
   public AbstractTableActionInput withSession(QSession session)
   {
      super.withSession(session);
      return (this);
   }

}
