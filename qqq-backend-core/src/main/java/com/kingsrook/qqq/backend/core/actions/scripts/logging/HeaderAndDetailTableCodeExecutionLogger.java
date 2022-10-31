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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Implementation of a code execution logger that logs into a header and child
 ** table
 *******************************************************************************/
public class HeaderAndDetailTableCodeExecutionLogger implements QCodeExecutionLoggerInterface
{
   private static final Logger LOG = LogManager.getLogger(HeaderAndDetailTableCodeExecutionLogger.class);

   private QRecord          header;
   private List<QRecord>    children = new ArrayList<>();
   private ExecuteCodeInput executeCodeInput;

   private Serializable scriptId;
   private Serializable scriptRevisionId;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public HeaderAndDetailTableCodeExecutionLogger(Serializable scriptId, Serializable scriptRevisionId)
   {
      this.scriptId = scriptId;
      this.scriptRevisionId = scriptRevisionId;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord buildHeaderRecord(ExecuteCodeInput executeCodeInput)
   {
      return (new QRecord()
         .withValue("scriptId", scriptId)
         .withValue("scriptRevisionId", scriptRevisionId)
         .withValue("startTimestamp", Instant.now())
         .withValue("input", truncate(executeCodeInput.getContext().toString())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QRecord buildDetailLogRecord(String logLine)
   {
      return (new QRecord()
         .withValue("scriptLogId", header.getValue("id"))
         .withValue("timestamp", Instant.now())
         .withValue("text", truncate(logLine)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String truncate(String logLine)
   {
      return StringUtils.safeTruncate(logLine, 1000, "...");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void updateHeaderAtEnd(Serializable output, Exception exception)
   {
      Instant startTimestamp = (Instant) header.getValue("startTimestamp");
      Instant endTimestamp   = Instant.now();
      header.setValue("endTimestamp", endTimestamp);
      header.setValue("runTimeMillis", startTimestamp.until(endTimestamp, ChronoUnit.MILLIS));

      if(exception != null)
      {
         header.setValue("hadError", true);
         header.setValue("error", exception.getMessage());
      }
      else
      {
         header.setValue("hadError", false);
         header.setValue("output", truncate(String.valueOf(output)));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void acceptExecutionStart(ExecuteCodeInput executeCodeInput)
   {
      try
      {
         this.executeCodeInput = executeCodeInput;
         QRecord scriptLog = buildHeaderRecord(executeCodeInput);

         InsertInput insertInput = new InsertInput(executeCodeInput.getInstance());
         insertInput.setSession(executeCodeInput.getSession());
         insertInput.setTableName("scriptLog");
         insertInput.setRecords(List.of(scriptLog));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         this.header = insertOutput.getRecords().get(0);
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
   public void acceptLogLine(String logLine)
   {
      children.add(buildDetailLogRecord(logLine));
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
         updateInput.setRecords(List.of(header));
         new UpdateAction().execute(updateInput);

         if(CollectionUtils.nullSafeHasContents(children))
         {
            InsertInput insertInput = new InsertInput(executeCodeInput.getInstance());
            insertInput.setSession(executeCodeInput.getSession());
            insertInput.setTableName("scriptLogLine");
            insertInput.setRecords(children);
            new InsertAction().execute(insertInput);
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error storing script log", e);
      }
   }

}
