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

package com.kingsrook.qqq.backend.core.model.actions.tables.update;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Input data handler for the update action
 **
 *******************************************************************************/
public class UpdateInput extends AbstractTableActionInput
{
   private QBackendTransaction transaction;
   private List<QRecord>       records;

   ////////////////////////////////////////////////////////////////////////////////////////////
   // allow a caller to specify that they KNOW this optimization (e.g., in SQL) can be made. //
   // If you set this to true, but it isn't, then you may not get an accurate update.        //
   // If you set this to false, but it isn't, then you may not get the best performance.     //
   // Just leave it null if you don't know what you're dong.                                 //
   ////////////////////////////////////////////////////////////////////////////////////////////
   private Boolean areAllValuesBeingUpdatedTheSame = null;

   private boolean omitDmlAudit = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateInput()
   {
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
   public UpdateInput withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
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
    ** Getter for areAllValuesBeingUpdatedTheSame
    **
    *******************************************************************************/
   public Boolean getAreAllValuesBeingUpdatedTheSame()
   {
      return areAllValuesBeingUpdatedTheSame;
   }



   /*******************************************************************************
    ** Setter for areAllValuesBeingUpdatedTheSame
    **
    *******************************************************************************/
   public void setAreAllValuesBeingUpdatedTheSame(Boolean areAllValuesBeingUpdatedTheSame)
   {
      this.areAllValuesBeingUpdatedTheSame = areAllValuesBeingUpdatedTheSame;
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
   public UpdateInput withOmitDmlAudit(boolean omitDmlAudit)
   {
      this.omitDmlAudit = omitDmlAudit;
      return (this);
   }

}
