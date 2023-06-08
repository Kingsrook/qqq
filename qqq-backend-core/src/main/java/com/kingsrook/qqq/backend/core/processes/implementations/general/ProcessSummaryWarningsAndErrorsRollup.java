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

package com.kingsrook.qqq.backend.core.processes.implementations.general;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;


/*******************************************************************************
 ** Helper class for process steps that want to roll up error summary and/or
 ** warning summary lines.  e.g., if the process might have a handful of different
 ** error messages.  Will record up to 50 unique errors, then throw the rest int
 ** an "other" errors summary.
 *******************************************************************************/
public class ProcessSummaryWarningsAndErrorsRollup
{
   private Map<String, ProcessSummaryLine> errorSummaries   = new HashMap<>();
   private Map<String, ProcessSummaryLine> warningSummaries = new HashMap<>();

   private ProcessSummaryLine otherErrorsSummary;
   private ProcessSummaryLine otherWarningsSummary;
   private ProcessSummaryLine errorTemplate;
   private ProcessSummaryLine warningTemplate;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ProcessSummaryWarningsAndErrorsRollup build(String pastTenseVerb)
   {
      return new ProcessSummaryWarningsAndErrorsRollup()
         .withErrorTemplate(new ProcessSummaryLine(Status.ERROR)
            .withSingularFutureMessage("record has an error: ")
            .withPluralFutureMessage("records have an error: ")
            .withSingularPastMessage("record had an error: ")
            .withPluralPastMessage("records had an error: "))
         .withWarningTemplate(new ProcessSummaryLine(Status.WARNING)
            .withSingularFutureMessage("record will be " + pastTenseVerb + ", but has a warning: ")
            .withPluralFutureMessage("records will be " + pastTenseVerb + ", but have a warning: ")
            .withSingularPastMessage("record was " + pastTenseVerb + ", but had a warning: ")
            .withPluralPastMessage("records were " + pastTenseVerb + ", but had a warning: "))
         .withOtherErrorsSummary(new ProcessSummaryLine(Status.ERROR)
            .withSingularFutureMessage("record has an other error.")
            .withPluralFutureMessage("records have other errors.")
            .withSingularPastMessage("record had an other error.")
            .withPluralPastMessage("records had other errors."))
         .withOtherWarningsSummary(new ProcessSummaryLine(Status.WARNING)
            .withSingularFutureMessage("record will be " + pastTenseVerb + ", but has an other warning.")
            .withPluralFutureMessage("records will be " + pastTenseVerb + ", but have other warnings.")
            .withSingularPastMessage("record was " + pastTenseVerb + ", but had other warnings.")
            .withPluralPastMessage("records were " + pastTenseVerb + ", but had other warnings."));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addToList(ArrayList<ProcessSummaryLineInterface> list)
   {
      addProcessSummaryLinesFromMap(list, errorSummaries);
      if(otherErrorsSummary != null)
      {
         otherErrorsSummary.addSelfToListIfAnyCount(list);
      }

      addProcessSummaryLinesFromMap(list, warningSummaries);
      if(otherWarningsSummary != null)
      {
         otherWarningsSummary.addSelfToListIfAnyCount(list);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addError(String message, Serializable primaryKey)
   {
      add(Status.ERROR, errorSummaries, errorTemplate, message, primaryKey);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addWarning(String message, Serializable primaryKey)
   {
      add(Status.WARNING, warningSummaries, warningTemplate, message, primaryKey);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int countWarnings()
   {
      int sum = 0;
      for(ProcessSummaryLine processSummaryLine : warningSummaries.values())
      {
         sum += Objects.requireNonNullElse(processSummaryLine.getCount(), 0);
      }
      if(otherWarningsSummary != null)
      {
         sum += Objects.requireNonNullElse(otherWarningsSummary.getCount(), 0);
      }
      return (sum);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int countErrors()
   {
      int sum = 0;
      for(ProcessSummaryLine processSummaryLine : errorSummaries.values())
      {
         sum += Objects.requireNonNullElse(processSummaryLine.getCount(), 0);
      }
      if(otherErrorsSummary != null)
      {
         sum += Objects.requireNonNullElse(otherErrorsSummary.getCount(), 0);
      }
      return (sum);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void add(Status status, Map<String, ProcessSummaryLine> summaryLineMap, ProcessSummaryLine templateLine, String message, Serializable primaryKey)
   {
      ProcessSummaryLine processSummaryLine = summaryLineMap.get(message);
      if(processSummaryLine == null)
      {
         if(summaryLineMap.size() < 50)
         {
            processSummaryLine = new ProcessSummaryLine(status)
               .withMessageSuffix(message)
               .withSingularFutureMessage(templateLine.getSingularFutureMessage())
               .withPluralFutureMessage(templateLine.getPluralFutureMessage())
               .withSingularPastMessage(templateLine.getSingularPastMessage())
               .withPluralPastMessage(templateLine.getPluralPastMessage());
            summaryLineMap.put(message, processSummaryLine);
         }
         else
         {
            if(status.equals(Status.ERROR))
            {
               if(otherErrorsSummary == null)
               {
                  otherErrorsSummary = new ProcessSummaryLine(Status.ERROR).withMessageSuffix("records had an other error.");
               }
               processSummaryLine = otherErrorsSummary;
            }
            else
            {
               if(otherWarningsSummary == null)
               {
                  otherWarningsSummary = new ProcessSummaryLine(Status.WARNING).withMessageSuffix("records had an other warning.");
               }
               processSummaryLine = otherWarningsSummary;
            }
         }
      }

      if(primaryKey == null)
      {
         processSummaryLine.incrementCount();
      }
      else
      {
         processSummaryLine.incrementCountAndAddPrimaryKey(primaryKey);
      }
   }



   /*******************************************************************************
    ** sort the process summary lines by count desc
    *******************************************************************************/
   private static void addProcessSummaryLinesFromMap(ArrayList<ProcessSummaryLineInterface> rs, Map<String, ProcessSummaryLine> summaryMap)
   {
      summaryMap.values().stream()
         .sorted(Comparator.comparing((ProcessSummaryLine psl) -> Objects.requireNonNullElse(psl.getCount(), 0)).reversed()
            .thenComparing((ProcessSummaryLine psl) -> Objects.requireNonNullElse(psl.getMessage(), ""))
            .thenComparing((ProcessSummaryLine psl) -> Objects.requireNonNullElse(psl.getMessageSuffix(), ""))
         )
         .forEach(psl -> psl.addSelfToListIfAnyCount(rs));
   }



   /*******************************************************************************
    ** Getter for otherErrorsSummary
    *******************************************************************************/
   public ProcessSummaryLine getOtherErrorsSummary()
   {
      return (this.otherErrorsSummary);
   }



   /*******************************************************************************
    ** Setter for otherErrorsSummary
    *******************************************************************************/
   public void setOtherErrorsSummary(ProcessSummaryLine otherErrorsSummary)
   {
      this.otherErrorsSummary = otherErrorsSummary;
   }



   /*******************************************************************************
    ** Fluent setter for otherErrorsSummary
    *******************************************************************************/
   public ProcessSummaryWarningsAndErrorsRollup withOtherErrorsSummary(ProcessSummaryLine otherErrorsSummary)
   {
      this.otherErrorsSummary = otherErrorsSummary;
      return (this);
   }



   /*******************************************************************************
    ** Getter for otherWarningsSummary
    *******************************************************************************/
   public ProcessSummaryLine getOtherWarningsSummary()
   {
      return (this.otherWarningsSummary);
   }



   /*******************************************************************************
    ** Setter for otherWarningsSummary
    *******************************************************************************/
   public void setOtherWarningsSummary(ProcessSummaryLine otherWarningsSummary)
   {
      this.otherWarningsSummary = otherWarningsSummary;
   }



   /*******************************************************************************
    ** Fluent setter for otherWarningsSummary
    *******************************************************************************/
   public ProcessSummaryWarningsAndErrorsRollup withOtherWarningsSummary(ProcessSummaryLine otherWarningsSummary)
   {
      this.otherWarningsSummary = otherWarningsSummary;
      return (this);
   }



   /*******************************************************************************
    ** Getter for errorTemplate
    *******************************************************************************/
   public ProcessSummaryLine getErrorTemplate()
   {
      return (this.errorTemplate);
   }



   /*******************************************************************************
    ** Setter for errorTemplate
    *******************************************************************************/
   public void setErrorTemplate(ProcessSummaryLine errorTemplate)
   {
      this.errorTemplate = errorTemplate;
   }



   /*******************************************************************************
    ** Fluent setter for errorTemplate
    *******************************************************************************/
   public ProcessSummaryWarningsAndErrorsRollup withErrorTemplate(ProcessSummaryLine errorTemplate)
   {
      this.errorTemplate = errorTemplate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for warningTemplate
    *******************************************************************************/
   public ProcessSummaryLine getWarningTemplate()
   {
      return (this.warningTemplate);
   }



   /*******************************************************************************
    ** Setter for warningTemplate
    *******************************************************************************/
   public void setWarningTemplate(ProcessSummaryLine warningTemplate)
   {
      this.warningTemplate = warningTemplate;
   }



   /*******************************************************************************
    ** Fluent setter for warningTemplate
    *******************************************************************************/
   public ProcessSummaryWarningsAndErrorsRollup withWarningTemplate(ProcessSummaryLine warningTemplate)
   {
      this.warningTemplate = warningTemplate;
      return (this);
   }

}
