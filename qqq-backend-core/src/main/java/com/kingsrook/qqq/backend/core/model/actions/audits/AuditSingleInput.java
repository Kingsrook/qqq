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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Input data to insert a single audit record (with optional child record)..
 *******************************************************************************/
public class AuditSingleInput
{
   private String  auditTableName;
   private String  auditUserName;
   private Instant timestamp;
   private String  message;
   private Integer recordId;

   private Map<String, Serializable> securityKeyValues;

   private List<String> details;



   /*******************************************************************************
    ** Getter for auditTableName
    *******************************************************************************/
   public String getAuditTableName()
   {
      return (this.auditTableName);
   }



   /*******************************************************************************
    ** Setter for auditTableName
    *******************************************************************************/
   public void setAuditTableName(String auditTableName)
   {
      this.auditTableName = auditTableName;
   }



   /*******************************************************************************
    ** Fluent setter for auditTableName
    *******************************************************************************/
   public AuditSingleInput withAuditTableName(String auditTableName)
   {
      this.auditTableName = auditTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for auditUserName
    *******************************************************************************/
   public String getAuditUserName()
   {
      return (this.auditUserName);
   }



   /*******************************************************************************
    ** Setter for auditUserName
    *******************************************************************************/
   public void setAuditUserName(String auditUserName)
   {
      this.auditUserName = auditUserName;
   }



   /*******************************************************************************
    ** Fluent setter for auditUserName
    *******************************************************************************/
   public AuditSingleInput withAuditUserName(String auditUserName)
   {
      this.auditUserName = auditUserName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for timestamp
    *******************************************************************************/
   public Instant getTimestamp()
   {
      return (this.timestamp);
   }



   /*******************************************************************************
    ** Setter for timestamp
    *******************************************************************************/
   public void setTimestamp(Instant timestamp)
   {
      this.timestamp = timestamp;
   }



   /*******************************************************************************
    ** Fluent setter for timestamp
    *******************************************************************************/
   public AuditSingleInput withTimestamp(Instant timestamp)
   {
      this.timestamp = timestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for message
    *******************************************************************************/
   public String getMessage()
   {
      return (this.message);
   }



   /*******************************************************************************
    ** Setter for message
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.message = message;
   }



   /*******************************************************************************
    ** Fluent setter for message
    *******************************************************************************/
   public AuditSingleInput withMessage(String message)
   {
      this.message = message;
      return (this);
   }



   /*******************************************************************************
    ** Getter for securityKeyValues
    *******************************************************************************/
   public Map<String, Serializable> getSecurityKeyValues()
   {
      return (this.securityKeyValues);
   }



   /*******************************************************************************
    ** Setter for securityKeyValues
    *******************************************************************************/
   public void setSecurityKeyValues(Map<String, Serializable> securityKeyValues)
   {
      this.securityKeyValues = securityKeyValues;
   }



   /*******************************************************************************
    ** Fluent setter for securityKeyValues
    *******************************************************************************/
   public AuditSingleInput withSecurityKeyValues(Map<String, Serializable> securityKeyValues)
   {
      this.securityKeyValues = securityKeyValues;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordId
    *******************************************************************************/
   public Integer getRecordId()
   {
      return (this.recordId);
   }



   /*******************************************************************************
    ** Setter for recordId
    *******************************************************************************/
   public void setRecordId(Integer recordId)
   {
      this.recordId = recordId;
   }



   /*******************************************************************************
    ** Fluent setter for recordId
    *******************************************************************************/
   public AuditSingleInput withRecordId(Integer recordId)
   {
      this.recordId = recordId;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AuditSingleInput forRecord(QTableMetaData table, QRecord record)
   {
      setRecordId(record.getValueInteger(table.getPrimaryKeyField())); // todo support non-integer
      setAuditTableName(table.getName());

      this.securityKeyValues = new HashMap<>();
      for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(table.getRecordSecurityLocks()))
      {
         this.securityKeyValues.put(recordSecurityLock.getFieldName(), record.getValueInteger(recordSecurityLock.getFieldName()));
      }

      return (this);
   }

   /*******************************************************************************
    ** Getter for details
    *******************************************************************************/
   public List<String> getDetails()
   {
      return (this.details);
   }



   /*******************************************************************************
    ** Setter for details
    *******************************************************************************/
   public void setDetails(List<String> details)
   {
      this.details = details;
   }



   /*******************************************************************************
    ** Fluent setter for details
    *******************************************************************************/
   public AuditSingleInput withDetails(List<String> details)
   {
      this.details = details;
      return (this);
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   public void addDetail(String detail)
   {
      if(this.details == null)
      {
         this.details = new ArrayList<>();
      }
      this.details.add(detail);
   }

}
