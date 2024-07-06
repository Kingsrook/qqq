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


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.dashboard.RenderWidgetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.AlertData;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerHelper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for ProcessAlertWidget 
 *******************************************************************************/
class ProcessAlertWidgetTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      MetaDataProducerHelper.processAllMetaDataProducersInPackage(QContext.getQInstance(), ProcessAlertWidget.class.getPackageName());

      RenderWidgetInput input = new RenderWidgetInput();
      input.setWidgetMetaData(QContext.getQInstance().getWidget(ProcessAlertWidget.NAME));

      ///////////////////////////////////////////////////////////////////////////////////////////
      // make sure we run w/o exceptions (and w/ default outputs) if there are no query params //
      ///////////////////////////////////////////////////////////////////////////////////////////
      RenderWidgetOutput output = new RenderWidgetAction().execute(input);
      assertEquals(AlertData.AlertType.WARNING, ((AlertData) output.getWidgetData()).getAlertType());
      assertEquals("Warning", ((AlertData) output.getWidgetData()).getHtml());

      //////////////////////////////////////////////////////
      // make sure we input params come through to output //
      //////////////////////////////////////////////////////
      input.addQueryParam("alertType", "ERROR");
      input.addQueryParam("alertHtml", "Do not touch Willy");
      output = new RenderWidgetAction().execute(input);
      assertEquals(AlertData.AlertType.ERROR, ((AlertData) output.getWidgetData()).getAlertType());
      assertEquals("Do not touch Willy", ((AlertData) output.getWidgetData()).getHtml());

   }

}