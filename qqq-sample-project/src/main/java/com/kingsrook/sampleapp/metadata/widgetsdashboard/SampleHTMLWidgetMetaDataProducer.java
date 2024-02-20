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

package com.kingsrook.sampleapp.metadata.widgetsdashboard;


import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.NoCodeWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.HtmlWrapper;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.QNoCodeWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.WidgetHtmlLine;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.MaterialDashboardIconRoleNames;


/*******************************************************************************
 ** Meta Data Producer for SampleHTMLWidget
 *******************************************************************************/
public class SampleHTMLWidgetMetaDataProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "SampleHTMLWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      QNoCodeWidgetMetaData widgetMetaData = (QNoCodeWidgetMetaData) new QNoCodeWidgetMetaData()
         .withName(NAME)
         .withType(WidgetType.HTML.getType())
         .withGridColumns(4)
         .withIsCard(true)
         .withLabel("HTML")
         .withTooltip("This is a sample of an HTML widget")
         .withShowReloadButton(false)
         .withIcon(MaterialDashboardIconRoleNames.TOP_RIGHT_INSIDE_CARD, new QIcon("data_object").withColor("#D87E28"))
         .withCodeReference(new QCodeReference(NoCodeWidgetRenderer.class));

      widgetMetaData.withOutput(new WidgetHtmlLine()
         .withWrapper(HtmlWrapper.BIG_CENTERED)
         .withVelocityTemplate("Purely Custom"));

      widgetMetaData.withOutput(new WidgetHtmlLine()
         .withVelocityTemplate("<i>User</i> <b>Defined</b> <u>HTML</u>"));

      return widgetMetaData;
   }

}
