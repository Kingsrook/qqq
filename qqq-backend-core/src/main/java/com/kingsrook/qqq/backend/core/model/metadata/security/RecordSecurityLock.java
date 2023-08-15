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
 **
 ** Here's an example of how the joinNameChain should be set up:
 ** given a table:  orderLineItemExtrinsic (that's 2 away from order, where the security field is):
 ** - recordSecurityLock.fieldName = order.clientId
 ** - recordSecurityLock.joinNameChain = [orderJoinOrderLineItem, orderLineItemJoinOrderLineItemExtrinsic]
 ** that is - what's the chain that takes us FROM the security fieldName TO the table with the lock.
 **
 ** LockScope controls what the lock prevents users from doing without a valid key.
 ** - READ_AND_WRITE means that users cannot read or write records without a valid key.
 ** - WRITE means that users cannot write records without a valid key (but they can read them).
 *******************************************************************************/
public class RecordSecurityLock
{
   private String            securityKeyType;
   private String            fieldName;
   private List<String>      joinNameChain;
   private NullValueBehavior nullValueBehavior = NullValueBehavior.DENY;

   private LockScope lockScope = LockScope.READ_AND_WRITE;



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
      ALLOW_WRITE_ONLY, // not common - but see Audit, where you can do a thing that inserts them into a generic table, even though you can't later read them yourself...
      DENY
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum LockScope
   {
      READ_AND_WRITE,
      WRITE
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



   /*******************************************************************************
    ** Getter for lockScope
    *******************************************************************************/
   public LockScope getLockScope()
   {
      return (this.lockScope);
   }



   /*******************************************************************************
    ** Setter for lockScope
    *******************************************************************************/
   public void setLockScope(LockScope lockScope)
   {
      this.lockScope = lockScope;
   }



   /*******************************************************************************
    ** Fluent setter for lockScope
    *******************************************************************************/
   public RecordSecurityLock withLockScope(LockScope lockScope)
   {
      this.lockScope = lockScope;
      return (this);
   }

}
