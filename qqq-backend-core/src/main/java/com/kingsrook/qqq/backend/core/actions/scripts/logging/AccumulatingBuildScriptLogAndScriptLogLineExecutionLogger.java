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

package com.kingsrook.qqq.backend.core.actions.scripts.logging;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class AccumulatingBuildScriptLogAndScriptLogLineExecutionLogger extends BuildScriptLogAndScriptLogLineExecutionLogger implements ScriptExecutionLoggerInterface
{
   private static final QLogger LOG = QLogger.getLogger(AccumulatingBuildScriptLogAndScriptLogLineExecutionLogger.class);

   private List<QRecord>       scriptLogs     = new ArrayList<>();
   private List<List<QRecord>> scriptLogLines = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public AccumulatingBuildScriptLogAndScriptLogLineExecutionLogger()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptExecutionStart(ExecuteCodeInput executeCodeInput)
   {
      super.acceptExecutionStart(executeCodeInput);
      super.setScriptLogLines(new ArrayList<>());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptException(Exception exception)
   {
      accumulate(null, exception);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptExecutionEnd(Serializable output)
   {
      accumulate(output, null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void accumulate(Serializable output, Exception exception)
   {
      super.updateHeaderAtEnd(output, exception);
      scriptLogs.add(super.getScriptLog());
      scriptLogLines.add(new ArrayList<>(super.getScriptLogLines()));
      super.getScriptLogLines().clear();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void storeAndClear()
   {
      try
      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName("scriptLog");
         insertInput.setRecords(scriptLogs);
         InsertOutput insertOutput = new InsertAction().execute(insertInput);

         List<QRecord> flatScriptLogLines = new ArrayList<>();
         for(int i = 0; i < insertOutput.getRecords().size(); i++)
         {
            QRecord       insertedScriptLog = insertOutput.getRecords().get(i);
            List<QRecord> subScriptLogLines = scriptLogLines.get(i);
            subScriptLogLines.forEach(r -> r.setValue("scriptLogId", insertedScriptLog.getValueInteger("id")));
            flatScriptLogLines.addAll(subScriptLogLines);
         }

         insertInput = new InsertInput();
         insertInput.setTableName("scriptLogLine");
         insertInput.setRecords(flatScriptLogLines);
         new InsertAction().execute(insertInput);
      }
      catch(Exception e)
      {
         LOG.warn("Error storing script logs", e);
      }

      scriptLogs.clear();
      scriptLogLines.clear();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setScriptId(Integer scriptId)
   {
      super.setScriptId(scriptId);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setScriptRevisionId(Integer scriptRevisionId)
   {
      super.setScriptRevisionId(scriptRevisionId);
   }
}
