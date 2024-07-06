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

package com.kingsrook.qqq.backend.core.processes.implementations.general;


import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import static com.kingsrook.qqq.backend.core.model.actions.processes.Status.ERROR;
import static com.kingsrook.qqq.backend.core.model.actions.processes.Status.OK;


/*******************************************************************************
 ** Helper for working with process summary lines
 *******************************************************************************/
public class StandardProcessSummaryLineProducer
{

   /*******************************************************************************
    ** Make a line that'll say " {will be/was/were} inserted"
    *******************************************************************************/
   public static ProcessSummaryLine getOkToInsertLine()
   {
      return new ProcessSummaryLine(OK)
         .withMessageSuffix(" inserted")
         .withSingularFutureMessage("will be")
         .withPluralFutureMessage("will be")
         .withSingularPastMessage("was")
         .withPluralPastMessage("were");
   }



   /*******************************************************************************
    ** Make a line that'll say " {will be/was/were} updated"
    *******************************************************************************/
   public static ProcessSummaryLine getOkToUpdateLine()
   {
      return new ProcessSummaryLine(OK)
         .withMessageSuffix(" updated")
         .withSingularFutureMessage("will be")
         .withPluralFutureMessage("will be")
         .withSingularPastMessage("was")
         .withPluralPastMessage("were");
   }



   /*******************************************************************************
    ** Make a line that'll say " {will be/was/were} deleted"
    *******************************************************************************/
   public static ProcessSummaryLine getOkToDeleteLine()
   {
      return new ProcessSummaryLine(OK)
         .withMessageSuffix(" deleted")
         .withSingularFutureMessage("will be")
         .withPluralFutureMessage("will be")
         .withSingularPastMessage("was")
         .withPluralPastMessage("were");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ProcessSummaryLine getNoDifferencesNoUpdateLine()
   {
      return new ProcessSummaryLine(Status.INFO)
         .withSingularFutureMessage("has no differences and will not be updated")
         .withPluralFutureMessage("have no differences and will not be updated")
         .withSingularPastMessage("has no differences and was not updated")
         .withPluralPastMessage("have no differences and were not updated");
   }



   /*******************************************************************************
    ** Make a line that'll say " had an error"
    *******************************************************************************/
   public static ProcessSummaryLine getErrorLine()
   {
      return new ProcessSummaryLine(ERROR, "had an error");
   }



   /*******************************************************************************
    ** one-liner for implementing getProcessSummary - just pass your lines in as varargs as in:
    ** return (StandardProcessSummaryLineProducer.toArrayList(okToInsert, okToUpdate));
    *******************************************************************************/
   public static ArrayList<ProcessSummaryLineInterface> toArrayList(ProcessSummaryLine... lines)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      for(ProcessSummaryLine line : lines)
      {
         line.addSelfToListIfAnyCount(rs);
      }
      return (rs);
   }
}
