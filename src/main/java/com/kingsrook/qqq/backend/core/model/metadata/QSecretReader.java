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

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.Map;


/*******************************************************************************
 ** To avoid having secrets (passwords, access keys, etc) committed into meta data
 ** files, this class is used by the Enricher to "promote" values, such as ${env.ACCESS_KEY}
 ** to be read from the environment (or other secret providers (to be implemented)).
 *******************************************************************************/
public class QSecretReader
{
   private Map<String, String> customEnvironment;



   /*******************************************************************************
    ** Translate a secret.
    **
    ** If input is null, output is null.
    ** If input looks like ${env.X}, then the return value is the value of the env variable 'X'
    ** Else the output is the input.
    *******************************************************************************/
   public String readSecret(String value)
   {
      if(value == null)
      {
         return (null);
      }

      if(value.startsWith("${env.") && value.endsWith("}"))
      {
         String envVarName = value.substring(6).replaceFirst("}$", "");
         String envValue   = getEnvironment().get(envVarName);
         return (envValue);
      }

      return (value);
   }



   /*******************************************************************************
    ** Setter for customEnvironment - protected - meant to be called (at least at this
    ** time), only in unit test
    **
    *******************************************************************************/
   protected void setCustomEnvironment(Map<String, String> customEnvironment)
   {
      this.customEnvironment = customEnvironment;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<String, String> getEnvironment()
   {
      if(this.customEnvironment != null)
      {
         return (this.customEnvironment);
      }

      return System.getenv();
   }
}
