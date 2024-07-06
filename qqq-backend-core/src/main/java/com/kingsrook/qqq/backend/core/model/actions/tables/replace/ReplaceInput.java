/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.actions.tables.replace;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 **
 *******************************************************************************/
public class ReplaceInput extends AbstractTableActionInput
{
   private QBackendTransaction transaction;
   private UniqueKey           key;
   private List<QRecord>       records;
   private QQueryFilter        filter;
   private boolean performDeletes                 = true;
   private boolean allowNullKeyValuesToEqual      = false;
   private boolean setPrimaryKeyInInsertedRecords = false;

   private boolean omitDmlAudit = false;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ReplaceInput()
   {
   }



   /*******************************************************************************
    ** Getter for transaction
    *******************************************************************************/
   public QBackendTransaction getTransaction()
   {
      return (this.transaction);
   }



   /*******************************************************************************
    ** Setter for transaction
    *******************************************************************************/
   public void setTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    ** Fluent setter for transaction
    *******************************************************************************/
   public ReplaceInput withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
      return (this);
   }



   /*******************************************************************************
    ** Getter for records
    *******************************************************************************/
   public List<QRecord> getRecords()
   {
      return (this.records);
   }



   /*******************************************************************************
    ** Setter for records
    *******************************************************************************/
   public void setRecords(List<QRecord> records)
   {
      this.records = records;
   }



   /*******************************************************************************
    ** Fluent setter for records
    *******************************************************************************/
   public ReplaceInput withRecords(List<QRecord> records)
   {
      this.records = records;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filter
    *******************************************************************************/
   public QQueryFilter getFilter()
   {
      return (this.filter);
   }



   /*******************************************************************************
    ** Setter for filter
    *******************************************************************************/
   public void setFilter(QQueryFilter filter)
   {
      this.filter = filter;
   }



   /*******************************************************************************
    ** Fluent setter for filter
    *******************************************************************************/
   public ReplaceInput withFilter(QQueryFilter filter)
   {
      this.filter = filter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for key
    *******************************************************************************/
   public UniqueKey getKey()
   {
      return (this.key);
   }



   /*******************************************************************************
    ** Setter for key
    *******************************************************************************/
   public void setKey(UniqueKey key)
   {
      this.key = key;
   }



   /*******************************************************************************
    ** Fluent setter for key
    *******************************************************************************/
   public ReplaceInput withKey(UniqueKey key)
   {
      this.key = key;
      return (this);
   }



   /*******************************************************************************
    ** Getter for omitDmlAudit
    *******************************************************************************/
   public boolean getOmitDmlAudit()
   {
      return (this.omitDmlAudit);
   }



   /*******************************************************************************
    ** Setter for omitDmlAudit
    *******************************************************************************/
   public void setOmitDmlAudit(boolean omitDmlAudit)
   {
      this.omitDmlAudit = omitDmlAudit;
   }



   /*******************************************************************************
    ** Fluent setter for omitDmlAudit
    *******************************************************************************/
   public ReplaceInput withOmitDmlAudit(boolean omitDmlAudit)
   {
      this.omitDmlAudit = omitDmlAudit;
      return (this);
   }



   /*******************************************************************************
    ** Getter for performDeletes
    *******************************************************************************/
   public boolean getPerformDeletes()
   {
      return (this.performDeletes);
   }



   /*******************************************************************************
    ** Setter for performDeletes
    *******************************************************************************/
   public void setPerformDeletes(boolean performDeletes)
   {
      this.performDeletes = performDeletes;
   }



   /*******************************************************************************
    ** Fluent setter for performDeletes
    *******************************************************************************/
   public ReplaceInput withPerformDeletes(boolean performDeletes)
   {
      this.performDeletes = performDeletes;
      return (this);
   }



   /*******************************************************************************
    ** Getter for allowNullKeyValuesToEqual
    *******************************************************************************/
   public boolean getAllowNullKeyValuesToEqual()
   {
      return (this.allowNullKeyValuesToEqual);
   }



   /*******************************************************************************
    ** Setter for allowNullKeyValuesToEqual
    *******************************************************************************/
   public void setAllowNullKeyValuesToEqual(boolean allowNullKeyValuesToEqual)
   {
      this.allowNullKeyValuesToEqual = allowNullKeyValuesToEqual;
   }



   /*******************************************************************************
    ** Fluent setter for allowNullKeyValuesToEqual
    *******************************************************************************/
   public ReplaceInput withAllowNullKeyValuesToEqual(boolean allowNullKeyValuesToEqual)
   {
      this.allowNullKeyValuesToEqual = allowNullKeyValuesToEqual;
      return (this);
   }



   /*******************************************************************************
    ** Getter for setPrimaryKeyInInsertedRecords
    *******************************************************************************/
   public boolean getSetPrimaryKeyInInsertedRecords()
   {
      return (this.setPrimaryKeyInInsertedRecords);
   }



   /*******************************************************************************
    ** Setter for setPrimaryKeyInInsertedRecords
    *******************************************************************************/
   public void setSetPrimaryKeyInInsertedRecords(boolean setPrimaryKeyInInsertedRecords)
   {
      this.setPrimaryKeyInInsertedRecords = setPrimaryKeyInInsertedRecords;
   }



   /*******************************************************************************
    ** Fluent setter for setPrimaryKeyInInsertedRecords
    *******************************************************************************/
   public ReplaceInput withSetPrimaryKeyInInsertedRecords(boolean setPrimaryKeyInInsertedRecords)
   {
      this.setPrimaryKeyInInsertedRecords = setPrimaryKeyInInsertedRecords;
      return (this);
   }

}
