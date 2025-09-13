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

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Interface for objects that can be output from a process to summarize its results.
 *******************************************************************************/
public interface ProcessSummaryLineInterface extends Serializable
{
   QLogger LOG = QLogger.getLogger(ProcessSummaryLineInterface.class);

   /***************************************************************************
    **
    ***************************************************************************/
   static void log(String message, Serializable summaryLines, List<LogPair> additionalLogPairs)
   {
      try
      {
         if(summaryLines instanceof List)
         {
            List<ProcessSummaryLineInterface> list = (List<ProcessSummaryLineInterface>) summaryLines;

            List<LogPair> logPairs = new ArrayList<>();
            for(ProcessSummaryLineInterface processSummaryLineInterface : list)
            {
               LogPair logPair = processSummaryLineInterface.toLogPair();
               logPair.setKey(logPair.getKey() + logPairs.size());
               logPairs.add(logPair);
            }

            if(additionalLogPairs != null)
            {
               logPairs.addAll(0, additionalLogPairs);
            }
            logPairs.add(0, logPair("message", message));

            LOG.info(logPairs);
         }
         else
         {
            LOG.info("Unrecognized type for summaryLines (expected List)", logPair("processSummary", summaryLines));
         }
      }
      catch(Exception e)
      {
         LOG.info("Error logging a process summary", e, logPair("processSummary", summaryLines));
      }
   }

   /*******************************************************************************
    ** Getter for status
    **
    *******************************************************************************/
   Status getStatus();

   /*******************************************************************************
    **
    *******************************************************************************/
   String getMessage();

   /*******************************************************************************
    ** meant to be called by framework, after process is complete, give the
    ** summary object a chance to finalize itself before it's sent to a frontend.
    *******************************************************************************/
   default void prepareForFrontend(boolean isForResultScreen)
   {

   }

   /*******************************************************************************
    **
    *******************************************************************************/
   LogPair toLogPair();
}
