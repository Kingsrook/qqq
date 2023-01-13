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
import java.util.List;


/*******************************************************************************
 ** Define, for a field, a lock that controls if users can or cannot see the field.
 *******************************************************************************/
public class FieldSecurityLock
{
   private String             securityKeyType;
   private Behavior           defaultBehavior = Behavior.DENY;
   private List<Serializable> overrideValues;



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
    ** Getter for overrideValues
    *******************************************************************************/
   public List<Serializable> getOverrideValues()
   {
      return (this.overrideValues);
   }



   /*******************************************************************************
    ** Setter for overrideValues
    *******************************************************************************/
   public void setOverrideValues(List<Serializable> overrideValues)
   {
      this.overrideValues = overrideValues;
   }



   /*******************************************************************************
    ** Fluent setter for overrideValues
    *******************************************************************************/
   public FieldSecurityLock withOverrideValues(List<Serializable> overrideValues)
   {
      this.overrideValues = overrideValues;
      return (this);
   }


}
