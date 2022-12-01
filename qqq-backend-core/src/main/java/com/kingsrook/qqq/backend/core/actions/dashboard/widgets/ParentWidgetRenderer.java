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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.values.SearchPossibleValueSourceAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ParentWidgetData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.ParentWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Generic widget for display a parent widget with children of possible values,
 ** child widgets, and child actions
 *******************************************************************************/
public class ParentWidgetRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      ActionHelper.validateSession(input);

      try
      {
         ParentWidgetMetaData metaData   = (ParentWidgetMetaData) input.getWidgetMetaData();
         ParentWidgetData     widgetData = new ParentWidgetData();

         /////////////////////////////////////////////////////////////
         // handle any PVSs creating dropdown data for the frontend //
         /////////////////////////////////////////////////////////////
         List<List<Map<String, String>>> pvsData   = new ArrayList<>();
         List<String>                    pvsLabels = new ArrayList<>();
         List<String>                    pvsNames  = new ArrayList<>();
         for(String possibleValueSourceName : CollectionUtils.nonNullList(metaData.getPossibleValueNameList()))
         {
            QPossibleValueSource possibleValueSource = input.getInstance().getPossibleValueSource(possibleValueSourceName);
            pvsLabels.add(possibleValueSource.getLabel() != null ? possibleValueSource.getLabel() : possibleValueSourceName);
            pvsNames.add(possibleValueSourceName);

            SearchPossibleValueSourceInput pvsInput = new SearchPossibleValueSourceInput(input.getInstance());
            pvsInput.setSession(input.getSession());
            pvsInput.setPossibleValueSourceName(possibleValueSourceName);
            SearchPossibleValueSourceOutput output = new SearchPossibleValueSourceAction().execute(pvsInput);

            List<Map<String, String>> dropdownOptionList = new ArrayList<>();
            pvsData.add(dropdownOptionList);

            //////////////////////////////////////////
            // sort results, dedupe, and add to map //
            //////////////////////////////////////////
            Set<String> exists = new HashSet<>();
            output.getResults().removeIf(pvs -> !exists.add(pvs.getLabel()));
            output.getResults().sort(Comparator.comparing(QPossibleValue::getLabel));
            for(QPossibleValue<?> possibleValue : output.getResults())
            {
               dropdownOptionList.add(Map.of(
                  "id", String.valueOf(possibleValue.getId()),
                  "label", possibleValue.getLabel()
               ));
            }
         }

         widgetData.setDropdownNameList(pvsNames);
         widgetData.setDropdownLabelList(pvsLabels);
         widgetData.setDropdownDataList(pvsData);
         widgetData.setChildWidgetNameList(metaData.getChildWidgetNameList());

         return (new RenderWidgetOutput(widgetData));
      }
      catch(Exception e)
      {
         throw (new QException("Error rendering parent widget", e));
      }
   }

}
