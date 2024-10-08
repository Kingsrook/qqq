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
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ProcessWidgetData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Generic widget for displaying a process as a widget
 *******************************************************************************/
public class ProcessWidgetRenderer extends AbstractWidgetRenderer
{
   public static final String WIDGET_PROCESS_NAME = "processName";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      ActionHelper.validateSession(input);

      try
      {
         ProcessWidgetData data = new ProcessWidgetData();
         if(input.getWidgetMetaData() instanceof QWidgetMetaData widgetMetaData)
         {
            setupDropdowns(input, widgetMetaData, data);

            String           processName     = (String) widgetMetaData.getDefaultValues().get(WIDGET_PROCESS_NAME);
            QProcessMetaData processMetaData = QContext.getQInstance().getProcess(processName);
            data.setProcessMetaData(processMetaData);

            data.setDefaultValues(new HashMap<>(input.getQueryParams()));
         }
         return (new RenderWidgetOutput(data));
      }
      catch(Exception e)
      {
         throw (new QException("Error rendering process widget", e));
      }
   }

}
