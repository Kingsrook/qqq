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
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Implementation of a code execution logger that logs into scriptLog and scriptLogLine
 ** tables - e.g., as defined in ScriptMetaDataProvider.
 *******************************************************************************/
public class StoreScriptLogAndScriptLogLineExecutionLogger extends BuildScriptLogAndScriptLogLineExecutionLogger
{
   private static final Logger LOG = LogManager.getLogger(StoreScriptLogAndScriptLogLineExecutionLogger.class);



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public StoreScriptLogAndScriptLogLineExecutionLogger(Serializable scriptId, Serializable scriptRevisionId)
   {
      super(scriptId, scriptRevisionId);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptExecutionStart(ExecuteCodeInput executeCodeInput)
   {
      try
      {
         super.acceptExecutionStart(executeCodeInput);

         InsertInput insertInput = new InsertInput(executeCodeInput.getInstance());
         insertInput.setSession(executeCodeInput.getSession());
         insertInput.setTableName("scriptLog");
         insertInput.setRecords(List.of(getScriptLog()));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);

         setScriptLog(insertOutput.getRecords().get(0));
      }
      catch(Exception e)
      {
         LOG.warn("Error starting storage of script log", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptException(Exception exception)
   {
      store(null, exception);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptExecutionEnd(Serializable output)
   {
      store(output, null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void store(Serializable output, Exception exception)
   {
      try
      {
         updateHeaderAtEnd(output, exception);
         UpdateInput updateInput = new UpdateInput(executeCodeInput.getInstance());
         updateInput.setSession(executeCodeInput.getSession());
         updateInput.setTableName("scriptLog");
         updateInput.setRecords(List.of(getScriptLog()));
         new UpdateAction().execute(updateInput);

         if(CollectionUtils.nullSafeHasContents(getScriptLogLines()))
         {
            InsertInput insertInput = new InsertInput(executeCodeInput.getInstance());
            insertInput.setSession(executeCodeInput.getSession());
            insertInput.setTableName("scriptLogLine");
            insertInput.setRecords(getScriptLogLines());
            new InsertAction().execute(insertInput);
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error storing script log", e);
      }
   }

}
