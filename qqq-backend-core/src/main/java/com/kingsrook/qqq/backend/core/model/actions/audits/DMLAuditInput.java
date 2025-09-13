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
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Input object for the DML audit action.
 *******************************************************************************/
public class DMLAuditInput extends AbstractActionInput implements Serializable
{
   private List<QRecord>            recordList;
   private List<QRecord>            oldRecordList;
   private AbstractTableActionInput tableActionInput;

   private QBackendTransaction transaction;

   private String auditContext = null;



   /*******************************************************************************
    ** Getter for recordList
    *******************************************************************************/
   public List<QRecord> getRecordList()
   {
      return (this.recordList);
   }



   /*******************************************************************************
    ** Setter for recordList
    *******************************************************************************/
   public void setRecordList(List<QRecord> recordList)
   {
      this.recordList = recordList;
   }



   /*******************************************************************************
    ** Fluent setter for recordList
    *******************************************************************************/
   public DMLAuditInput withRecordList(List<QRecord> recordList)
   {
      this.recordList = recordList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableActionInput
    *******************************************************************************/
   public AbstractTableActionInput getTableActionInput()
   {
      return (this.tableActionInput);
   }



   /*******************************************************************************
    ** Setter for tableActionInput
    *******************************************************************************/
   public void setTableActionInput(AbstractTableActionInput tableActionInput)
   {
      this.tableActionInput = tableActionInput;
   }



   /*******************************************************************************
    ** Fluent setter for tableActionInput
    *******************************************************************************/
   public DMLAuditInput withTableActionInput(AbstractTableActionInput tableActionInput)
   {
      this.tableActionInput = tableActionInput;
      return (this);
   }



   /*******************************************************************************
    ** Getter for oldRecordList
    *******************************************************************************/
   public List<QRecord> getOldRecordList()
   {
      return (this.oldRecordList);
   }



   /*******************************************************************************
    ** Setter for oldRecordList
    *******************************************************************************/
   public void setOldRecordList(List<QRecord> oldRecordList)
   {
      this.oldRecordList = oldRecordList;
   }



   /*******************************************************************************
    ** Fluent setter for oldRecordList
    *******************************************************************************/
   public DMLAuditInput withOldRecordList(List<QRecord> oldRecordList)
   {
      this.oldRecordList = oldRecordList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for auditContext
    *******************************************************************************/
   public String getAuditContext()
   {
      return (this.auditContext);
   }



   /*******************************************************************************
    ** Setter for auditContext
    *******************************************************************************/
   public void setAuditContext(String auditContext)
   {
      this.auditContext = auditContext;
   }



   /*******************************************************************************
    ** Fluent setter for auditContext
    *******************************************************************************/
   public DMLAuditInput withAuditContext(String auditContext)
   {
      this.auditContext = auditContext;
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
    * transaction that will be used for inserting the audits, where (presumably)
    * the DML against the record occurred as well
    *
    * @return this
    *******************************************************************************/
   public DMLAuditInput withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
      return (this);
   }


}
