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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Asset;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.AssetAudienceInt;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Audience;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Group;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 **
 *******************************************************************************/
public class SharingTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws Exception
   {
      TestUtils.primeTestDatabase("prime-test-database-sharing-test.sql");

      QInstance qInstance = TestUtils.defineInstance();
      SharingMetaDataProvider.defineAll(qInstance);

      QContext.init(qInstance, new QSession());

      loadData();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void loadData() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(SharingMetaDataProvider.USER_ID_ALL_ACCESS_KEY_TYPE, true);

      List<QRecordEntity> userList = List.of(
         new User().withId(100).withUsername("homer"),
         new User().withId(101).withUsername("marge"),
         new User().withId(102).withUsername("bart"),
         new User().withId(103).withUsername("lisa"),
         new User().withId(110).withUsername("burns"));
      new InsertAction().execute(new InsertInput(User.TABLE_NAME).withRecordEntities(userList));

      List<QRecordEntity> groupList = List.of(
         new Group().withId(200).withName("simpsons"),
         new Group().withId(201).withName("powerplant"));
      new InsertAction().execute(new InsertInput(Group.TABLE_NAME).withRecordEntities(groupList));

      List<QRecordEntity> assetList = List.of(
         new Asset().withId(3000).withName("742evergreen").withUserId(100),
         new Asset().withId(3001).withName("beer").withUserId(100),
         new Asset().withId(3010).withName("bed").withUserId(101),
         new Asset().withId(3020).withName("skateboard").withUserId(102),
         new Asset().withId(3030).withName("saxamaphone").withUserId(103));
      new InsertAction().execute(new InsertInput(Asset.TABLE_NAME).withRecordEntities(assetList));

      List<QRecordEntity> assetAudienceIntList = List.of(
         // homer shares his house with the simpson family (group)
         new AssetAudienceInt().withAssetId(3000).withAudienceId(200),

         // marge shares a bed with homer
         new AssetAudienceInt().withAssetId(3010).withAudienceId(100)

      );
      new InsertAction().execute(new InsertInput(AssetAudienceInt.TABLE_NAME).withRecordEntities(assetAudienceIntList));

      List<QRecordEntity> audienceList = new ArrayList<>();
      for(QRecordEntity entity : userList)
      {
         User user = (User) entity;
         audienceList.add(new Audience().withId(user.getId()).withName(user.getUsername()).withType("user"));
      }
      for(QRecordEntity entity : groupList)
      {
         Group group = (Group) entity;
         audienceList.add(new Audience().withId(group.getId()).withName(group.getName()).withType("group"));
      }
      new InsertAction().execute(new InsertInput(Audience.TABLE_NAME).withRecordEntities(audienceList));

      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      assertEquals(0, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(SharingMetaDataProvider.USER_ID_KEY_TYPE, 101);
      assertEquals(1, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(SharingMetaDataProvider.USER_ID_KEY_TYPE, 100);
      assertEquals(2, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(SharingMetaDataProvider.USER_ID_KEY_TYPE, 100);
      QContext.getQSession().withSecurityKeyValue(SharingMetaDataProvider.USER_ID_KEY_TYPE, 101);
      assertEquals(3, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());
   }

}
