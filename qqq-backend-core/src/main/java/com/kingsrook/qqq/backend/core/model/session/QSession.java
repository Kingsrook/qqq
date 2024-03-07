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

package com.kingsrook.qqq.backend.core.model.session;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MutableMap;


/*******************************************************************************
 **
 *******************************************************************************/
public class QSession implements Serializable
{
   private String idReference;
   private QUser  user;
   private String uuid;

   private Set<String>                     permissions;

   private Map<String, List<Serializable>> securityKeyValues;
   private Map<String, Serializable>       backendVariants;

   // implementation-specific custom values
   private Map<String, String> values;

   public static final String VALUE_KEY_USER_TIMEZONE                = "UserTimezone";
   public static final String VALUE_KEY_USER_TIMEZONE_OFFSET_MINUTES = "UserTimezoneOffsetMinutes";



   /*******************************************************************************
    ** Default constructor, puts a uuid in the session
    **
    *******************************************************************************/
   public QSession()
   {
      this.uuid = UUID.randomUUID().toString();
   }



   /*******************************************************************************
    ** Getter for idReference
    **
    *******************************************************************************/
   public String getIdReference()
   {
      return idReference;
   }



   /*******************************************************************************
    ** Setter for idReference
    **
    *******************************************************************************/
   public void setIdReference(String idReference)
   {
      this.idReference = idReference;
   }



   /*******************************************************************************
    ** Getter for user
    **
    *******************************************************************************/
   public QUser getUser()
   {
      return user;
   }



