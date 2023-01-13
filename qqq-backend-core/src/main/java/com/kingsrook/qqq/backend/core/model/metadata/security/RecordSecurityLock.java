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

package com.kingsrook.qqq.backend.core.model.metadata.security;


import java.util.List;


/*******************************************************************************
 ** Define (for a table) a lock that applies to records in the table - e.g.,
 ** a key type, and a field that has values for that key.
 *
 *******************************************************************************/
public class RecordSecurityLock
{
   private String            securityKeyType;
   private String            fieldName;
   private List<String>      joinNameChain; // todo - add validation in validator!!
   private NullValueBehavior nullValueBehavior = NullValueBehavior.DENY;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public RecordSecurityLock()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum NullValueBehavior
   {
      ALLOW,
      DENY
   }



   /*******************************************************************************
    ** Getter for securityKeyType
    *******************************************************************************/
   public String getSecurityKeyType()
   {
      return (this.securityKeyType);
   }



   /*******************************************************************************
    ** Setter for securityKeyType
    *******************************************************************************/
   public void setSecurityKeyType(String securityKeyType)
   {
      this.securityKeyType = securityKeyType;
   }



   /*******************************************************************************
    ** Fluent setter for securityKeyType
    *******************************************************************************/
   public RecordSecurityLock withSecurityKeyType(String securityKeyType)
   {
      this.securityKeyType = securityKeyType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fieldName
    *******************************************************************************/
   public String getFieldName()
   {
      return (this.fieldName);
   }



   /*******************************************************************************
    ** Setter for fieldName
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fieldName
    *******************************************************************************/
   public RecordSecurityLock withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for nullValueBehavior
    *******************************************************************************/
   public NullValueBehavior getNullValueBehavior()
   {
      return (this.nullValueBehavior);
   }



   /*******************************************************************************
    ** Setter for nullValueBehavior
    *******************************************************************************/
   public void setNullValueBehavior(NullValueBehavior nullValueBehavior)
   {
      this.nullValueBehavior = nullValueBehavior;
   }



   /*******************************************************************************
    ** Fluent setter for nullValueBehavior
    *******************************************************************************/
   public RecordSecurityLock withNullValueBehavior(NullValueBehavior nullValueBehavior)
   {
      this.nullValueBehavior = nullValueBehavior;
      return (this);
   }



   /*******************************************************************************
    ** Getter for joinNameChain
    *******************************************************************************/
   public List<String> getJoinNameChain()
   {
      return (this.joinNameChain);
   }



   /*******************************************************************************
    ** Setter for joinNameChain
    *******************************************************************************/
   public void setJoinNameChain(List<String> joinNameChain)
   {
      this.joinNameChain = joinNameChain;
   }



   /*******************************************************************************
    ** Fluent setter for joinNameChain
    *******************************************************************************/
   public RecordSecurityLock withJoinNameChain(List<String> joinNameChain)
   {
      this.joinNameChain = joinNameChain;
      return (this);
   }

}
