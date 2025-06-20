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


import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerOutput;


/*******************************************************************************
 ** extension of MetaDataProducerInterface, designed for producing meta data
 ** within a (java-defined, at this time) QBit.
 **
 ** Specifically exists to accept the QBitConfig as a type parameter and a value,
 ** easily accessed in the producer's methods as getQBitConfig()
 *******************************************************************************/
public abstract class QBitComponentMetaDataProducer<T extends MetaDataProducerOutput, C extends QBitConfig> implements QBitComponentMetaDataProducerInterface<T, C>
{
   private C qBitConfig = null;



   /*******************************************************************************
    ** Getter for qBitConfig
    *******************************************************************************/
   @Override
   public C getQBitConfig()
   {
      return (this.qBitConfig);
   }



   /*******************************************************************************
    ** Setter for qBitConfig
    *******************************************************************************/
   @Override
   public void setQBitConfig(C qBitConfig)
   {
      this.qBitConfig = qBitConfig;
   }



   /*******************************************************************************
    ** Fluent setter for qBitConfig
    *******************************************************************************/
   public QBitComponentMetaDataProducer<T, C> withQBitConfig(C qBitConfig)
   {
      this.qBitConfig = qBitConfig;
      return (this);
   }

}
