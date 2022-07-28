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

package com.kingsrook.qqq.backend.core.model.actions.tables.delete;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 ** Input for a Delete action.
 **
 *******************************************************************************/
public class DeleteInput extends AbstractTableActionInput
{
   private List<Serializable> primaryKeys;
   private QQueryFilter queryFilter;



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteInput()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteInput(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    ** Getter for ids
    **
    *******************************************************************************/
   public List<Serializable> getPrimaryKeys()
   {
      return primaryKeys;
   }



   /*******************************************************************************
    ** Setter for ids
    **
    *******************************************************************************/
   public void setPrimaryKeys(List<Serializable> primaryKeys)
   {
      this.primaryKeys = primaryKeys;
   }



   /*******************************************************************************
    ** Setter for ids
    **
    *******************************************************************************/
   public DeleteInput withPrimaryKeys(List<Serializable> primaryKeys)
   {
      this.primaryKeys = primaryKeys;
      return (this);
   }


   /*******************************************************************************
    ** Getter for queryFilter
    **
    *******************************************************************************/
   public QQueryFilter getQueryFilter()
   {
      return queryFilter;
   }



   /*******************************************************************************
    ** Setter for queryFilter
    **
    *******************************************************************************/
   public void setQueryFilter(QQueryFilter queryFilter)
   {
      this.queryFilter = queryFilter;
   }


   /*******************************************************************************
    ** Fluent setter for queryFilter
    **
    *******************************************************************************/
   public DeleteInput withQueryFilter(QQueryFilter queryFilter)
   {
      this.queryFilter = queryFilter;
      return this;
   }

}