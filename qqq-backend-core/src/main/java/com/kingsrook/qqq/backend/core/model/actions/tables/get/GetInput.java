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

package com.kingsrook.qqq.backend.core.model.actions.tables.get;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;


/*******************************************************************************
 ** Input data for the Get action
 **
 *******************************************************************************/
public class GetInput extends AbstractTableActionInput
{
   private QBackendTransaction transaction;

   private Serializable              primaryKey;
   private Map<String, Serializable> uniqueKey;

   private boolean shouldTranslatePossibleValues = false;
   private boolean shouldGenerateDisplayValues   = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   public GetInput()
   {
   }



   /*******************************************************************************
    ** Getter for primaryKey
    **
    *******************************************************************************/
   public Serializable getPrimaryKey()
   {
      return primaryKey;
   }



   /*******************************************************************************
    ** Setter for primaryKey
    **
    *******************************************************************************/
   public void setPrimaryKey(Serializable primaryKey)
   {
      this.primaryKey = primaryKey;
   }



   /*******************************************************************************
    ** Fluent setter for primaryKey
    **
    *******************************************************************************/
   public GetInput withPrimaryKey(Serializable primaryKey)
   {
      this.primaryKey = primaryKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for uniqueKey
    **
    *******************************************************************************/
   public Map<String, Serializable> getUniqueKey()
   {
      return uniqueKey;
   }



   /*******************************************************************************
    ** Setter for uniqueKey
    **
    *******************************************************************************/
   public void setUniqueKey(Map<String, Serializable> uniqueKey)
   {
      this.uniqueKey = uniqueKey;
   }



   /*******************************************************************************
    ** Fluent setter for uniqueKey
    **
    *******************************************************************************/
   public GetInput withUniqueKey(Map<String, Serializable> uniqueKey)
   {
      this.uniqueKey = uniqueKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for shouldTranslatePossibleValues
    **
    *******************************************************************************/
   public boolean getShouldTranslatePossibleValues()
   {
      return shouldTranslatePossibleValues;
   }



   /*******************************************************************************
    ** Setter for shouldTranslatePossibleValues
    **
    *******************************************************************************/
   public void setShouldTranslatePossibleValues(boolean shouldTranslatePossibleValues)
   {
      this.shouldTranslatePossibleValues = shouldTranslatePossibleValues;
   }



   /*******************************************************************************
    ** Getter for shouldGenerateDisplayValues
    **
    *******************************************************************************/
   public boolean getShouldGenerateDisplayValues()
   {
      return shouldGenerateDisplayValues;
   }



   /*******************************************************************************
    ** Setter for shouldGenerateDisplayValues
    **
    *******************************************************************************/
   public void setShouldGenerateDisplayValues(boolean shouldGenerateDisplayValues)
   {
      this.shouldGenerateDisplayValues = shouldGenerateDisplayValues;
   }



   /*******************************************************************************
    ** Getter for transaction
    **
    *******************************************************************************/
   public QBackendTransaction getTransaction()
   {
      return transaction;
   }



   /*******************************************************************************
    ** Setter for transaction
    **
    *******************************************************************************/
   public void setTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    ** Fluent setter for transaction
    **
    *******************************************************************************/
   public GetInput withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
      return (this);
   }

}
