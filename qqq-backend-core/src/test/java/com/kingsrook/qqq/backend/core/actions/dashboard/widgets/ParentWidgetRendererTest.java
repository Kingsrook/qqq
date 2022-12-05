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
import com.kingsrook.qqq.backend.core.actions.dashboard.RenderWidgetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChildRecordListData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ParentWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.ParentWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for ChildRecordListRenderer
 *******************************************************************************/
class ParentWidgetRendererTest
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
   void testParentWidget() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      QWidgetMetaDataInterface parcelRulesWidget = new ParentWidgetMetaData()
         .withTitle("Parcel Rules")
         .withPossibleValueNameList(
            List.of(
               TestUtils.defineStatesPossibleValueSource().getName(),
               TestUtils.defineShapePossibleValueSource().getName()
            )
         )
         .withChildWidgetNameList(
            List.of(
               ProcessWidgetRenderer.class.getSimpleName()
            )
         )
         .withType(WidgetType.PARENT_WIDGET.getType())
         .withName(ProcessWidgetRenderer.class.getSimpleName())
         .withGridColumns(12)
         .withLabel("Test Parent Widget")
         .withCodeReference(new QCodeReference(ParentWidgetRenderer.class, null))
         .withIcon("local_shipping");
      qInstance.addWidget(parcelRulesWidget);

      RenderWidgetInput input = new RenderWidgetInput(qInstance);
      input.setSession(new QSession());
      input.setWidgetMetaData(parcelRulesWidget);

      RenderWidgetAction renderWidgetAction = new RenderWidgetAction();
      RenderWidgetOutput output             = renderWidgetAction.execute(input);

      assertThat(output.getWidgetData()).isNotNull();
      ParentWidgetData parentWidgetData = (ParentWidgetData) output.getWidgetData();
      assertThat(parentWidgetData.getDropdownDataList().size()).isEqualTo(2);
      assertThat(parentWidgetData.getChildWidgetNameList().size()).isEqualTo(1);
      assertThat(parentWidgetData.getType()).isEqualTo(WidgetType.PARENT_WIDGET.getType());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoChildRecordsFound() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(qInstance.getJoin("orderLineItem"))
         .withLabel("Line Items")
         .getWidgetMetaData();
      qInstance.addWidget(widget);

      TestUtils.insertRecords(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_ORDER), List.of(
         new QRecord().withValue("id", 1)
      ));

      RenderWidgetInput input = new RenderWidgetInput(qInstance);
      input.setSession(new QSession());
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
      QInstance qInstance = TestUtils.defineInstance();
      QWidgetMetaData widget = ChildRecordListRenderer.widgetMetaDataBuilder(qInstance.getJoin("orderLineItem"))
         .withLabel("Line Items")
         .getWidgetMetaData();
      qInstance.addWidget(widget);

      TestUtils.insertRecords(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_ORDER), List.of(
         new QRecord().withValue("id", 1),
         new QRecord().withValue("id", 2)
      ));

      TestUtils.insertRecords(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_LINE_ITEM), List.of(
         new QRecord().withValue("orderId", 1).withValue("sku", "ABC").withValue("lineNumber", 2),
         new QRecord().withValue("orderId", 1).withValue("sku", "BCD").withValue("lineNumber", 1),
         new QRecord().withValue("orderId", 2).withValue("sku", "XYZ") // should not be found.
      ));

      RenderWidgetInput input = new RenderWidgetInput(qInstance);
      input.setSession(new QSession());
      input.setWidgetMetaData(widget);
      input.setQueryParams(new HashMap<>(Map.of("id", "1")));

      RenderWidgetAction renderWidgetAction = new RenderWidgetAction();
      RenderWidgetOutput output             = renderWidgetAction.execute(input);

      ChildRecordListData childRecordListData = (ChildRecordListData) output.getWidgetData();
      assertThat(childRecordListData.getChildTableMetaData()).hasFieldOrPropertyWithValue("name", TestUtils.TABLE_NAME_LINE_ITEM);
      assertThat(childRecordListData.getQueryOutput().getRecords()).hasSize(2);
      assertThat(childRecordListData.getQueryOutput().getRecords().get(0).getValueString("sku")).isEqualTo("BCD");
      assertThat(childRecordListData.getQueryOutput().getRecords().get(1).getValueString("sku")).isEqualTo("ABC");
   }

}
