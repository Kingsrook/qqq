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

package com.kingsrook.qqq.backend.core.model.actions.scripts;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.QCodeExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 **
 *******************************************************************************/
public class ExecuteCodeInput extends AbstractActionInput
{
   private QCodeReference                codeReference;
   private Map<String, Serializable>     input;
   private Map<String, Serializable>     context;
   private QCodeExecutionLoggerInterface executionLogger;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ExecuteCodeInput(QInstance qInstance)
   {
      super(qInstance);
   }



   /*******************************************************************************
    ** Getter for codeReference
    **
    *******************************************************************************/
   public QCodeReference getCodeReference()
   {
      return codeReference;
   }



   /*******************************************************************************
    ** Setter for codeReference
    **
    *******************************************************************************/
   public void setCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
   }



   /*******************************************************************************
    ** Fluent setter for codeReference
    **
    *******************************************************************************/
   public ExecuteCodeInput withCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
      return (this);
   }



   /*******************************************************************************
    ** Getter for input
    **
    *******************************************************************************/
   public Map<String, Serializable> getInput()
   {
      return input;
   }



   /*******************************************************************************
    ** Setter for input
    **
    *******************************************************************************/
   public void setInput(Map<String, Serializable> input)
   {
      this.input = input;
   }



   /*******************************************************************************
    ** Fluent setter for input
    **
    *******************************************************************************/
   public ExecuteCodeInput withInput(Map<String, Serializable> input)
   {
      this.input = input;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for input
    **
    *******************************************************************************/
   public ExecuteCodeInput withInput(String key, Serializable value)
   {
      if(this.input == null)
      {
         input = new HashMap<>();
      }
      this.input.put(key, value);
      return (this);
   }



   /*******************************************************************************
    ** Getter for context
    **
    *******************************************************************************/
   public Map<String, Serializable> getContext()
   {
      return context;
   }



   /*******************************************************************************
    ** Setter for context
    **
    *******************************************************************************/
   public void setContext(Map<String, Serializable> context)
   {
      this.context = context;
   }



   /*******************************************************************************
    ** Fluent setter for context
    **
    *******************************************************************************/
   public ExecuteCodeInput withContext(Map<String, Serializable> context)
   {
      this.context = context;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for context
    **
    *******************************************************************************/
   public ExecuteCodeInput withContext(String key, Serializable value)
   {
      if(this.context == null)
      {
         context = new HashMap<>();
      }
      this.context.put(key, value);
      return (this);
   }



   /*******************************************************************************
    ** Getter for executionLogger
    **
    *******************************************************************************/
   public QCodeExecutionLoggerInterface getExecutionLogger()
   {
      return executionLogger;
   }



   /*******************************************************************************
    ** Setter for executionLogger
    **
    *******************************************************************************/
   public void setExecutionLogger(QCodeExecutionLoggerInterface executionLogger)
   {
      this.executionLogger = executionLogger;
   }



   /*******************************************************************************
    ** Fluent setter for executionLogger
    **
    *******************************************************************************/
   public ExecuteCodeInput withExecutionLogger(QCodeExecutionLoggerInterface executionLogger)
   {
      this.executionLogger = executionLogger;
      return (this);
   }

}
