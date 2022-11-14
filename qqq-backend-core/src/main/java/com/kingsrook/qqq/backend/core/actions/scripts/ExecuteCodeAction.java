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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.Log4jCodeExecutionLogger;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.QCodeExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QCodeException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 ** Action to execute user/runtime defined code.
 **
 ** This action is designed to support code in multiple languages, by using
 ** executors, e.g., provided by additional runtime qqq dependencies.  Initially
 ** we are building qqq-language-support-javascript.
 **
 ** We also have a Java executor, to provide at least a little bit of testability
 ** within qqq-backend-core.  This executor is a candidate to be replaced in the
 ** future with something that would do actual dynamic java (whether that's compiled
 ** at runtime, or loaded from a plugin jar at runtime).  In other words, the java
 ** executor in place today is just meant to be a placeholder.
 *******************************************************************************/
public class ExecuteCodeAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:indentation")
   public void run(ExecuteCodeInput input, ExecuteCodeOutput output) throws QException, QCodeException
   {
      QCodeReference codeReference = input.getCodeReference();

      QCodeExecutionLoggerInterface executionLogger = input.getExecutionLogger();
      if(executionLogger == null)
      {
         executionLogger = getDefaultExecutionLogger();
      }
      executionLogger.acceptExecutionStart(input);

      try
      {
         String languageExecutor = switch(codeReference.getCodeType())
            {
               case JAVA -> "com.kingsrook.qqq.backend.core.actions.scripts.QJavaExecutor";
               case JAVA_SCRIPT -> "com.kingsrook.qqq.languages.javascript.QJavaScriptExecutor";
            };

         @SuppressWarnings("unchecked")
         Class<? extends QCodeExecutor> executorClass = (Class<? extends QCodeExecutor>) Class.forName(languageExecutor);
         QCodeExecutor qCodeExecutor = executorClass.getConstructor().newInstance();

         ////////////////////////////////////////////////////////////////////////////////////////////////////
         // merge all of the input context, plus the input... input - into a context for the code executor //
         ////////////////////////////////////////////////////////////////////////////////////////////////////
         Map<String, Serializable> context = new HashMap<>();
         if(input.getContext() != null)
         {
            context.putAll(input.getContext());
         }
         if(input.getInput() != null)
         {
            context.putAll(input.getInput());
         }

         Serializable codeOutput = qCodeExecutor.execute(codeReference, context, executionLogger);
         output.setOutput(codeOutput);
         executionLogger.acceptExecutionEnd(codeOutput);
      }
      catch(QCodeException qCodeException)
      {
         executionLogger.acceptException(qCodeException);
         throw (qCodeException);
      }
      catch(Exception e)
      {
         executionLogger.acceptException(e);
         throw (new QException("Error executing code [" + codeReference + "]", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QCodeExecutionLoggerInterface getDefaultExecutionLogger()
   {
      return (new Log4jCodeExecutionLogger());
   }

}