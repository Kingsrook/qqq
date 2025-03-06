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


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** interface for how a QBit's meta-data gets produced and added to a QInstance.
 **
 ** When implementing a QBit, you'll implement this interface:
 ** - adding a QBitConfig subclass as a property
 ** - overriding the produce(qInstance, namespace) method - where you'll:
 ** -- create and add your QBitMetaData
 ** -- call MetaDataProducerHelper.findProducers
 ** -- hand off to finishProducing() in this interface
 **
 ** When using a QBit, you'll create an instance of the QBit's config object,
 ** pass it in to the producer, then call produce, ala:
 **
 ** new SomeQBitProducer()
 **   .withQBitConfig(someQBitConfig)
 **   .produce(qInstance);
 **
 *******************************************************************************/
public interface QBitProducer
{
   QLogger LOG = QLogger.getLogger(QBitProducer.class);


   /***************************************************************************
    **
    ***************************************************************************/
   default void produce(QInstance qInstance) throws QException
   {
      produce(qInstance, null);
   }

   /***************************************************************************
    **
    ***************************************************************************/
   void produce(QInstance qInstance, String namespace) throws QException;


   /***************************************************************************
    *
    ***************************************************************************/
   default <C extends QBitConfig> void finishProducing(QInstance qInstance, QBitMetaData qBitMetaData, C qBitConfig, List<MetaDataProducerInterface<?>> producers) throws QException
   {
      qBitConfig.validate(qInstance);

      ///////////////////////////////
      // todo - move to base class //
      ///////////////////////////////
      for(MetaDataProducerInterface<?> producer : producers)
      {
         if(producer instanceof QBitComponentMetaDataProducer<?, ?>)
         {
            QBitComponentMetaDataProducer<?, C> qBitComponentMetaDataProducer = (QBitComponentMetaDataProducer<?, C>) producer;
            qBitComponentMetaDataProducer.setQBitConfig(qBitConfig);
         }

         if(!producer.isEnabled())
         {
            LOG.debug("Not using producer which is not enabled", logPair("producer", producer.getClass().getSimpleName()));
            continue;
         }

         MetaDataProducerOutput output = producer.produce(qInstance);

         /////////////////////////////////////////
         // apply table customizer, if provided //
         /////////////////////////////////////////
         if(qBitConfig.getTableMetaDataCustomizer() != null && output instanceof QTableMetaData table)
         {
            output = qBitConfig.getTableMetaDataCustomizer().customizeMetaData(qInstance, table);
         }

         /////////////////////////////////////////////////
         // set source qbit, if output is aware of such //
         /////////////////////////////////////////////////
         if(output instanceof SourceQBitAware sourceQBitAware)
         {
            sourceQBitAware.setSourceQBitName(qBitMetaData.getName());
         }

         output.addSelfToInstance(qInstance);
      }
   }
}
