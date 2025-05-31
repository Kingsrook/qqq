/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.automation;


import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.automation.RecordAutomationInput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** RecordAutomationHandler implementation that is called by automation runner
 ** that doesn't know to deal with a TableTrigger record that it received.
 **
 ** e.g., if an app has altered that table (e.g., workflows-qbit).
 *******************************************************************************/
public class RunCustomTableTriggerRecordAutomationHandler implements RecordAutomationHandlerInterface
{
   private static final QLogger LOG = QLogger.getLogger(RunCustomTableTriggerRecordAutomationHandler.class);

   private static Map<String, QCodeReference> handlers = new LinkedHashMap<>();



   /***************************************************************************
    **
    ***************************************************************************/
   public static void registerHandler(String name, QCodeReference codeReference)
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if there's already a value mapped for this name, warn about it (unless it's for the same code reference) //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(handlers.containsKey(name))
      {
         if(handlers.get(name).getName().equals(codeReference.getName()))
         {
            LOG.warn("Registering a CustomTableTriggerRecordAutomationHandler for a name that is already registered", logPair("name", name));
         }
      }

      handlers.put(name, codeReference);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void execute(RecordAutomationInput recordAutomationInput) throws QException
   {
      for(QCodeReference codeReference : handlers.values())
      {
         CustomTableTriggerRecordAutomationHandler customHandler = QCodeLoader.getAdHoc(CustomTableTriggerRecordAutomationHandler.class, codeReference);
         if(customHandler.handlesThisInput(recordAutomationInput))
         {
            customHandler.execute(recordAutomationInput);
            return;
         }
      }

      throw (new QException("No custom record automation handler was found for " + recordAutomationInput));
   }

}
