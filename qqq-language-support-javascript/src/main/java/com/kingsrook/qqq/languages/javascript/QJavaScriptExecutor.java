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


import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.scripts.QCodeExecutor;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.QCodeExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QCodeException;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang.NotImplementedException;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.internal.runtime.ECMAException;
import org.openjdk.nashorn.internal.runtime.ParserException;


/*******************************************************************************
 **
 *******************************************************************************/
public class QJavaScriptExecutor implements QCodeExecutor
{

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
   private Serializable runInline(String code, Map<String, Serializable> inputContext, QCodeExecutionLoggerInterface executionLogger) throws QCodeException
   {
      new NashornScriptEngineFactory();
      ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

      //////////////////////////////////////////////
      // setup the javascript environment/context //
      //////////////////////////////////////////////
      Bindings bindings = engine.createBindings();
      bindings.putAll(inputContext);

      if(!bindings.containsKey("logger"))
      {
         bindings.put("logger", executionLogger);
      }

      ////////////////////////////////////////////////////////////////////////
      // wrap the user's code in an immediately-invoked function expression //
      // if the user's code (%s below) returns - then our IIFE is done.     //
      // if the user's code doesn't return, but instead created a 'script'  //
      // variable, with a 'main' function on it (e.g., from a compiled      //
      // type script file), then call main function and return its result.  //
      ////////////////////////////////////////////////////////////////////////
      String codeToRun = """
         (function userDefinedFunction()
         {
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
         output = (Serializable) engine.eval(codeToRun, bindings);
      }
      catch(ScriptException se)
      {
         QCodeException qCodeException = getQCodeExceptionFromScriptException(se);
         throw (qCodeException);
      }

      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QCodeException getQCodeExceptionFromScriptException(ScriptException se)
   {
      boolean isParserException     = ExceptionUtils.findClassInRootChain(se, ParserException.class) != null;
      boolean isUserThrownException = ExceptionUtils.findClassInRootChain(se, ECMAException.class) != null;

      String message      = se.getMessage();
      String errorContext = null;
      if(message != null)
      {
         message = message.replaceFirst(" in <eval>.*", "");
         message = message.replaceFirst("<eval>:\\d+:\\d+", "");

         if(message.contains("\n"))
         {
            String[] parts = message.split("\n", 2);
            message = parts[0];
            errorContext = parts[1];
         }
      }

      int actualScriptLineNumber = se.getLineNumber() - 2;

      String  prefix         = "Script Exception";
      boolean includeColumn  = true;
      boolean includeContext = false;
      if(isParserException)
      {
         prefix = "Script parser exception";
         includeContext = true;
      }
      else if(isUserThrownException)
      {
         prefix = "Script threw an exception";
         includeColumn = false;
      }

      QCodeException qCodeException = new QCodeException(prefix + " at line " + actualScriptLineNumber + (includeColumn ? (" column " + se.getColumnNumber()) : "") + ": " + message);
      if(includeContext)
      {
         qCodeException.setContext(errorContext);
      }

      return (qCodeException);
   }



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