   /*******************************************************************************
    ** Setter for user
    **
    *******************************************************************************/
   public void setUser(QUser user)
   {
      this.user = user;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getValue(String key)
   {
      if(values == null)
      {
         return null;
      }
      return values.get(key);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setValue(String key, String value)
   {
      if(values == null)
      {
         values = new HashMap<>();
      }
      values.put(key, value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void removeValue(String key)
   {
      if(values != null)
      {
         values.remove(key);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QSession withValue(String key, String value)
   {
      if(values == null)
      {
         values = new HashMap<>();
      }
      values.put(key, value);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, String> getValues()
   {
      return values;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setValues(Map<String, String> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Getter for uuid
    **
    *******************************************************************************/
   public String getUuid()
   {
      return uuid;
   }



   /*******************************************************************************
    ** Setter for uuid
    **
    *******************************************************************************/
   public void setUuid(String uuid)
   {
      this.uuid = uuid;
   }



   /*******************************************************************************
    ** Getter for securityKeyValues
    *******************************************************************************/
   public Map<String, List<Serializable>> getSecurityKeyValues()
   {
      return (this.securityKeyValues);
   }



   /*******************************************************************************
    ** Getter for securityKeyValues - the list under a given key - never null.
    *******************************************************************************/
   public List<Serializable> getSecurityKeyValues(String keyName)
   {
      if(securityKeyValues == null)
      {
         return (new ArrayList<>());
      }

      return (Objects.requireNonNullElseGet(securityKeyValues.get(keyName), ArrayList::new));
   }



   /*******************************************************************************
    ** Getter for securityKeyValues - the list under a given key - as the expected tye - never null.
    *******************************************************************************/
   public List<Serializable> getSecurityKeyValues(String keyName, QFieldType type)
   {
      if(securityKeyValues == null)
      {
         return (new ArrayList<>());
      }

      List<Serializable> rawValues = securityKeyValues.get(keyName);
      if(rawValues == null)
      {
         return (new ArrayList<>());
      }

      List<Serializable> valuesAsType = new ArrayList<>();
      for(Serializable rawValue : rawValues)
      {
         valuesAsType.add(ValueUtils.getValueAsFieldType(type, rawValue));
      }
      return (valuesAsType);
   }



   /*******************************************************************************
    ** Test if this session has a given value for a given key
    *******************************************************************************/
   public boolean hasSecurityKeyValue(String keyName, Serializable value)
   {
      if(securityKeyValues == null)
      {
         return (false);
      }

      if(!securityKeyValues.containsKey(keyName))
      {
         return (false);
      }

      List<Serializable> values = securityKeyValues.get(keyName);
      return (values != null && values.contains(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean hasSecurityKeyValue(String keyName, Serializable value, QFieldType fieldType)
   {
      if(securityKeyValues == null)
      {
         return (false);
      }

      if(!securityKeyValues.containsKey(keyName))
      {
         return (false);
      }

      List<Serializable> values      = securityKeyValues.get(keyName);
      Serializable       valueAsType = ValueUtils.getValueAsFieldType(fieldType, value);
      for(Serializable keyValue : values)
      {
         Serializable keyValueAsType = ValueUtils.getValueAsFieldType(fieldType, keyValue);
         if(keyValueAsType.equals(valueAsType))
         {
            return (true);
         }
      }

      return (false);
   }



   /*******************************************************************************
    ** Setter for securityKeyValues
    *******************************************************************************/
   public void setSecurityKeyValues(Map<String, List<Serializable>> securityKeyValues)
   {
      this.securityKeyValues = new MutableMap<>(securityKeyValues);
   }



   /*******************************************************************************
    ** Fluent setter for securityKeyValues - replaces the map.
    *******************************************************************************/
   public QSession withSecurityKeyValues(Map<String, List<Serializable>> securityKeyValues)
   {
      this.securityKeyValues = new MutableMap<>(securityKeyValues);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for securityKeyValues - add 1 value for 1 key.
    *******************************************************************************/
   public QSession withSecurityKeyValue(String keyName, Serializable value)
   {
      if(securityKeyValues == null)
      {
         securityKeyValues = new HashMap<>();
      }

      securityKeyValues.computeIfAbsent(keyName, (k) -> new ArrayList<>());

      try
      {
         securityKeyValues.get(keyName).add(value);
      }
      catch(UnsupportedOperationException uoe)
      {
         /////////////////////
         // grr, List.of... //
         /////////////////////
         securityKeyValues.put(keyName, new ArrayList<>(securityKeyValues.get(keyName)));
         securityKeyValues.get(keyName).add(value);
      }

      return (this);
   }



   /*******************************************************************************
    ** Clear the map of security key values in the session.
    *******************************************************************************/
   public void clearSecurityKeyValues()
   {
      if(securityKeyValues != null)
      {
         securityKeyValues.clear();
      }
   }



   /*******************************************************************************
    ** Setter for permissions
    **
    *******************************************************************************/
   public void setPermissions(Set<String> permissions)
   {
      this.permissions = permissions;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QSession withPermission(String permission)
   {
      if(this.permissions == null)
      {
         this.permissions = new HashSet<>();
      }
      this.permissions.add(permission);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QSession withPermissions(String... permissionNames)
   {
      if(this.permissions == null)
      {
         this.permissions = new HashSet<>();
      }

      Collections.addAll(this.permissions, permissionNames);

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QSession withPermissions(Collection<String> permissionNames)
   {
      if(this.permissions == null)
      {
         this.permissions = new HashSet<>();
      }

      this.permissions.addAll(permissionNames);

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean hasPermission(String permissionName)
   {
      return (permissions != null && permissions.contains(permissionName));
   }



   /*******************************************************************************
    ** Getter for permissions
    *******************************************************************************/
   public Set<String> getPermissions()
   {
      return (this.permissions);
   }



   /*******************************************************************************
    ** Fluent setter for user
    *******************************************************************************/
   public QSession withUser(QUser user)
   {
      this.user = user;
      return (this);
   }



   /*******************************************************************************
    ** Getter for backendVariants
    *******************************************************************************/
   public Map<String, Serializable> getBackendVariants()
   {
      return (this.backendVariants);
   }



   /*******************************************************************************
    ** Setter for backendVariants
    *******************************************************************************/
   public void setBackendVariants(Map<String, Serializable> backendVariants)
   {
      this.backendVariants = backendVariants;
   }



   /*******************************************************************************
    ** Fluent setter for backendVariants
    *******************************************************************************/
   public QSession withBackendVariants(Map<String, Serializable> backendVariants)
   {
      this.backendVariants = backendVariants;
      return (this);
   }

}
