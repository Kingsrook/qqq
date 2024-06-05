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

package com.kingsrook.qqq.backend.core.actions.dashboard.widgets;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for Aggregate2DTableWidgetRenderer 
 *******************************************************************************/
class Aggregate2DTableWidgetRendererTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_PERSON_MEMORY).withRecords(List.of(
         new QRecord().withValue("lastName", "Simpson").withValue("homeStateId", 50),
         new QRecord().withValue("lastName", "Simpson").withValue("homeStateId", 50),
         new QRecord().withValue("lastName", "Simpson").withValue("homeStateId", 50),
         new QRecord().withValue("lastName", "Simpson").withValue("homeStateId", 49),
         new QRecord().withValue("lastName", "Flanders").withValue("homeStateId", 49),
         new QRecord().withValue("lastName", "Flanders").withValue("homeStateId", 49),
         new QRecord().withValue("lastName", "Burns").withValue("homeStateId", 50)
      )));

      RenderWidgetInput input = new RenderWidgetInput();
      input.setWidgetMetaData(new QWidgetMetaData()
         .withDefaultValue("tableName", TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withDefaultValue("valueField", "id")
         .withDefaultValue("rowField", "lastName")
         .withDefaultValue("columnField", "homeStateId")
         .withDefaultValue("orderBys", "row")
      );
      RenderWidgetOutput output    = new Aggregate2DTableWidgetRenderer().render(input);
      TableData          tableData = (TableData) output.getWidgetData();
      System.out.println(tableData.getRows());

      TableDataAssert.assertThat(tableData)
         .hasRowWithColumnContaining("_row", "Simpson", row ->
            row.hasColumnContaining("50", "3")
               .hasColumnContaining("49", "1")
               .hasColumnContaining("_total", "4"))
         .hasRowWithColumnContaining("_row", "Flanders", row ->
            row.hasColumnContaining("50", "0")
               .hasColumnContaining("49", "2")
               .hasColumnContaining("_total", "2"))
         .hasRowWithColumnContaining("_row", "Burns", row ->
            row.hasColumnContaining("50", "1")
               .hasColumnContaining("49", "0")
               .hasColumnContaining("_total", "1"))
         .hasRowWithColumnContaining("_row", "Total", row ->
            row.hasColumnContaining("50", "4")
               .hasColumnContaining("49", "3")
               .hasColumnContaining("_total", "7"));

      List<String> rowLabels = tableData.getRows().stream().map(r -> r.get("_row").toString()).toList();
      assertEquals(List.of("Burns", "Flanders", "Simpson", "Total"), rowLabels);
   }

}