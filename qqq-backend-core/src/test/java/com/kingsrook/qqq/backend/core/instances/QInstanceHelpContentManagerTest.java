/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.instances;


import java.util.List;
import java.util.Set;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.dashboard.PersonsByCreateDateBarChart;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.helpcontent.HelpContent;
import com.kingsrook.qqq.backend.core.model.helpcontent.HelpContentMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.helpcontent.HelpContentRole;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.help.HelpRole;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpRole;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for QInstanceHelpContentManager 
 *******************************************************************************/
class QInstanceHelpContentManagerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableField() throws QException
   {
      /////////////////////////////////////
      // get the instance from base test //
      /////////////////////////////////////
      QInstance qInstance = QContext.getQInstance();
      new HelpContentMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      ////////////////////////////////////////////////////////
      // first, assert there's no help content on person.id //
      ////////////////////////////////////////////////////////
      assertNoPersonIdHelp(qInstance);

      HelpContent recordEntity = new HelpContent()
         .withId(1)
         .withKey("table:person;field:id")
         .withContent("v1")
         .withRole(HelpContentRole.INSERT_SCREEN.getId());
      new InsertAction().execute(new InsertInput(HelpContent.TABLE_NAME).withRecordEntity(recordEntity));

      ///////////////////////////////////////////////////////////////////////////////////////////////
      // now - post-insert customizer should have automatically added help content to the instance //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      assertOnePersonIdHelp(qInstance, "v1", Set.of(QHelpRole.INSERT_SCREEN));

      ///////////////////////////////////////////////////
      // define a new instance - assert is empty again //
      ///////////////////////////////////////////////////
      QInstance newInstance = TestUtils.defineInstance();
      QContext.setQInstance(newInstance);
      new HelpContentMetaDataProvider().defineAll(newInstance, TestUtils.MEMORY_BACKEND_NAME, null);
      assertNoPersonIdHelp(newInstance);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now run the method that start-up (or hotswap) will run, to look up existing records and translate to meta-data //
      // then re-assert that the help is back                                                                           //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QInstanceHelpContentManager.loadHelpContent(newInstance);
      assertOnePersonIdHelp(newInstance, "v1", Set.of(QHelpRole.INSERT_SCREEN));

      ////////////////////////////////////////////////////////////////////
      // update the record's content - the meta-data should get updated //
      ////////////////////////////////////////////////////////////////////
      recordEntity.setContent("v2");
      new UpdateAction().execute(new UpdateInput(HelpContent.TABLE_NAME).withRecordEntity(recordEntity));
      assertOnePersonIdHelp(newInstance, "v2", Set.of(QHelpRole.INSERT_SCREEN));

      ////////////////////////////////////////////////////////////////////////////
      // now update the role and assert it "moves" in the meta-data as expected //
      ////////////////////////////////////////////////////////////////////////////
      recordEntity.setRole(HelpContentRole.WRITE_SCREENS.getId());
      new UpdateAction().execute(new UpdateInput(HelpContent.TABLE_NAME).withRecordEntity(recordEntity));
      assertOnePersonIdHelp(newInstance, "v2", Set.of(QHelpRole.WRITE_SCREENS));

      //////////////////////////////////////////////////////////////////////////////////////
      // now delete the record - the pre-insert should remove the help from the meta-data //
      //////////////////////////////////////////////////////////////////////////////////////
      new DeleteAction().execute(new DeleteInput(HelpContent.TABLE_NAME).withPrimaryKeys(List.of(1)));
      assertNoPersonIdHelp(newInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableSection() throws QException
   {
      /////////////////////////////////////
      // get the instance from base test //
      /////////////////////////////////////
      QInstance qInstance = QContext.getQInstance();
      new HelpContentMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      //////////////////////////////////////////////////////////
      // first, assert there's no help content on the section //
      //////////////////////////////////////////////////////////
      assertNoPersonSectionHelp(qInstance);

      HelpContent recordEntity = new HelpContent()
         .withId(1)
         .withKey("table:person;section:identity")
         .withContent("v1")
         .withRole(HelpContentRole.INSERT_SCREEN.getId());
      new InsertAction().execute(new InsertInput(HelpContent.TABLE_NAME).withRecordEntity(recordEntity));

      ///////////////////////////////////////////////////////////////////////////////////////////////
      // now - post-insert customizer should have automatically added help content to the instance //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      assertOnePersonSectionHelp(qInstance, "v1", Set.of(QHelpRole.INSERT_SCREEN));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testProcessField() throws QException
   {
      /////////////////////////////////////
      // get the instance from base test //
      /////////////////////////////////////
      QInstance qInstance = QContext.getQInstance();
      new HelpContentMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      //////////////////////////////////////////////////////////
      // first, assert there's no help content on the section //
      //////////////////////////////////////////////////////////
      assertNoGreetPersonFieldHelp(qInstance);

      HelpContent recordEntity = new HelpContent()
         .withId(1)
         .withKey("process:" + TestUtils.PROCESS_NAME_GREET_PEOPLE + ";field:greetingPrefix")
         .withContent("v1")
         .withRole(HelpContentRole.INSERT_SCREEN.getId());
      new InsertAction().execute(new InsertInput(HelpContent.TABLE_NAME).withRecordEntity(recordEntity));

      ///////////////////////////////////////////////////////////////////////////////////////////////
      // now - post-insert customizer should have automatically added help content to the instance //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      assertOneGreetPersonFieldHelp(qInstance, "v1", Set.of(QHelpRole.INSERT_SCREEN));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWidget() throws QException
   {
      /////////////////////////////////////
      // get the instance from base test //
      /////////////////////////////////////
      QInstance qInstance = QContext.getQInstance();
      new HelpContentMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      //////////////////////////////////////////////////////////
      // first, assert there's no help content on the section //
      //////////////////////////////////////////////////////////
      QWidgetMetaDataInterface widget = qInstance.getWidget(PersonsByCreateDateBarChart.class.getSimpleName());
      assertTrue(CollectionUtils.nullSafeIsEmpty(widget.getHelpContent()));

      HelpContent recordEntity = new HelpContent()
         .withId(1)
         .withKey("widget:" + widget.getName() + ";slot:label")
         .withContent("i need somebody")
         .withRole(HelpContentRole.ALL_SCREENS.getId());
      new InsertAction().execute(new InsertInput(HelpContent.TABLE_NAME).withRecordEntity(recordEntity));

      ///////////////////////////////////////////////////////////////////////////////////////////////
      // now - post-insert customizer should have automatically added help content to the instance //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      assertTrue(widget.getHelpContent().containsKey("label"));
      assertEquals("i need somebody", widget.getHelpContent().get("label").get(0).getContent());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsertedRecordReplacesHelpContentFromMetaData() throws QException
   {
      /////////////////////////////////////
      // get the instance from base test //
      /////////////////////////////////////
      QInstance qInstance = QContext.getQInstance();
      qInstance.getTable(TestUtils.TABLE_NAME_PERSON).getField("id")
         .withHelpContent(new QHelpContent().withContent("v0").withRole(QHelpRole.INSERT_SCREEN));
      new HelpContentMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      /////////////////////////////////////////////
      // assert the help from meta-data is there //
      /////////////////////////////////////////////
      assertOnePersonIdHelp(qInstance, "v0", Set.of(QHelpRole.INSERT_SCREEN));

      HelpContent recordEntity = new HelpContent()
         .withId(1)
         .withKey("table:person;field:id")
         .withContent("v1")
         .withRole(HelpContentRole.INSERT_SCREEN.getId());
      new InsertAction().execute(new InsertInput(HelpContent.TABLE_NAME).withRecordEntity(recordEntity));

      ///////////////////////////////////////////////////////////////////////////////////////////////
      // now - post-insert customizer should have automatically added help content to the instance //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      assertOnePersonIdHelp(qInstance, "v1", Set.of(QHelpRole.INSERT_SCREEN));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void assertNoPersonIdHelp(QInstance qInstance)
   {
      List<QHelpContent> helpContents = qInstance.getTable(TestUtils.TABLE_NAME_PERSON).getField("id").getHelpContents();
      assertThat(helpContents).isNullOrEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void assertOnePersonIdHelp(QInstance qInstance, String content, Set<HelpRole> roles)
   {
      List<QHelpContent> helpContents = qInstance.getTable(TestUtils.TABLE_NAME_PERSON).getField("id").getHelpContents();
      assertEquals(1, helpContents.size());
      assertEquals(content, helpContents.get(0).getContent());
      assertEquals(roles, helpContents.get(0).getRoles());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void assertNoPersonSectionHelp(QInstance qInstance)
   {
      List<QHelpContent> helpContents = qInstance.getTable(TestUtils.TABLE_NAME_PERSON).getSections()
         .stream().filter(s -> s.getName().equals("identity")).findFirst()
         .get().getHelpContents();
      assertThat(helpContents).isNullOrEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void assertOnePersonSectionHelp(QInstance qInstance, String content, Set<HelpRole> roles)
   {
      List<QHelpContent> helpContents = qInstance.getTable(TestUtils.TABLE_NAME_PERSON).getSections()
         .stream().filter(s -> s.getName().equals("identity")).findFirst()
         .get().getHelpContents();
      assertEquals(1, helpContents.size());
      assertEquals(content, helpContents.get(0).getContent());
      assertEquals(roles, helpContents.get(0).getRoles());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void assertNoGreetPersonFieldHelp(QInstance qInstance)
   {
      List<QHelpContent> helpContents = qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE).getInputFields()
         .stream().filter(f -> f.getName().equals("greetingPrefix")).findFirst()
         .get().getHelpContents();
      assertThat(helpContents).isNullOrEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void assertOneGreetPersonFieldHelp(QInstance qInstance, String content, Set<HelpRole> roles)
   {
      List<QHelpContent> helpContents = qInstance.getProcess(TestUtils.PROCESS_NAME_GREET_PEOPLE).getInputFields()
         .stream().filter(f -> f.getName().equals("greetingPrefix")).findFirst()
         .get().getHelpContents();
      assertEquals(1, helpContents.size());
      assertEquals(content, helpContents.get(0).getContent());
      assertEquals(roles, helpContents.get(0).getRoles());
   }

}