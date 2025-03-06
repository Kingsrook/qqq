/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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


import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChildRecordListData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for RecordListWidgetRenderer 
 *******************************************************************************/
class RecordListWidgetRendererTest extends BaseTest
{

   /***************************************************************************
    **
    ***************************************************************************/
   private QWidgetMetaData defineWidget()
   {
      return RecordListWidgetRenderer.widgetMetaDataBuilder("testRecordListWidget")
         .withTableName(TestUtils.TABLE_NAME_SHAPE)
         .withMaxRows(20)
         .withLabel("Some Shapes")
         .withFilter(new QQueryFilter()
            .withCriteria("id", QCriteriaOperator.LESS_THAN_OR_EQUALS, "${input.maxShapeId}")
            .withCriteria("name", QCriteriaOperator.NOT_EQUALS, "Square")
            .withOrderBy(new QFilterOrderBy("id", false))
         ).getWidgetMetaData();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidation() throws QInstanceValidationException
   {
      {
         QInstance       qInstance      = TestUtils.defineInstance();
         QWidgetMetaData widgetMetaData = defineWidget();
         widgetMetaData.getDefaultValues().remove("tableName");
         qInstance.addWidget(widgetMetaData);

         assertThatThrownBy(() -> new QInstanceValidator().validate(qInstance))
            .isInstanceOf(QInstanceValidationException.class)
            .hasMessageContaining("defaultValue for tableName must be given");
      }

      {
         QInstance       qInstance      = TestUtils.defineInstance();
         QWidgetMetaData widgetMetaData = defineWidget();
         widgetMetaData.getDefaultValues().remove("filter");
         qInstance.addWidget(widgetMetaData);

         assertThatThrownBy(() -> new QInstanceValidator().validate(qInstance))
            .isInstanceOf(QInstanceValidationException.class)
            .hasMessageContaining("defaultValue for filter must be given");
      }

      {
         QInstance       qInstance      = TestUtils.defineInstance();
         QWidgetMetaData widgetMetaData = defineWidget();
         widgetMetaData.getDefaultValues().remove("tableName");
         widgetMetaData.getDefaultValues().remove("filter");
         qInstance.addWidget(widgetMetaData);

         assertThatThrownBy(() -> new QInstanceValidator().validate(qInstance))
            .isInstanceOf(QInstanceValidationException.class)
            .hasMessageContaining("defaultValue for filter must be given")
            .hasMessageContaining("defaultValue for tableName must be given");
      }

      {
         QInstance       qInstance      = TestUtils.defineInstance();
         QWidgetMetaData widgetMetaData = defineWidget();
         QQueryFilter    filter         = (QQueryFilter) widgetMetaData.getDefaultValues().get("filter");
         filter.addCriteria(new QFilterCriteria("noField", QCriteriaOperator.EQUALS, "noValue"));
         qInstance.addWidget(widgetMetaData);

         assertThatThrownBy(() -> new QInstanceValidator().validate(qInstance))
            .isInstanceOf(QInstanceValidationException.class)
            .hasMessageContaining("Criteria fieldName noField is not a field in this table");
      }

      {
         QInstance       qInstance      = TestUtils.defineInstance();
         QWidgetMetaData widgetMetaData = defineWidget();
         qInstance.addWidget(widgetMetaData);

         //////////////////////////////////
         // make sure valid setup passes //
         //////////////////////////////////
         new QInstanceValidator().validate(qInstance);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRender() throws QException
   {
      QInstance       qInstance      = TestUtils.defineInstance();
      QWidgetMetaData widgetMetaData = defineWidget();
      qInstance.addWidget(widgetMetaData);

      TestUtils.insertDefaultShapes(qInstance);
      TestUtils.insertExtraShapes(qInstance);

      {
         RecordListWidgetRenderer recordListWidgetRenderer = new RecordListWidgetRenderer();
         RenderWidgetInput        input                    = new RenderWidgetInput();
         input.setWidgetMetaData(widgetMetaData);
         input.setQueryParams(Map.of("maxShapeId", "1"));
         RenderWidgetOutput output = recordListWidgetRenderer.render(input);

         ChildRecordListData widgetData = (ChildRecordListData) output.getWidgetData();
         assertEquals(1, widgetData.getTotalRows());
         assertEquals(1, widgetData.getQueryOutput().getRecords().get(0).getValue("id"));
         assertEquals("Triangle", widgetData.getQueryOutput().getRecords().get(0).getValue("name"));
      }

      {
         RecordListWidgetRenderer recordListWidgetRenderer = new RecordListWidgetRenderer();
         RenderWidgetInput        input                    = new RenderWidgetInput();
         input.setWidgetMetaData(widgetMetaData);
         input.setQueryParams(Map.of("maxShapeId", "4"));
         RenderWidgetOutput output = recordListWidgetRenderer.render(input);

         ChildRecordListData widgetData = (ChildRecordListData) output.getWidgetData();
         assertEquals(3, widgetData.getTotalRows());

         /////////////////////////////////////////////////////////////////////////
         // id=2,name=Square was skipped due to NOT_EQUALS Square in the filter //
         // max-shape-id applied we don't get id=5 or 6                         //
         // and they're ordered as specified in the filter (id desc)            //
         /////////////////////////////////////////////////////////////////////////
         assertEquals(4, widgetData.getQueryOutput().getRecords().get(0).getValue("id"));
         assertEquals("Rectangle", widgetData.getQueryOutput().getRecords().get(0).getValue("name"));

         assertEquals(3, widgetData.getQueryOutput().getRecords().get(1).getValue("id"));
         assertEquals("Circle", widgetData.getQueryOutput().getRecords().get(1).getValue("name"));

         assertEquals(1, widgetData.getQueryOutput().getRecords().get(2).getValue("id"));
         assertEquals("Triangle", widgetData.getQueryOutput().getRecords().get(2).getValue("name"));
      }
   }

}