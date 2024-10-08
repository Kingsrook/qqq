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

package com.kingsrook.qqq.backend.core.actions.scripts.logging;


import java.io.Serializable;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Implementation of a code execution logger that logs to LOG 4j
 *******************************************************************************/
public class Log4jCodeExecutionLogger implements QCodeExecutionLoggerInterface
{
   private static final QLogger LOG = QLogger.getLogger(Log4jCodeExecutionLogger.class);

   private QCodeReference qCodeReference;
   private String         uuid = UUID.randomUUID().toString();

   private boolean includeUUID = true;


   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptExecutionStart(ExecuteCodeInput executeCodeInput)
   {
      this.qCodeReference = executeCodeInput.getCodeReference();

      String inputString = StringUtils.safeTruncate(ValueUtils.getValueAsString(executeCodeInput.getInput()), 250, "...");
      LOG.info("Starting script execution: " + qCodeReference.getName() + (includeUUID ? ", uuid: " + uuid : "") + ", with input: " + inputString);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptLogLine(String logLine)
   {
      LOG.info("Script log: " + (includeUUID ? uuid + ": " : "") + logLine);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptException(Exception exception)
   {
      LOG.info("Script Exception: " + (includeUUID ? uuid : ""), exception);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptExecutionEnd(Serializable output)
   {
      String outputString = StringUtils.safeTruncate(ValueUtils.getValueAsString(output), 250, "...");
      LOG.info("Finished script execution: " + qCodeReference.getName() + (includeUUID ? ", uuid: " + uuid : "") + ", with output: " + outputString);
   }



   /*******************************************************************************
    ** Getter for includeUUID
    *******************************************************************************/
   public boolean getIncludeUUID()
   {
      return (this.includeUUID);
   }



   /*******************************************************************************
    ** Setter for includeUUID
    *******************************************************************************/
   public void setIncludeUUID(boolean includeUUID)
   {
      this.includeUUID = includeUUID;
   }



   /*******************************************************************************
    ** Fluent setter for includeUUID
    *******************************************************************************/
   public Log4jCodeExecutionLogger withIncludeUUID(boolean includeUUID)
   {
      this.includeUUID = includeUUID;
      return (this);
   }

}
