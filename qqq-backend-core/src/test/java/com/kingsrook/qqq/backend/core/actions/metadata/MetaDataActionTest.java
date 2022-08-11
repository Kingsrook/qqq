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


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendAppMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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
}
