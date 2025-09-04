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

package com.kingsrook.qqq.backend.core.actions.dashboard.widgets;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.dashboard.RenderWidgetAction;
import com.kingsrook.qqq.backend.core.actions.metadata.personalization.ExamplePersonalizer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChildRecordListData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ChildRecordListRenderer
 *******************************************************************************/
class ChildRecordListRendererTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testParentRecordNotFound() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(qInstance.getJoin("orderLineItem"))
         .withLabel("Line Items")
         .getWidgetMetaData();
      qInstance.addWidget(widget);

      RenderWidgetInput input = new RenderWidgetInput();
      input.setWidgetMetaData(widget);
      input.setQueryParams(new HashMap<>(Map.of("id", "1")));

      RenderWidgetAction renderWidgetAction = new RenderWidgetAction();
      assertThatThrownBy(() -> renderWidgetAction.execute(input))
         .isInstanceOf(QNotFoundException.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoChildRecordsFound() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);
      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(qInstance.getJoin("orderLineItem"))
         .withLabel("Line Items")
         .getWidgetMetaData();
      qInstance.addWidget(widget);

      TestUtils.insertRecords(qInstance.getTable(TestUtils.TABLE_NAME_ORDER), List.of(
         new QRecord().withValue("id", 1)
      ));

      RenderWidgetInput input = new RenderWidgetInput();
      input.setWidgetMetaData(widget);
      input.setQueryParams(new HashMap<>(Map.of("id", "1")));

      RenderWidgetAction renderWidgetAction = new RenderWidgetAction();
      RenderWidgetOutput output             = renderWidgetAction.execute(input);

      ChildRecordListData childRecordListData = (ChildRecordListData) output.getWidgetData();
      assertThat(childRecordListData.getChildTableMetaData()).hasFieldOrPropertyWithValue("name", TestUtils.TABLE_NAME_LINE_ITEM);
      assertThat(childRecordListData.getQueryOutput().getRecords()).isEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testChildRecordsFound() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);
      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(qInstance.getJoin("orderLineItem"))
         .withLabel("Line Items")
         .getWidgetMetaData();
      qInstance.addWidget(widget);

      insertTwoOrdersAndThreeLines(qInstance);

      RenderWidgetInput input = new RenderWidgetInput();
      input.setWidgetMetaData(widget);
      input.setQueryParams(new HashMap<>(Map.of("id", "1")));

      RenderWidgetAction renderWidgetAction = new RenderWidgetAction();
      RenderWidgetOutput output             = renderWidgetAction.execute(input);

      ChildRecordListData childRecordListData = (ChildRecordListData) output.getWidgetData();
      assertThat(childRecordListData.getChildTableMetaData()).hasFieldOrPropertyWithValue("name", TestUtils.TABLE_NAME_LINE_ITEM);
      assertThat(childRecordListData.getQueryOutput().getRecords()).hasSize(2);
      assertThat(childRecordListData.getQueryOutput().getRecords().get(0).getValueString("sku")).isEqualTo("BCD");
      assertThat(childRecordListData.getQueryOutput().getRecords().get(1).getValueString("sku")).isEqualTo("ABC");

      ////////////////////////////////////////////////////////////////////////////////////////////////////
      // order id, being the join field, should implicitly be omitted - and we asked to omit lineNumber //
      ////////////////////////////////////////////////////////////////////////////////////////////////////
      assertTrue(childRecordListData.getOmitFieldNames().contains("orderId"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOmitFields() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);
      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(qInstance.getJoin("orderLineItem"))
         .withLabel("Line Items")
         .withOmitFieldNames(List.of("lineNumber"))
         .getWidgetMetaData();
      qInstance.addWidget(widget);

      insertTwoOrdersAndThreeLines(qInstance);

      RenderWidgetInput input = new RenderWidgetInput();
      input.setWidgetMetaData(widget);
      input.setQueryParams(new HashMap<>(Map.of("id", "1")));

      RenderWidgetAction renderWidgetAction = new RenderWidgetAction();
      RenderWidgetOutput output             = renderWidgetAction.execute(input);

      ChildRecordListData childRecordListData = (ChildRecordListData) output.getWidgetData();
      assertThat(childRecordListData.getChildTableMetaData()).hasFieldOrPropertyWithValue("name", TestUtils.TABLE_NAME_LINE_ITEM);
      assertThat(childRecordListData.getQueryOutput().getRecords()).hasSize(2);

      ///////////////////////////////////////////////////////////////////////////
      // we still get the data - it just includes a list of omitFieldNames now //
      ///////////////////////////////////////////////////////////////////////////
      assertThat(childRecordListData.getQueryOutput().getRecords().get(0).getValue("orderId")).isNotNull();

      ////////////////////////////////////////////////////////////////////////////////////////////////////
      // order id, being the join field, should implicitly be omitted - and we asked to omit lineNumber //
      ////////////////////////////////////////////////////////////////////////////////////////////////////
      assertTrue(childRecordListData.getOmitFieldNames().contains("orderId"));
      assertTrue(childRecordListData.getOmitFieldNames().contains("lineNumber"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPersonalization() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);
      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(qInstance.getJoin("orderLineItem"))
         .withLabel("Line Items")
         .getWidgetMetaData();
      qInstance.addWidget(widget);

      insertTwoOrdersAndThreeLines(qInstance);

      RenderWidgetInput input = new RenderWidgetInput();
      input.setWidgetMetaData(widget);
      input.setQueryParams(new HashMap<>(Map.of("id", "1")));

      RenderWidgetAction  renderWidgetAction  = new RenderWidgetAction();
      RenderWidgetOutput  output              = renderWidgetAction.execute(input);
      ChildRecordListData childRecordListData = (ChildRecordListData) output.getWidgetData();

      //////////////////////////////////////////////////////
      // by default make sure we get the lineNumber field //
      //////////////////////////////////////////////////////
      assertThat(childRecordListData.getQueryOutput().getRecords()).hasSize(2);
      assertThat(childRecordListData.getQueryOutput().getRecords()).allMatch(record -> record.getValue("lineNumber") != null);

      ////////////////////////////////////////////////////
      // now personalize the table to remove that field //
      ////////////////////////////////////////////////////
      String userId = "jdoe";
      ExamplePersonalizer.registerInQInstance();
      ExamplePersonalizer.addCustomizableTable(TestUtils.TABLE_NAME_LINE_ITEM);
      ExamplePersonalizer.addFieldToRemoveForUserId(TestUtils.TABLE_NAME_LINE_ITEM, "lineNumber", userId);
      QContext.getQSession().getUser().setIdReference(userId);

      //////////////////////////////////////
      // re-run and assert no lineNumbers //
      //////////////////////////////////////
      renderWidgetAction = new RenderWidgetAction();
      output = renderWidgetAction.execute(input);
      childRecordListData = (ChildRecordListData) output.getWidgetData();

      assertThat(childRecordListData.getQueryOutput().getRecords()).hasSize(2);
      assertThat(childRecordListData.getQueryOutput().getRecords()).allMatch(record -> !record.getValues().containsKey("lineNumber"));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static void insertTwoOrdersAndThreeLines(QInstance qInstance) throws QException
   {
      TestUtils.insertRecords(qInstance.getTable(TestUtils.TABLE_NAME_ORDER), List.of(
         new QRecord().withValue("id", 1),
         new QRecord().withValue("id", 2)
      ));

      TestUtils.insertRecords(qInstance.getTable(TestUtils.TABLE_NAME_LINE_ITEM), List.of(
         new QRecord().withValue("orderId", 1).withValue("sku", "ABC").withValue("lineNumber", 2),
         new QRecord().withValue("orderId", 1).withValue("sku", "BCD").withValue("lineNumber", 1),
         new QRecord().withValue("orderId", 2).withValue("sku", "XYZ")
      ));
   }

}