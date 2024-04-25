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


import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Asset;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.Group;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.SharedAsset;
import com.kingsrook.qqq.backend.module.rdbms.sharing.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.module.rdbms.sharing.SharingMetaDataProvider.GROUP_ID_KEY_TYPE;
import static com.kingsrook.qqq.backend.module.rdbms.sharing.SharingMetaDataProvider.USER_ID_ALL_ACCESS_KEY_TYPE;
import static com.kingsrook.qqq.backend.module.rdbms.sharing.SharingMetaDataProvider.USER_ID_KEY_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 **
 *******************************************************************************/
public class SharingTest
{
   //////////////
   // user ids //
   //////////////
   public static final int HOMER_ID       = 1;
   public static final int MARGE_ID       = 2;
   public static final int BART_ID        = 3;
   public static final int LISA_ID        = 4;
   public static final int BURNS_ID       = 5;

   ///////////////
   // group ids //
   ///////////////
   public static final int SIMPSONS_ID    = 1;
   public static final int POWER_PLANT_ID = 2;
   public static final int BOGUS_GROUP_ID = Integer.MAX_VALUE;



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
         new User().withId(HOMER_ID).withUsername("homer"),
         new User().withId(MARGE_ID).withUsername("marge"),
         new User().withId(BART_ID).withUsername("bart"),
         new User().withId(LISA_ID).withUsername("lisa"),
         new User().withId(BURNS_ID).withUsername("burns"));
      new InsertAction().execute(new InsertInput(User.TABLE_NAME).withRecordEntities(userList));

      List<QRecordEntity> groupList = List.of(
         new Group().withId(SIMPSONS_ID).withName("simpsons"),
         new Group().withId(POWER_PLANT_ID).withName("powerplant"));
      new InsertAction().execute(new InsertInput(Group.TABLE_NAME).withRecordEntities(groupList));

      List<QRecordEntity> assetList = List.of(
         new Asset().withId(1).withName("742evergreen").withUserId(HOMER_ID),
         new Asset().withId(2).withName("beer").withUserId(HOMER_ID),
         new Asset().withId(3).withName("car").withUserId(MARGE_ID),
         new Asset().withId(4).withName("skateboard").withUserId(BART_ID),
         new Asset().withId(5).withName("santaslittlehelper").withUserId(BART_ID),
         new Asset().withId(6).withName("saxamaphone").withUserId(LISA_ID),
         new Asset().withId(7).withName("radiation").withUserId(BURNS_ID));
      new InsertAction().execute(new InsertInput(Asset.TABLE_NAME).withRecordEntities(assetList));

      List<QRecordEntity> sharedAssetList = List.of(
         new SharedAsset().withAssetId(1).withGroupId(SIMPSONS_ID), // homer shares his house with the simpson family (group)
         new SharedAsset().withAssetId(3).withUserId(HOMER_ID), // marge shares a car with homer
         new SharedAsset().withAssetId(5).withGroupId(SIMPSONS_ID), // bart shares santa's little helper with the whole family
         new SharedAsset().withAssetId(7).withGroupId(POWER_PLANT_ID) // burns shares radiation with the power plant
      );
      new InsertAction().execute(new InsertInput(SharedAsset.TABLE_NAME).withRecordEntities(sharedAssetList));

      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAssetWithUserIdOnlySecurityKey() throws QException
   {
      ////////////////////////////////////////////////////////////////////
      // update the asset table to change its lock to only be on userId //
      ////////////////////////////////////////////////////////////////////
      QContext.getQInstance().getTable(Asset.TABLE_NAME)
         .withRecordSecurityLocks(List.of(new RecordSecurityLock()
            .withSecurityKeyType(USER_ID_KEY_TYPE)
            .withFieldName("userId")));

      ////////////////////////////////////////////////////////
      // with nothing in session, make sure we find nothing //
      ////////////////////////////////////////////////////////
      assertEquals(0, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      ////////////////////////////////////
      // marge direct owner only of car //
      ////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(USER_ID_KEY_TYPE, MARGE_ID);
      assertEquals(1, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      /////////////////////////////////////////////////
      // homer direct owner of 742evergreen and beer //
      /////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(USER_ID_KEY_TYPE, HOMER_ID);
      assertEquals(2, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      /////////////////////////////////////////////////////
      // marge & homer - own car, 742evergreen, and beer //
      /////////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(USER_ID_KEY_TYPE, HOMER_ID);
      QContext.getQSession().withSecurityKeyValue(USER_ID_KEY_TYPE, MARGE_ID);
      assertEquals(3, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());
   }



   /*******************************************************************************
    ** normally (?) maybe we wouldn't query sharedAsset directly (we'd instead query
    ** for asset, and understand that there's a security lock coming from sharedAsset),
    ** but this test is here as we build up making a more complex lock like that.
    *******************************************************************************/
   @Test
   void testSharedAssetDirectly() throws QException
   {
      ////////////////////////////////////////////////////////
      // with nothing in session, make sure we find nothing //
      ////////////////////////////////////////////////////////
      assertEquals(0, new QueryAction().execute(new QueryInput(SharedAsset.TABLE_NAME)).getRecords().size());

      /////////////////////////////////////
      // homer has a car shared with him //
      /////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(USER_ID_KEY_TYPE, HOMER_ID);
      assertEquals(1, new QueryAction().execute(new QueryInput(SharedAsset.TABLE_NAME)).getRecords().size());

      /////////////////////////////////////////////////////////////////////////////////////////
      // now put homer's groups in the session as well - and we should find 742evergreen too //
      /////////////////////////////////////////////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(USER_ID_KEY_TYPE, HOMER_ID);
      QContext.getQSession().withSecurityKeyValue(GROUP_ID_KEY_TYPE, SIMPSONS_ID);
      QContext.getQSession().withSecurityKeyValue(GROUP_ID_KEY_TYPE, POWER_PLANT_ID);
      List<QRecord> records = new QueryAction().execute(new QueryInput(SharedAsset.TABLE_NAME)).getRecords();
      assertEquals(4, records.size());
   }


   /*******************************************************************************
    ** real-world use-case (e.g., why sharing concept exists) - query the asset table
    **
    *******************************************************************************/
   @Test
   void testAssetsWithLockThroughSharing() throws QException, SQLException
   {
      ////////////////////////////////////////////////////////
      // with nothing in session, make sure we find nothing //
      ////////////////////////////////////////////////////////
      assertEquals(0, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // homer has a car shared with him and 2 things he owns himself - so w/ only his userId in session (and no groups), should find those 3 //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(USER_ID_KEY_TYPE, HOMER_ID);
      assertEquals(3, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      //////////////////////////////////////////////////////////////////////
      // add a group that matches nothing now, just to ensure same result //
      //////////////////////////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValue(GROUP_ID_KEY_TYPE, BOGUS_GROUP_ID);
      assertEquals(3, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now put homer's groups in the session as well - and we should find the 3 from above, plus a shared family asset and shared power-plant asset //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(USER_ID_KEY_TYPE, HOMER_ID);
      QContext.getQSession().withSecurityKeyValue(GROUP_ID_KEY_TYPE, SIMPSONS_ID);
      QContext.getQSession().withSecurityKeyValue(GROUP_ID_KEY_TYPE, POWER_PLANT_ID);
      assertEquals(5, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAllAccessKeys() throws QException
   {
      ///////////////////////////////////////////////////////////////
      // with user-id all access key, should get all asset records //
      ///////////////////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(new HashMap<>());
      QContext.getQSession().withSecurityKeyValue(USER_ID_ALL_ACCESS_KEY_TYPE, true);
      assertEquals(7, new QueryAction().execute(new QueryInput(Asset.TABLE_NAME)).getRecords().size());
   }

}
