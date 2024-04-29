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


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Asset;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Client;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Group;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.SharedAsset;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.User;


/*******************************************************************************
 **
 *******************************************************************************/
public class SharingMetaDataProvider
{
   public static final String USER_ID_KEY_TYPE            = "userIdKey";
   public static final String USER_ID_ALL_ACCESS_KEY_TYPE = "userIdAllAccessKey";

   public static final String GROUP_ID_KEY_TYPE            = "groupIdKey";
   public static final String GROUP_ID_ALL_ACCESS_KEY_TYPE = "groupIdAllAccessKey";

   private static final String ASSET_JOIN_SHARED_ASSET = "assetJoinSharedAsset";
   private static final String SHARED_ASSET_JOIN_ASSET = "sharedAssetJoinAsset";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void defineAll(QInstance qInstance) throws QException
   {
      qInstance.addSecurityKeyType(new QSecurityKeyType()
         .withName(USER_ID_KEY_TYPE)
         .withAllAccessKeyName(USER_ID_ALL_ACCESS_KEY_TYPE));

      qInstance.addSecurityKeyType(new QSecurityKeyType()
         .withName(GROUP_ID_KEY_TYPE)
         .withAllAccessKeyName(GROUP_ID_ALL_ACCESS_KEY_TYPE));

      qInstance.addTable(new QTableMetaData()
         .withName(Asset.TABLE_NAME)
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withBackendDetails(new RDBMSTableBackendDetails().withTableName("asset"))
         .withFieldsFromEntity(Asset.class)

         ////////////////////////////////////////
         // This is original - just owner/user //
         ////////////////////////////////////////
         // .withRecordSecurityLock(new RecordSecurityLock()
         //    .withSecurityKeyType(USER_ID_KEY_TYPE)
         //    .withFieldName("userId")));

         .withRecordSecurityLock(new MultiRecordSecurityLock()
            .withOperator(MultiRecordSecurityLock.BooleanOperator.OR)
            .withLock(new RecordSecurityLock()
               .withSecurityKeyType(USER_ID_KEY_TYPE)
               .withFieldName("userId"))
            .withLock(new RecordSecurityLock()
               .withSecurityKeyType(USER_ID_KEY_TYPE)
               .withFieldName(SharedAsset.TABLE_NAME + ".userId")
               .withJoinNameChain(List.of(SHARED_ASSET_JOIN_ASSET)))
            .withLock(new RecordSecurityLock()
               .withSecurityKeyType(GROUP_ID_KEY_TYPE)
               .withFieldName(SharedAsset.TABLE_NAME + ".groupId")
               .withJoinNameChain(List.of(SHARED_ASSET_JOIN_ASSET)))
         ));
      QInstanceEnricher.setInferredFieldBackendNames(qInstance.getTable(Asset.TABLE_NAME));

      qInstance.addTable(new QTableMetaData()
         .withName(SharedAsset.TABLE_NAME)
         .withBackendDetails(new RDBMSTableBackendDetails().withTableName("shared_asset"))
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withFieldsFromEntity(SharedAsset.class)
         .withRecordSecurityLock(new MultiRecordSecurityLock()
            .withOperator(MultiRecordSecurityLock.BooleanOperator.OR)
            .withLock(new RecordSecurityLock()
               .withSecurityKeyType(USER_ID_KEY_TYPE)
               .withFieldName("userId"))
            .withLock(new RecordSecurityLock()
               .withSecurityKeyType(GROUP_ID_KEY_TYPE)
               .withFieldName("groupId"))
         ));
      QInstanceEnricher.setInferredFieldBackendNames(qInstance.getTable(SharedAsset.TABLE_NAME));

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

      qInstance.addJoin(new QJoinMetaData()
         .withName(ASSET_JOIN_SHARED_ASSET)
         .withLeftTable(Asset.TABLE_NAME)
         .withRightTable(SharedAsset.TABLE_NAME)
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "assetId"))
      );

      qInstance.addJoin(new QJoinMetaData()
         .withName(SHARED_ASSET_JOIN_ASSET)
         .withLeftTable(SharedAsset.TABLE_NAME)
         .withRightTable(Asset.TABLE_NAME)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("assetId", "id"))
      );
   }

}
