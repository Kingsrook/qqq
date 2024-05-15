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


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Define, for a field, a lock that controls if users can or cannot see the field.
 **
 ** The lock has a defaultBehavior, which is how the field should be treated, well,
 ** by default.
 ** The lock also references a securityKeyType; whose values, when looked up in
 ** the lock's keyValueBehaviors map, change the default behavior.
 **
 ** For example, consider a lock with a keyType of 'internalOrExternalUser' (with
 ** possible values of 'internal' and 'external'), a defaultBehavior of DENY,
 ** and a keyValueBehaviors map containing internal => ALLOW.  If a session has
 ** no security key of the internalOrExternalUser type, or a key with the value of
 ** 'external', then the lock's behavior will be the default (DENY).  However,
 ** a key value of 'internal' would trigger the behavior specified for that key
 ** (ALLOW).
 *******************************************************************************/
public class FieldSecurityLock
{
   private static final QLogger LOG = QLogger.getLogger(FieldSecurityLock.class);

   private String   securityKeyType;
   private Behavior defaultBehavior = Behavior.DENY;

   private Map<Serializable, Behavior> keyValueBehaviors;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FieldSecurityLock()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public enum Behavior
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
   public FieldSecurityLock withSecurityKeyType(String securityKeyType)
   {
      this.securityKeyType = securityKeyType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultBehavior
    *******************************************************************************/
   public Behavior getDefaultBehavior()
   {
      return (this.defaultBehavior);
   }



   /*******************************************************************************
    ** Setter for defaultBehavior
    *******************************************************************************/
   public void setDefaultBehavior(Behavior defaultBehavior)
   {
      this.defaultBehavior = defaultBehavior;
   }



   /*******************************************************************************
    ** Fluent setter for defaultBehavior
    *******************************************************************************/
   public FieldSecurityLock withDefaultBehavior(Behavior defaultBehavior)
   {
      this.defaultBehavior = defaultBehavior;
      return (this);
   }



   /*******************************************************************************
    ** Getter for keyValueBehaviors
    *******************************************************************************/
   public Map<Serializable, Behavior> getKeyValueBehaviors()
   {
      return (this.keyValueBehaviors);
   }



   /*******************************************************************************
    ** Setter for keyValueBehaviors
    *******************************************************************************/
   public void setKeyValueBehaviors(Map<Serializable, Behavior> keyValueBehaviors)
   {
      this.keyValueBehaviors = keyValueBehaviors;
   }



   /*******************************************************************************
    ** Fluent setter for keyValueBehaviors
    *******************************************************************************/
   public FieldSecurityLock withKeyValueBehaviors(Map<Serializable, Behavior> keyValueBehaviors)
   {
      this.keyValueBehaviors = keyValueBehaviors;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for a single keyValueBehavior
    *******************************************************************************/
   public FieldSecurityLock withKeyValueBehavior(Serializable keyValue, Behavior behavior)
   {
      if(this.keyValueBehaviors == null)
      {
         this.keyValueBehaviors = new HashMap<>();
      }
      this.keyValueBehaviors.put(keyValue, behavior);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Behavior getBehaviorForSession(QSession session)
   {
      if(session != null && session.getSecurityKeyValues(this.securityKeyType) != null)
      {
         QSecurityKeyType securityKeyType = QContext.getQInstance().getSecurityKeyType(this.securityKeyType);

         for(Serializable securityKeyValue : session.getSecurityKeyValues(this.securityKeyType))
         {
            try
            {
               if(securityKeyType.getValueType() != null)
               {
                  securityKeyValue = ValueUtils.getValueAsFieldType(securityKeyType.getValueType(), securityKeyValue);
               }

               if(keyValueBehaviors.containsKey(securityKeyValue))
               {
                  return keyValueBehaviors.get(securityKeyValue);
               }
            }
            catch(Exception e)
            {
               LOG.warn("Error getting field behavior", e);
            }
         }
      }

      return getDefaultBehavior();
   }

}
