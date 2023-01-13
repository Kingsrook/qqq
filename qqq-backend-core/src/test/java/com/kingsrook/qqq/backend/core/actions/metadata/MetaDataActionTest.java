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

package com.kingsrook.qqq.backend.core.actions.metadata;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNodeType;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendReportMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.DenyBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for MetaDataAction
 **
 *******************************************************************************/
class MetaDataActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      MetaDataInput request = new MetaDataInput(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      MetaDataOutput result = new MetaDataAction().execute(request);
      assertNotNull(result);

      ///////////////////////////////////
      // assert against the tables map //
      ///////////////////////////////////
      assertNotNull(result.getTables());
      assertNotNull(result.getTables().get("person"));
      assertEquals("person", result.getTables().get("person").getName());
      assertEquals("Person", result.getTables().get("person").getLabel());

      //////////////////////////////////////
      // assert against the processes map //
      //////////////////////////////////////
      assertNotNull(result.getProcesses().get("greet"));
      assertNotNull(result.getProcesses().get("greetInteractive"));
      assertNotNull(result.getProcesses().get("etl.basic"));
      assertNotNull(result.getProcesses().get("person.bulkInsert"));
      assertNotNull(result.getProcesses().get("person.bulkEdit"));
      assertNotNull(result.getProcesses().get("person.bulkDelete"));

      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      // assert against the apps map - which is appName to app - but not fully hierarchical - that's appTree //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      Map<String, QFrontendAppMetaData> apps = result.getApps();
      assertNotNull(apps.get(TestUtils.APP_NAME_GREETINGS));
      assertNotNull(apps.get(TestUtils.APP_NAME_PEOPLE));
      assertNotNull(apps.get(TestUtils.APP_NAME_MISCELLANEOUS));

      QFrontendAppMetaData peopleApp = apps.get(TestUtils.APP_NAME_PEOPLE);
      assertThat(peopleApp.getChildren()).isNotEmpty();
      Optional<AppTreeNode> greetingsAppUnderPeopleFromMapOptional = peopleApp.getChildren().stream()
         .filter(e -> e.getName().equals(TestUtils.APP_NAME_GREETINGS)).findFirst();
      assertThat(greetingsAppUnderPeopleFromMapOptional).isPresent();

      //////////////////////////////////////////////////////////////////////////////
      // we want to show that in the appMap (e.g., "apps"), that the apps are not //
      // hierarchical - that is - that a sub-app doesn't list ITS children here.  //
      //////////////////////////////////////////////////////////////////////////////
      assertThat(greetingsAppUnderPeopleFromMapOptional.get().getChildren()).isNullOrEmpty();

      ///////////////////////////////////////////////
      // assert against the hierarchical apps tree //
      ///////////////////////////////////////////////
      List<AppTreeNode> appTree             = result.getAppTree();
      Set<String>       appNamesInTopOfTree = appTree.stream().map(AppTreeNode::getName).collect(Collectors.toSet());
      assertThat(appNamesInTopOfTree).contains(TestUtils.APP_NAME_PEOPLE);
      assertThat(appNamesInTopOfTree).contains(TestUtils.APP_NAME_MISCELLANEOUS);
      assertThat(appNamesInTopOfTree).doesNotContain(TestUtils.APP_NAME_GREETINGS);

      Optional<AppTreeNode> peopleAppOptional = appTree.stream()
         .filter(e -> e.getName().equals(TestUtils.APP_NAME_PEOPLE)).findFirst();
      assertThat(peopleAppOptional).isPresent();
      assertThat(peopleAppOptional.get().getChildren()).isNotEmpty();

      Optional<AppTreeNode> greetingsAppUnderPeopleFromTree = peopleAppOptional.get().getChildren().stream()
         .filter(e -> e.getName().equals(TestUtils.APP_NAME_GREETINGS)).findFirst();
      assertThat(greetingsAppUnderPeopleFromTree).isPresent();

      /////////////////////////////////////////////////////////////////////////////////
      // but here, when this app comes from the tree, then it DOES have its children //
      /////////////////////////////////////////////////////////////////////////////////
      assertThat(greetingsAppUnderPeopleFromTree.get().getChildren()).isNotEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testHasAccessNoPermissionAndHide() throws QException
   {
      ///////////////////////////////////////////////////////////////////////////////////////////
      // with 'hasAccess' set as the default instance rule, but no permissions in the session, //
      // and the deny behavior as 'hide' we should have 0 of these                             //
      ///////////////////////////////////////////////////////////////////////////////////////////
      QInstance instance = TestUtils.defineInstance();
      instance.setDefaultPermissionRules(new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION));
      MetaDataInput input = new MetaDataInput(instance);
      input.setSession(new QSession());
      MetaDataOutput result = new MetaDataAction().execute(input);

      assertEquals(0, result.getTables().size());
      assertEquals(0, result.getProcesses().size());
      assertEquals(0, result.getReports().size());
      assertEquals(0, result.getWidgets().size());
      assertEquals(0, result.getApps().size());
      assertEquals(0, result.getAppTree().size());

      ////////////////////////////////////////////////////////////////////////////////////////////////////////
      // the only kinds of app meta data we should find are other apps - no tables, processes, reports, etc //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(QFrontendAppMetaData appMetaData : result.getApps().values())
      {
         assertThat(appMetaData.getClass()).isEqualTo(QFrontendAppMetaData.class);
         for(AppTreeNode child : appMetaData.getChildren())
         {
            assertEquals(AppTreeNodeType.APP, child.getType());
         }
      }

      List<AppTreeNode> toExplore = new ArrayList<>(result.getAppTree());
      while(!toExplore.isEmpty())
      {
         AppTreeNode exploring = toExplore.remove(0);
         if(exploring.getChildren() != null)
         {
            toExplore.addAll(exploring.getChildren());
         }
         assertEquals(AppTreeNodeType.APP, exploring.getType());
      }

      // todo -- assert about sections in those apps not having stuff
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testHasAccessNoPermissionAndDisable() throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      // with 'hasAccess' set as the default instance rule, but no permissions in the session,           //
      // and the deny behavior as 'disable', we should have lots of things, but all with no permissions. //
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      QInstance instance = TestUtils.defineInstance();
      instance.setDefaultPermissionRules(new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION).withDenyBehavior(DenyBehavior.DISABLED));
      MetaDataInput input = new MetaDataInput(instance);
      input.setSession(new QSession());
      MetaDataOutput result = new MetaDataAction().execute(input);

      assertNotEquals(0, result.getTables().size());
      assertNotEquals(0, result.getProcesses().size());
      assertNotEquals(0, result.getReports().size());
      assertNotEquals(0, result.getWidgets().size());
      assertNotEquals(0, result.getApps().size());
      assertNotEquals(0, result.getAppTree().size());

      assertTrue(result.getTables().values().stream().allMatch(t -> !t.getDeletePermission() && !t.getReadPermission() && !t.getInsertPermission() && !t.getEditPermission()));
      assertTrue(result.getProcesses().values().stream().noneMatch(QFrontendProcessMetaData::getHasPermission));
      assertTrue(result.getReports().values().stream().noneMatch(QFrontendReportMetaData::getHasPermission));
      assertTrue(result.getWidgets().values().stream().noneMatch(QFrontendWidgetMetaData::getHasPermission));
      // todo ... apps...  uh...
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testHasAccessSomePermissionsAndHide() throws QException
   {
      QInstance instance = TestUtils.defineInstance();
      instance.setDefaultPermissionRules(new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION));
      MetaDataInput input = new MetaDataInput(instance);
      input.setSession(new QSession().withPermissions(
         "person.hasAccess",
         "increaseBirthdate.hasAccess",
         "runShapesPersonReport.hasAccess",
         "shapesPersonReport.hasAccess",
         "personJoinShapeReport.hasAccess",
         "simplePersonReport.hasAccess",
         "PersonsByCreateDateBarChart.hasAccess"
      ));
      MetaDataOutput result = new MetaDataAction().execute(input);

      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      // with several permissions set, we should see some things, and they should have permissions turned on //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertEquals(Set.of("person"), result.getTables().keySet());
      assertEquals(Set.of("increaseBirthdate", "runShapesPersonReport", "person.bulkInsert", "person.bulkEdit", "person.bulkDelete"), result.getProcesses().keySet());
      assertEquals(Set.of("shapesPersonReport", "personJoinShapeReport", "simplePersonReport"), result.getReports().keySet());
      assertEquals(Set.of("PersonsByCreateDateBarChart"), result.getWidgets().keySet());

      assertTrue(result.getTables().values().stream().allMatch(t -> t.getDeletePermission() && t.getReadPermission() && t.getInsertPermission() && t.getEditPermission()));
      assertTrue(result.getProcesses().values().stream().allMatch(QFrontendProcessMetaData::getHasPermission));
      assertTrue(result.getReports().values().stream().allMatch(QFrontendReportMetaData::getHasPermission));
      assertTrue(result.getWidgets().values().stream().allMatch(QFrontendWidgetMetaData::getHasPermission));

      // todo -- assert about apps & sections in those apps having just the right stuff
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testTableReadWritePermissions() throws QException
   {
      QInstance instance = TestUtils.defineInstance();
      instance.setDefaultPermissionRules(new QPermissionRules().withLevel(PermissionLevel.READ_WRITE_PERMISSIONS));
      MetaDataInput input = new MetaDataInput(instance);
      input.setSession(new QSession().withPermissions(
         "person.read",
         "personFile.write",
         "personMemory.read",
         "personMemory.write",
         "personMemoryCache.hasAccess", // this one should NOT come through.
         "increaseBirthdate.hasAccess"
      ));
      MetaDataOutput result = new MetaDataAction().execute(input);

      assertEquals(Set.of("person", "personFile", "personMemory"), result.getTables().keySet());

      assertEquals(Set.of("increaseBirthdate", "personFile.bulkInsert", "personFile.bulkEdit", "personFile.bulkDelete", "personMemory.bulkInsert", "personMemory.bulkEdit", "personMemory.bulkDelete"), result.getProcesses().keySet());
      assertEquals(Set.of(), result.getReports().keySet());
      assertEquals(Set.of(), result.getWidgets().keySet());

      QFrontendTableMetaData personTable = result.getTables().get("person");
      assertTrue(personTable.getReadPermission());
      assertFalse(personTable.getInsertPermission());
      assertFalse(personTable.getEditPermission());
      assertFalse(personTable.getDeletePermission());

      QFrontendTableMetaData personFileTable = result.getTables().get("personFile");
      assertFalse(personFileTable.getReadPermission());
      assertTrue(personFileTable.getInsertPermission());
      assertTrue(personFileTable.getEditPermission());
      assertTrue(personFileTable.getDeletePermission());

      QFrontendTableMetaData personMemoryTable = result.getTables().get("personMemory");
      assertTrue(personMemoryTable.getReadPermission());
      assertTrue(personMemoryTable.getInsertPermission());
      assertTrue(personMemoryTable.getEditPermission());
      assertTrue(personMemoryTable.getDeletePermission());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testTableReadInsertEditDeletePermissions() throws QException
   {
      QInstance instance = TestUtils.defineInstance();
      instance.setDefaultPermissionRules(new QPermissionRules().withLevel(PermissionLevel.READ_INSERT_EDIT_DELETE_PERMISSIONS));
      MetaDataInput input = new MetaDataInput(instance);
      input.setSession(new QSession().withPermissions(
         "person.read",
         "personFile.insert",
         "personFile.edit",
         "personMemory.read",
         "personMemory.delete",
         "personMemoryCache.hasAccess", // this one should NOT come through.
         "increaseBirthdate.hasAccess"
      ));
      MetaDataOutput result = new MetaDataAction().execute(input);

      assertEquals(Set.of("person", "personFile", "personMemory"), result.getTables().keySet());
      assertEquals(Set.of("increaseBirthdate", "personFile.bulkInsert", "personFile.bulkEdit", "personMemory.bulkDelete"), result.getProcesses().keySet());
      assertEquals(Set.of(), result.getReports().keySet());
      assertEquals(Set.of(), result.getWidgets().keySet());

      QFrontendTableMetaData personTable = result.getTables().get("person");
      assertTrue(personTable.getReadPermission());
      assertFalse(personTable.getInsertPermission());
      assertFalse(personTable.getEditPermission());
      assertFalse(personTable.getDeletePermission());

      QFrontendTableMetaData personFileTable = result.getTables().get("personFile");
      assertFalse(personFileTable.getReadPermission());
      assertTrue(personFileTable.getInsertPermission());
      assertTrue(personFileTable.getEditPermission());
      assertFalse(personFileTable.getDeletePermission());

      QFrontendTableMetaData personMemoryTable = result.getTables().get("personMemory");
      assertTrue(personMemoryTable.getReadPermission());
      assertFalse(personMemoryTable.getInsertPermission());
      assertFalse(personMemoryTable.getEditPermission());
      assertTrue(personMemoryTable.getDeletePermission());
   }

}
