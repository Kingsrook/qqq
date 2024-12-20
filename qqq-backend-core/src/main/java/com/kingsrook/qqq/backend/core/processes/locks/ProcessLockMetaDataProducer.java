/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.locks;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 ** MetaData producer for Process Locks "system"
 *******************************************************************************/
public class ProcessLockMetaDataProducer implements MetaDataProducerInterface<MetaDataProducerMultiOutput>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public MetaDataProducerMultiOutput produce(QInstance qInstance) throws QException
   {
      MetaDataProducerMultiOutput output = new MetaDataProducerMultiOutput();

      ////////////////////////
      // process lock table //
      ////////////////////////
      output.add(new QTableMetaData()
         .withName(ProcessLock.TABLE_NAME)
         .withFieldsFromEntity(ProcessLock.class)
         .withIcon(new QIcon().withName("sync_lock"))
         .withUniqueKey(new UniqueKey("processLockTypeId", "key"))
         .withRecordLabelFormat("%s %s")
         .withRecordLabelFields("processLockTypeId", "key")
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "processLockTypeId", "key")))
         .withSection(new QFieldSection("data", new QIcon().withName("text_snippet"), Tier.T2, List.of("userId", "sessionUUID", "details", "checkInTimestamp", "expiresAtTimestamp")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")))
      );

      /////////////////////////////
      // process lock type table //
      /////////////////////////////
      output.add(new QTableMetaData()
         .withName(ProcessLockType.TABLE_NAME)
         .withFieldsFromEntity(ProcessLockType.class)
         .withIcon(new QIcon().withName("lock"))
         .withUniqueKey(new UniqueKey("name"))
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("label")
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id", "name", "label")))
         .withSection(new QFieldSection("data", new QIcon().withName("text_snippet"), Tier.T2, List.of("defaultExpirationSeconds")))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")))
      );

      ///////////////////////////
      // process lock type PVS //
      ///////////////////////////
      output.add(QPossibleValueSource.newForTable(ProcessLockType.TABLE_NAME));

      /////////////////////////////////////////////////////
      // join between process lock type and process lock //
      /////////////////////////////////////////////////////
      output.add(new QJoinMetaData()
         .withLeftTable(ProcessLockType.TABLE_NAME)
         .withRightTable(ProcessLock.TABLE_NAME)
         .withInferredName()
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "processLockTypeId"))
      );

      return output;
   }

}
