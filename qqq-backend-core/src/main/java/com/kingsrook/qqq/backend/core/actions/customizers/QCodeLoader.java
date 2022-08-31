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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.util.Optional;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TableAutomationAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Utility to load code for running QQQ customizers.
 *******************************************************************************/
public class QCodeLoader
{
   private static final Logger LOG = LogManager.getLogger(QCodeLoader.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <T, R> Optional<Function<T, R>> getTableCustomizerFunction(QTableMetaData table, String customizerName)
   {
      Optional<QCodeReference> codeReference = table.getCustomizer(customizerName);
      if(codeReference.isPresent())
      {
         return (Optional.ofNullable(QCodeLoader.getFunction(codeReference.get())));
      }
      return (Optional.empty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static <T, R> Function<T, R> getFunction(QCodeReference codeReference)
   {
      if(codeReference == null)
      {
         return (null);
      }

      if(!codeReference.getCodeType().equals(QCodeType.JAVA))
      {
         ///////////////////////////////////////////////////////////////////////////////////////
         // todo - 1) support more languages, 2) wrap them w/ java Functions here, 3) profit! //
         ///////////////////////////////////////////////////////////////////////////////////////
         throw (new IllegalArgumentException("Only JAVA customizers are supported at this time."));
      }

      try
      {
         Class<?> customizerClass = Class.forName(codeReference.getName());
         return ((Function<T, R>) customizerClass.getConstructor().newInstance());
      }
      catch(Exception e)
      {
         LOG.error("Error initializing customizer: " + codeReference);

         //////////////////////////////////////////////////////////////////////////////////////////////////////////
         // return null here - under the assumption that during normal run-time operations, we'll never hit here //
         // as we'll want to validate all functions in the instance validator at startup time (and IT will throw //
         // if it finds an invalid code reference                                                                //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////
         return (null);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <T extends BackendStep> T getBackendStep(Class<T> expectedType, QCodeReference codeReference)
   {
      if(codeReference == null)
      {
         return (null);
      }

      if(!codeReference.getCodeType().equals(QCodeType.JAVA))
      {
         ///////////////////////////////////////////////////////////////////////////////////////
         // todo - 1) support more languages, 2) wrap them w/ java Functions here, 3) profit! //
         ///////////////////////////////////////////////////////////////////////////////////////
         throw (new IllegalArgumentException("Only JAVA BackendSteps are supported at this time."));
      }

      try
      {
         Class<?> customizerClass = Class.forName(codeReference.getName());
         @SuppressWarnings("unchecked")
         T t = (T) customizerClass.getConstructor().newInstance();
         return t;
      }
      catch(Exception e)
      {
         LOG.error("Error initializing customizer: " + codeReference);

         //////////////////////////////////////////////////////////////////////////////////////////////////////////
         // return null here - under the assumption that during normal run-time operations, we'll never hit here //
         // as we'll want to validate all functions in the instance validator at startup time (and IT will throw //
         // if it finds an invalid code reference                                                                //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////
         return (null);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static RecordAutomationHandler getRecordAutomationHandler(TableAutomationAction action) throws QException
   {
      try
      {
         QCodeReference codeReference = action.getCodeReference();
         if(!codeReference.getCodeType().equals(QCodeType.JAVA))
         {
            ///////////////////////////////////////////////////////////////////////////////////////
            // todo - 1) support more languages, 2) wrap them w/ java Functions here, 3) profit! //
            ///////////////////////////////////////////////////////////////////////////////////////
            throw (new IllegalArgumentException("Only JAVA customizers are supported at this time."));
         }

         Class<?> codeClass  = Class.forName(codeReference.getName());
         Object   codeObject = codeClass.getConstructor().newInstance();
         if(!(codeObject instanceof RecordAutomationHandler recordAutomationHandler))
         {
            throw (new QException("The supplied code [" + codeClass.getName() + "] is not an instance of RecordAutomationHandler"));
         }
         return (recordAutomationHandler);
      }
      catch(QException qe)
      {
         throw (qe);
      }
      catch(Exception e)
      {
         throw (new QException("Error getting record automation handler for action [" + action.getName() + "]", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QCustomPossibleValueProvider getCustomPossibleValueProvider(QPossibleValueSource possibleValueSource) throws QException
   {
      try
      {
         Class<?> codeClass  = Class.forName(possibleValueSource.getCustomCodeReference().getName());
         Object   codeObject = codeClass.getConstructor().newInstance();
         if(!(codeObject instanceof QCustomPossibleValueProvider customPossibleValueProvider))
         {
            throw (new QException("The supplied code [" + codeClass.getName() + "] is not an instance of QCustomPossibleValueProvider"));
         }
         return (customPossibleValueProvider);
      }
      catch(QException qe)
      {
         throw (qe);
      }
      catch(Exception e)
      {
         throw (new QException("Error getting custom possible value provider for PVS [" + possibleValueSource.getName() + "]", e));
      }
   }

}
