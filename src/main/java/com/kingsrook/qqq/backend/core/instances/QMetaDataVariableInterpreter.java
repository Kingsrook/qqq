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

package com.kingsrook.qqq.backend.core.instances;


import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** To avoid having secrets (passwords, access keys, etc) committed into meta data
 ** files, as well as to just let some meta data not be hard-coded, this class is
 ** used by the Enricher to "promote" values, such as ${env.ACCESS_KEY}
 ** to be read from the environment (or other secret providers (to be implemented)).
 **
 ** Supported syntax / value sources are:
 ** ${env.VAR} = system environment variables, e.g., export VAR=val
 ** ${prop.VAR} = properties, e.g., -DVAR=val
 ** ${literal.VAR} = get back a literal "VAR" (in case VAR matches some of the other supported syntax in here)
 *******************************************************************************/
public class QMetaDataVariableInterpreter
{
   private static final Logger LOG = LogManager.getLogger(QMetaDataVariableInterpreter.class);

   private Map<String, String> customEnvironment;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void interpretObject(Object o) throws QException
   {
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // get the InterpretableFields from the object's class - exiting if the annotation isn't present //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      InterpretableFields interpretableFields = o.getClass().getAnnotation(InterpretableFields.class);
      if(interpretableFields == null)
      {
         return;
      }

      //////////////////////////////////////////////////////////
      // iterate over interpretable fields, interpreting each //
      //////////////////////////////////////////////////////////
      for(String fieldName : interpretableFields.fieldNames())
      {
         try
         {
            ///////////////////////////////////////////////////////////////////////////////////////
            // get the getter & setter methods for the field (getMethod will throw if not found) //
            // enforce Strings-only at this time.                                                //
            ///////////////////////////////////////////////////////////////////////////////////////
            String   fieldNameUcFirst = fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
            Method   getter           = o.getClass().getMethod("get" + fieldNameUcFirst);
            Class<?> fieldType        = getter.getReturnType();
            if(!fieldType.equals(String.class))
            {
               throw new QException("Interpretable field: " + fieldName + " on class " + o.getClass() + " is not a String (which is required at this time)");
            }
            Method setter = o.getClass().getMethod("set" + fieldNameUcFirst, fieldType);

            //////////////////////////////////////////////////////////////////////////////////////////////
            // get the value - if it's null, move on, else, interpret it, and put it back in the object //
            //////////////////////////////////////////////////////////////////////////////////////////////
            Object value  = getter.invoke(o);
            if(value == null)
            {
               continue;
            }
            String interpreted = interpret((String) value);
            setter.invoke(o, interpreted);
         }
         catch(Exception e)
         {
            throw (new QException("Error interpreting variables in object " + o, e));
         }
      }
   }



   /*******************************************************************************
    ** Interpret a value string, which may be a variable, into its run-time value.
    **
    ** If input is null, output is null.
    ** If input looks like ${env.X}, then the return value is the value of the env variable 'X'
    ** If input looks like ${prop.X}, then the return value is the value of the system property 'X'
    ** If input looks like ${literal.X}, then the return value is the literal 'X'
    **  - used if you really want to get back the literal value, ${env.X}, for example.
    ** Else the output is the input.
    *******************************************************************************/
   public String interpret(String value)
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

      if(value.startsWith("${prop.") && value.endsWith("}"))
      {
         String propertyName  = value.substring(7).replaceFirst("}$", "");
         String propertyValue = System.getProperty(propertyName);
         return (propertyValue);
      }

      if(value.startsWith("${literal.") && value.endsWith("}"))
      {
         String literalValue = value.substring(10).replaceFirst("}$", "");
         return (literalValue);
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
