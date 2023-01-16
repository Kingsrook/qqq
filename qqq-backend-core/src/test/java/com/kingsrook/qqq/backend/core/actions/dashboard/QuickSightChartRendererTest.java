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

package com.kingsrook.qqq.backend.core.actions.dashboard;


import java.net.UnknownHostException;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.QuickSightChartRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QuickSightChartMetaData;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Unit test for QuickSightChartRenderer
 *******************************************************************************/
class QuickSightChartRendererTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWrongMetaDataClass() throws QException
   {
      RenderWidgetInput input = getInput().withWidgetMetaData(new QWidgetMetaData());
      assertThatThrownBy(() -> new QuickSightChartRenderer().render(input))
         .hasRootCauseInstanceOf(ClassCastException.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoCredentials() throws QException
   {
      RenderWidgetInput input = getInput().withWidgetMetaData(new QuickSightChartMetaData());
      assertThatThrownBy(() -> new QuickSightChartRenderer().render(input))
         .hasRootCauseInstanceOf(NullPointerException.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBadCredentials() throws QException
   {
      RenderWidgetInput input = getInput().withWidgetMetaData(
         new QuickSightChartMetaData()
            .withName("test")
            .withAccessKey("FAIL")
            .withSecretKey("FAIL")
            .withRegion("FAIL")
            .withAccountId("FAIL")
      );
      assertThatThrownBy(() -> new QuickSightChartRenderer().render(input))
         .hasRootCauseInstanceOf(UnknownHostException.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RenderWidgetInput getInput()
   {
      return (new RenderWidgetInput());

   }

}
