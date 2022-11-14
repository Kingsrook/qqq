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


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Class for loading widget implementation code and rendering of widgets
 **
 *******************************************************************************/
public class RenderWidgetAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public RenderWidgetOutput execute(RenderWidgetInput input) throws QException
   {
      ActionHelper.validateSession(input);

      AbstractWidgetRenderer widgetRenderer = QCodeLoader.getAdHoc(AbstractWidgetRenderer.class, input.getWidgetMetaData().getCodeReference());

      ///////////////////////////////////////////////////////////////
      // move default values from meta data into this render input //
      ///////////////////////////////////////////////////////////////
      if(input.getWidgetMetaData() instanceof QWidgetMetaData widgetMetaData)
      {
         for(Map.Entry<String, Serializable> entry : widgetMetaData.getDefaultValues().entrySet())
         {
            input.getQueryParams().putIfAbsent(entry.getKey(), ValueUtils.getValueAsString(entry.getValue()));
         }
      }

      return (widgetRenderer.render(input));
   }
}
