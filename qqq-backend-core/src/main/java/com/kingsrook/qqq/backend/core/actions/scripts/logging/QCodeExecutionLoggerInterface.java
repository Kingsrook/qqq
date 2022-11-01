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
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;


/*******************************************************************************
 ** Interface to provide logging functionality to QCodeExecution (e.g., scripts)
 *******************************************************************************/
public interface QCodeExecutionLoggerInterface
{

   /*******************************************************************************
    ** Called when the execution starts - takes the execution's input object.
    *******************************************************************************/
   void acceptExecutionStart(ExecuteCodeInput executeCodeInput);

   /*******************************************************************************
    ** Called to log a line, a message.
    *******************************************************************************/
   void acceptLogLine(String logLine);

   /*******************************************************************************
    ** In case the loggerInterface object is provided to the script as context,
    ** this method gives a clean interface for the script to log a line.
    *******************************************************************************/
   default void log(String message)
   {
      acceptLogLine(message);
   }

   /*******************************************************************************
    ** Called if the script fails with an exception.
    *******************************************************************************/
   void acceptException(Exception exception);

   /*******************************************************************************
    ** Called if the script completes without exception.
    *******************************************************************************/
   void acceptExecutionEnd(Serializable output);

}
