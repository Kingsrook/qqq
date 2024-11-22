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


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.AlertData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;


/*******************************************************************************
 ** Widget that can add an Alert to a process screen.
 **
 ** In the process, you'll want values:
 ** - alertType - name of entry in AlertType enum (ERROR, WARNING, SUCCESS)
 ** - alertHtml - html to display inside the alert (other than its icon)
 *******************************************************************************/
public class AlertWidgetRenderer extends AbstractWidgetRenderer implements MetaDataProducerInterface<QWidgetMetaData>
{
   public static final String NAME = "AlertWidgetRenderer";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      AlertData.AlertType alertType = AlertData.AlertType.WARNING;
      if(input.getQueryParams().containsKey("alertType"))
      {
         alertType = AlertData.AlertType.valueOf(input.getQueryParams().get("alertType"));
      }

      String html = "Warning";
      if(input.getQueryParams().containsKey("alertHtml"))
      {
         html = input.getQueryParams().get("alertHtml");
      }

      return (new RenderWidgetOutput(new AlertData(alertType, html)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      return new QWidgetMetaData()
         .withType(WidgetType.ALERT.getType())
         .withGridColumns(12)
         .withName(NAME)
         .withIsCard(false)
         .withShowReloadButton(false)
         .withCodeReference(new QCodeReference(getClass()));
   }
}
