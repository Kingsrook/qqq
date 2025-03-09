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

package com.kingsrook.qqq.backend.core.model.metadata.qbits;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Interface for configuration settings used both in the production of meta-data
 ** for a QBit, but also at runtime, e.g., to be aware of exactly how the qbit
 ** has been incorporated into an application.
 **
 ** For example:
 ** - should the QBit define certain tables, or will they be supplied by the application?
 ** - what other meta-data names should the qbit reference (backends, schedulers)
 ** - what meta-data-customizer(s) should be used?
 **
 ** When implementing a QBit, you'll implement this interface - adding whatever
 ** (if any) properties you need, and if you have any rules, then overriding
 ** the validate method (ideally the one that takes the List-of-String errors)
 **
 ** When using a QBit, you'll create an instance of the QBit's config object,
 ** and pass it through to the QBit producer.
 *******************************************************************************/
public interface QBitConfig extends Serializable
{
   QLogger LOG = QLogger.getLogger(QBitConfig.class);


   /***************************************************************************
    **
    ***************************************************************************/
   default void validate(QInstance qInstance) throws QBitConfigValidationException
   {
      List<String> errors = new ArrayList<>();

      try
      {
         validate(qInstance, errors);
      }
      catch(Exception e)
      {
         LOG.warn("Error validating QBitConfig: " + this.getClass().getName(), e);
      }

      if(!errors.isEmpty())
      {
         throw (new QBitConfigValidationException(this, errors));
      }
   }


   /***************************************************************************
    **
    ***************************************************************************/
   default void validate(QInstance qInstance, List<String> errors)
   {
      /////////////////////////////////////
      // nothing to validate by default! //
      /////////////////////////////////////
   }


   /***************************************************************************
    **
    ***************************************************************************/
   default boolean assertCondition(boolean condition, String message, List<String> errors)
   {
      if(!condition)
      {
         errors.add(message);
      }
      return (condition);
   }


   /***************************************************************************
    **
    ***************************************************************************/
   default MetaDataCustomizerInterface<QTableMetaData> getTableMetaDataCustomizer()
   {
      return (null);
   }
}
