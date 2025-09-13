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
   public String getSummary()
   {
      StringBuilder rs = new StringBuilder();

      ///////////////////////////
      // print header & errors //
      ///////////////////////////
      if(CollectionUtils.nullSafeIsEmpty(errors))
      {
         rs.append("Assessment passed with no errors! \uD83D\uDE0E\n");
      }
      else
      {
         rs.append("Assessment found the following ").append(StringUtils.plural(errors, "error", "errors")).append(": \uD83D\uDE32\n");

         for(String error : errors)
         {
            rs.append(" - ").append(error).append("\n");
         }
      }

      /////////////////////////////////////
      // print warnings if there are any //
      /////////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(warnings))
      {
         rs.append("\nAssessment found the following ").append(StringUtils.plural(warnings, "warning", "warnings")).append(": \uD83E\uDD28\n");

         for(String warning : warnings)
         {
            rs.append(" - ").append(warning).append("\n");
         }
      }

      //////////////////////////////////////////
      // print suggestions, if there were any //
      //////////////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(suggestions))
      {
         rs.append("\nThe following ").append(StringUtils.plural(suggestions, "fix is", "fixes are")).append(" suggested: \uD83E\uDD13\n");

         for(String suggestion : suggestions)
         {
            rs.append("\n").append(suggestion).append("\n\n");
         }
      }

      return (rs.toString());
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
