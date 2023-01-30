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
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;


/*******************************************************************************
 **
 *******************************************************************************/
public class AuditInput extends AbstractActionInput
{
   private String        auditTableName;
   private String        auditUserName;
   private Instant       timestamp;
   private String        message;
   private List<Integer> recordIdList;

   private Map<String, Serializable> securityKeyValues;



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
   public AuditInput withAuditTableName(String auditTableName)
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
   public AuditInput withAuditUserName(String auditUserName)
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
   public AuditInput withTimestamp(Instant timestamp)
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
   public AuditInput withMessage(String message)
   {
      this.message = message;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordIdList
    *******************************************************************************/
   public List<Integer> getRecordIdList()
   {
      return (this.recordIdList);
   }



   /*******************************************************************************
    ** Setter for recordIdList
    *******************************************************************************/
   public void setRecordIdList(List<Integer> recordIdList)
   {
      this.recordIdList = recordIdList;
   }



   /*******************************************************************************
    ** Fluent setter for recordIdList
    *******************************************************************************/
   public AuditInput withRecordIdList(List<Integer> recordIdList)
   {
      this.recordIdList = recordIdList;
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
   public AuditInput withSecurityKeyValues(Map<String, Serializable> securityKeyValues)
   {
      this.securityKeyValues = securityKeyValues;
      return (this);
   }

}
