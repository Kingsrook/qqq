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

package com.kingsrook.qqq.backend.core.model.actions.audits;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;


/*******************************************************************************
 ** Input object for the audit action - an object which contains a list of "single"
 ** audit inputs - e.g., the data needed to insert 1 audit.
 *******************************************************************************/
public class AuditInput extends AbstractActionInput implements Serializable
{
   private List<AuditSingleInput> auditSingleInputList = new ArrayList<>();

   private QBackendTransaction transaction;



   /*******************************************************************************
    ** Getter for auditSingleInputList
    *******************************************************************************/
   public List<AuditSingleInput> getAuditSingleInputList()
   {
      return (this.auditSingleInputList);
   }



   /*******************************************************************************
    ** Setter for auditSingleInputList
    *******************************************************************************/
   public void setAuditSingleInputList(List<AuditSingleInput> auditSingleInputList)
   {
      this.auditSingleInputList = auditSingleInputList;
   }



   /*******************************************************************************
    ** Fluent setter for auditSingleInputList
    *******************************************************************************/
   public AuditInput withAuditSingleInputList(List<AuditSingleInput> auditSingleInputList)
   {
      this.auditSingleInputList = auditSingleInputList;
      return (this);
   }



   /*******************************************************************************
    ** Add a single auditSingleInput
    *******************************************************************************/
   public void addAuditSingleInput(AuditSingleInput auditSingleInput)
   {
      if(this.auditSingleInputList == null)
      {
         this.auditSingleInputList = new ArrayList<>();
      }
      this.auditSingleInputList.add(auditSingleInput);
   }



   /*******************************************************************************
    ** Fluent setter to add a single auditSingleInput
    *******************************************************************************/
   public AuditInput withAuditSingleInput(AuditSingleInput auditSingleInput)
   {
      addAuditSingleInput(auditSingleInput);
      return (this);
   }


   /*******************************************************************************
    * Getter for transaction
    * @see #withTransaction(QBackendTransaction)
    *******************************************************************************/
   public QBackendTransaction getTransaction()
   {
      return (this.transaction);
   }



   /*******************************************************************************
    * Setter for transaction
    * @see #withTransaction(QBackendTransaction)
    *******************************************************************************/
   public void setTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    * Fluent setter for transaction
    *
    * @param transaction
    * transaction upon which the audits will be inserted.
    *
    * @return this
    *******************************************************************************/
   public AuditInput withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
      return (this);
   }


}
