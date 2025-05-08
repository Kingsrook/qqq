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

package com.kingsrook.qqq.languages.javascript;

// Javax imports removed for GraalVM migration


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.scripts.QCodeExecutor;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.QCodeExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QCodeException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang.NotImplementedException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;


/*******************************************************************************
 **
 *******************************************************************************/
public class QJavaScriptExecutor implements QCodeExecutor
{
   private static final QLogger LOG = QLogger.getLogger(QJavaScriptExecutor.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Serializable execute(QCodeReference codeReference, Map<String, Serializable> inputContext, QCodeExecutionLoggerInterface executionLogger) throws QCodeException
   {
      String       code   = getCode(codeReference);
      Serializable output = runInline(code, inputContext, executionLogger);
      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Object convertObjectToJava(Object object) throws QCodeException
   {
      try
      {
         if(object == null || object instanceof String || object instanceof Boolean || object instanceof Integer || object instanceof Long || object instanceof BigDecimal)
         {
            return (object);
         }
         else if(object instanceof Float f)
         {
            return (new BigDecimal(f));
         }
         else if(object instanceof Double d)
         {
            return (new BigDecimal(d));
         }
         else if(object instanceof Value val)
         {
            try
            {
               //////////////////////////////////////////////////
               // treat JavaScript null/undefined as Java null //
               //////////////////////////////////////////////////
               if(val.isNull() || val.isHostObject() && val.asHostObject() == null)
               {
                  return null;
               }
               ////////////////
               // primitives //
               ////////////////
               if(val.isString())
               {
                  return val.asString();
               }
               if(val.isBoolean())
               {
                  return val.asBoolean();
               }
               if(val.isNumber())
               {
                  //////////////////////////////////////////
                  // preserve integer types when possible //
                  //////////////////////////////////////////
                  if(val.fitsInInt())
                  {
                     return val.asInt();
                  }
                  else if(val.fitsInLong())
                  {
                     return val.asLong();
                  }
                  else
                  {
                     return new BigDecimal(val.asDouble());
                  }
               }
               //////////////////////////////////////////////
               // detect JS Date by existence of getTime() //
               //////////////////////////////////////////////
               if(val.hasMember("getTime") && val.canInvokeMember("getTime"))
               {
                  double millis = val.invokeMember("getTime").asDouble();
                  return Instant.ofEpochMilli((long) millis);
               }
               ////////////
               // arrays //
               ////////////
               if(val.hasArrayElements())
               {
                  List<Object> result = new ArrayList<>();
                  long         size   = val.getArraySize();
                  for(long i = 0; i < size; i++)
                  {
                     result.add(convertObjectToJava(val.getArrayElement(i)));
                  }
                  return result;
               }
               /////////////
               // objects //
               /////////////
               if(val.hasMembers())
               {
                  Map<String, Object> result = new HashMap<>();
                  for(String key : val.getMemberKeys())
                  {
                     result.put(key, convertObjectToJava(val.getMember(key)));
                  }
                  return result;
               }
            }
            catch(Exception e)
            {
               LOG.debug("Error converting GraalVM value", e);
            }
         }
         return QCodeExecutor.super.convertObjectToJava(object);
      }
      catch(Exception e)
      {
         throw (new QCodeException("Error converting java object", e));
      }
   }



   /*******************************************************************************
    ** Convert a native java object into one for the script's language/runtime.
    ** e.g., a java Instant to a Nashorn Date
    **
    *******************************************************************************/
   public Object convertJavaObject(Object object, Object requestedTypeHint) throws QCodeException
   {
      try
      {
         if("Date".equals(requestedTypeHint))
         {
            if(object instanceof Instant i)
            {
               long millis = (i.getEpochSecond() * 1000 + i.getLong(ChronoField.MILLI_OF_SECOND));
               Context context = Context.newBuilder("js")
                  .allowAllAccess(true)
                  .allowExperimentalOptions(true)
                  .option("js.ecmascript-version", "2022")
                  .build();
               Value jsDate = context.eval("js", "new Date(" + millis + ")");
               return jsDate.asHostObject();
            }
         }

         return (QCodeExecutor.super.convertJavaObject(object, requestedTypeHint));
      }
      catch(Exception e)
      {
         throw (new QCodeException("Error converting java object", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Serializable runInline(String code, Map<String, Serializable> inputContext, QCodeExecutionLoggerInterface executionLogger) throws QCodeException
   {
      Context context = Context.newBuilder("js")
         .allowAllAccess(true)
         .allowExperimentalOptions(true)
         .option("js.ecmascript-version", "2022")
         .build();
      // Populate GraalJS bindings from the inputContext
      Value bindingsScope = context.getBindings("js");
      for(Map.Entry<String, Serializable> entry : inputContext.entrySet())
      {
         bindingsScope.putMember(entry.getKey(), entry.getValue());
      }
      // Ensure logger is available
      if(!bindingsScope.hasMember("logger"))
      {
         bindingsScope.putMember("logger", executionLogger);
      }
      // wrap the user's code in an immediately-invoked function expression
      String codeToRun = """
         (function userDefinedFunction()
         {
            'use strict';
            %s
         
            var mainFunction = null;
            try
            {
               if(script && script.main && typeof script.main == "function")
               {
                  mainFunction = script.main;
               }
            }
            catch(e) { }
         
            if(mainFunction != null)
            {
               return (mainFunction());
            }
         })();
         """.formatted(code);

      Serializable output;
      try
      {
         Source source = Source.newBuilder("js", codeToRun, "batchName.js")
            .mimeType("application/javascript+module")
            .build();
         Value result = context.eval(source);
         output = (Serializable) result.asHostObject();
      }
      catch(Exception se)
      {
         // We no longer have ScriptException, so wrap as QCodeException
         throw new QCodeException("Error during JavaScript execution: " + se.getMessage(), se);
      }

      return (output);
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   // getQCodeExceptionFromScriptException is now unused (ScriptException/Nashorn removed)



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getCode(QCodeReference codeReference)
   {
      if(StringUtils.hasContent(codeReference.getInlineCode()))
      {
         return (codeReference.getInlineCode());
      }
      else
      {
         throw (new NotImplementedException("Only inline code is implemented at this time."));
      }
   }

}
