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
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.QCodeExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QCodeException;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 ** Interface to be implemented by language-specific code executors, e.g., in
 ** qqq-language-support-${languageName} maven modules.
 *******************************************************************************/
public interface QCodeExecutor
{

   /*******************************************************************************
    **
    *******************************************************************************/
   Serializable execute(QCodeReference codeReference, Map<String, Serializable> inputContext, QCodeExecutionLoggerInterface executionLogger) throws QCodeException;

   /*******************************************************************************
    ** Process an object from the script's language/runtime into a (more) native java object.
    ** e.g., a Nashorn ScriptObjectMirror will end up as a "primitive", or a List or Map of such
    **
    *******************************************************************************/
   default Object convertObjectToJava(Object object)
   {
      return (object);
   }

}
