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

package com.kingsrook.qqq.backend.core.model.actions.tables.insert;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.InputSource;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Input data for the Insert action
 **
 *******************************************************************************/
public class InsertInput extends AbstractTableActionInput
{
   private QBackendTransaction transaction;
   private List<QRecord>       records;
   private InputSource         inputSource = QInputSource.SYSTEM;

   private boolean skipUniqueKeyCheck = false;

   private boolean omitDmlAudit = false;
   private String  auditContext = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertInput()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public InsertInput(String tableName)
   {
      setTableName(tableName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertInput withTableName(String tableName)
   {
      super.withTableName(tableName);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertInput withRecord(QRecord record)
   {
      if(records == null)
      {
         records = new ArrayList<>();
      }

      records.add(record);

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertInput withRecordEntity(QRecordEntity recordEntity)
   {
      return (withRecord(recordEntity.toQRecord()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertInput withRecordEntities(List<QRecordEntity> recordEntityList)
   {
      for(QRecordEntity recordEntity : CollectionUtils.nonNullList(recordEntityList))
      {
         withRecordEntity(recordEntity);
      }

      return (this);
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
   public InsertInput withTransaction(QBackendTransaction transaction)
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
    ** Getter for skipUniqueKeyCheck
    **
    *******************************************************************************/
   public boolean getSkipUniqueKeyCheck()
   {
      return skipUniqueKeyCheck;
   }



   /*******************************************************************************
    ** Setter for skipUniqueKeyCheck
    **
    *******************************************************************************/
   public void setSkipUniqueKeyCheck(boolean skipUniqueKeyCheck)
   {
      this.skipUniqueKeyCheck = skipUniqueKeyCheck;
   }



   /*******************************************************************************
    ** Fluent setter for skipUniqueKeyCheck
    **
    *******************************************************************************/
   public InsertInput withSkipUniqueKeyCheck(boolean skipUniqueKeyCheck)
   {
      this.skipUniqueKeyCheck = skipUniqueKeyCheck;
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
   public InsertInput withOmitDmlAudit(boolean omitDmlAudit)
   {
      this.omitDmlAudit = omitDmlAudit;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for records
    *******************************************************************************/
   public InsertInput withRecords(List<QRecord> records)
   {
      this.records = records;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputSource
    *******************************************************************************/
   public InputSource getInputSource()
   {
      return (this.inputSource);
   }



   /*******************************************************************************
    ** Setter for inputSource
    *******************************************************************************/
   public void setInputSource(InputSource inputSource)
   {
      this.inputSource = inputSource;
   }



   /*******************************************************************************
    ** Fluent setter for inputSource
    *******************************************************************************/
   public InsertInput withInputSource(InputSource inputSource)
   {
      this.inputSource = inputSource;
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
   public InsertInput withAuditContext(String auditContext)
   {
      this.auditContext = auditContext;
      return (this);
   }

}
