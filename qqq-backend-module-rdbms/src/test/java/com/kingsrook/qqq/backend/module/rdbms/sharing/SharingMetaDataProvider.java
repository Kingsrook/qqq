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

package com.kingsrook.qqq.backend.module.rdbms.sharing;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Asset;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.AssetAudienceInt;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Audience;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Client;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Group;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.User;


/*******************************************************************************
 **
 *******************************************************************************/
public class SharingMetaDataProvider
{
   public static final String USER_ID_KEY_TYPE            = "userIdKey";
   public static final String USER_ID_ALL_ACCESS_KEY_TYPE = "userIdAllAccessKey";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void defineAll(QInstance qInstance) throws QException
   {
      qInstance.addSecurityKeyType(new QSecurityKeyType()
         .withName(USER_ID_KEY_TYPE)
         .withAllAccessKeyName(USER_ID_ALL_ACCESS_KEY_TYPE));

      qInstance.addTable(new QTableMetaData()
         .withName(Asset.TABLE_NAME)
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withFieldsFromEntity(Asset.class)
         .withRecordSecurityLock(new RecordSecurityLock()
            .withSecurityKeyType(USER_ID_KEY_TYPE)
            .withFieldName("userId")));
      QInstanceEnricher.setInferredFieldBackendNames(qInstance.getTable(Asset.TABLE_NAME));

      qInstance.addTable(new QTableMetaData()
         .withName(Audience.TABLE_NAME)
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withFieldsFromEntity(Audience.class));
      QInstanceEnricher.setInferredFieldBackendNames(qInstance.getTable(Audience.TABLE_NAME));

      qInstance.addTable(new QTableMetaData()
         .withName(AssetAudienceInt.TABLE_NAME)
         .withBackendDetails(new RDBMSTableBackendDetails().withTableName("asset_audience_int"))
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withFieldsFromEntity(AssetAudienceInt.class));
      QInstanceEnricher.setInferredFieldBackendNames(qInstance.getTable(AssetAudienceInt.TABLE_NAME));

      qInstance.addTable(new QTableMetaData()
         .withName(User.TABLE_NAME)
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withFieldsFromEntity(User.class)
         .withRecordSecurityLock(new RecordSecurityLock()
            .withSecurityKeyType(USER_ID_KEY_TYPE)
            .withFieldName("id")));
      QInstanceEnricher.setInferredFieldBackendNames(qInstance.getTable(User.TABLE_NAME));

      qInstance.addTable(new QTableMetaData()
         .withName(Group.TABLE_NAME)
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withFieldsFromEntity(Group.class));
      QInstanceEnricher.setInferredFieldBackendNames(qInstance.getTable(Group.TABLE_NAME));

      qInstance.addTable(new QTableMetaData()
         .withName(Client.TABLE_NAME)
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withFieldsFromEntity(Client.class));
      QInstanceEnricher.setInferredFieldBackendNames(qInstance.getTable(Client.TABLE_NAME));

      qInstance.addPossibleValueSource(QPossibleValueSource.newForTable(User.TABLE_NAME));
      qInstance.addPossibleValueSource(QPossibleValueSource.newForTable(Group.TABLE_NAME));
      qInstance.addPossibleValueSource(QPossibleValueSource.newForTable(Client.TABLE_NAME));
      qInstance.addPossibleValueSource(QPossibleValueSource.newForTable(Asset.TABLE_NAME));
      qInstance.addPossibleValueSource(QPossibleValueSource.newForTable(Audience.TABLE_NAME));
   }

}
