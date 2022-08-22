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
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
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
   public static Function<?, ?> getTableCustomizerFunction(QTableMetaData table, String customizerName)
   {
      Optional<QCodeReference> codeReference = table.getCustomizer(customizerName);
      if(codeReference.isPresent())
      {
         return (QCodeLoader.getFunction(codeReference.get()));
      }

      return null;
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
   public static <T> T getAdHoc(Class<T> expectedType, QCodeReference codeReference)
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
         throw (new IllegalArgumentException("Only JAVA code references are supported at this time."));
      }

      try
      {
         Class<?> customizerClass = Class.forName(codeReference.getName());
         return ((T) customizerClass.getConstructor().newInstance());
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

}
