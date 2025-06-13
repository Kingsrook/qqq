/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.instances.assessment;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** POC of a class that is meant to review meta-data for accuracy vs. real backends.
 *******************************************************************************/
public class QInstanceAssessor
{
   private static final QLogger LOG = QLogger.getLogger(QInstanceAssessor.class);

   private final QInstance qInstance;

   private List<String> errors      = new ArrayList<>();
   private List<String> warnings    = new ArrayList<>();
   private List<String> suggestions = new ArrayList<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QInstanceAssessor(QInstance qInstance)
   {
      this.qInstance = qInstance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void assess()
   {
      for(QBackendMetaData backend : qInstance.getBackends().values())
      {
         if(backend instanceof Assessable assessable)
         {
            assessable.assess(this, qInstance);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
   public void printSummary()
   {
      ///////////////////////////
      // print header & errors //
      ///////////////////////////
      if(CollectionUtils.nullSafeIsEmpty(errors))
      {
         System.out.println("Assessment passed with no errors! \uD83D\uDE0E");
      }
      else
      {
         System.out.println("Assessment found the following " + StringUtils.plural(errors, "error", "errors") + ": \uD83D\uDE32");

         for(String error : errors)
         {
            System.out.println(" - " + error);
         }
      }

      /////////////////////////////////////
      // print warnings if there are any //
      /////////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(warnings))
      {
         System.out.println("\nAssessment found the following " + StringUtils.plural(warnings, "warning", "warnings") + ": \uD83E\uDD28");

         for(String warning : warnings)
         {
            System.out.println(" - " + warning);
         }
      }

      //////////////////////////////////////////
      // print suggestions, if there were any //
      //////////////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(suggestions))
      {
         System.out.println("\nThe following " + StringUtils.plural(suggestions, "fix is", "fixes are") + " suggested: \uD83E\uDD13");

         for(String suggestion : suggestions)
         {
            System.out.println("\n" + suggestion + "\n");
         }
      }
   }



   /*******************************************************************************
    ** Getter for qInstance
    **
    *******************************************************************************/
   public QInstance getInstance()
   {
      return qInstance;
   }



   /*******************************************************************************
    ** Getter for errors
    **
    *******************************************************************************/
   public List<String> getErrors()
   {
      return errors;
   }



   /*******************************************************************************
    ** Getter for warnings
    **
    *******************************************************************************/
   public List<String> getWarnings()
   {
      return warnings;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addError(String errorMessage)
   {
      errors.add(errorMessage);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addWarning(String warningMessage)
   {
      warnings.add(warningMessage);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addError(String errorMessage, Exception e)
   {
      addError(errorMessage + " : " + e.getMessage());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addSuggestion(String message)
   {
      suggestions.add(message);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int getExitCode()
   {
      if(CollectionUtils.nullSafeHasContents(errors))
      {
         return (1);
      }
      else
      {
         return (0);
      }
   }
}
