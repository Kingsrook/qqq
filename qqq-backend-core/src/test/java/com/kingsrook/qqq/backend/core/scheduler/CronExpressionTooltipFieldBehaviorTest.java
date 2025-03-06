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

package com.kingsrook.qqq.backend.core.scheduler;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for CronExpressionTooltipFieldBehavior 
 *******************************************************************************/
class CronExpressionTooltipFieldBehaviorTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QFieldMetaData field = new QFieldMetaData("cronExpression", QFieldType.STRING);
      QContext.getQInstance().getTable(TestUtils.TABLE_NAME_SHAPE)
         .addField(field);

      CronExpressionTooltipFieldBehavior.addToField(field);

      new InsertAction().execute(new InsertInput(TestUtils.TABLE_NAME_SHAPE).withRecord(
         new QRecord().withValue("name", "Square").withValue("cronExpression", "* * * * * ?")));

      QRecord record = new GetAction().executeForRecord(new GetInput(TestUtils.TABLE_NAME_SHAPE).withPrimaryKey(1).withShouldGenerateDisplayValues(true));
      assertThat(record.getDisplayValue("cronExpression:" + AdornmentType.TooltipValues.TOOLTIP_DYNAMIC))
         .contains("every second");
   }

}