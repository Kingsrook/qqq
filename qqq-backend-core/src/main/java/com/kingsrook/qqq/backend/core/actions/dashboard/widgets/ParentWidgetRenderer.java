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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ParentWidgetData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.ParentWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


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
         List<List<Map<String, String>>> pvsData                   = new ArrayList<>();
         List<String>                    pvsLabels                 = new ArrayList<>();
         List<String>                    pvsNames                  = new ArrayList<>();
         List<String>                    missingRequiredSelections = new ArrayList<>();
         for(ParentWidgetMetaData.DropdownData dropdownData : CollectionUtils.nonNullList(metaData.getDropdowns()))
         {
            String               possibleValueSourceName = dropdownData.getPossibleValueSourceName();
            QPossibleValueSource possibleValueSource     = input.getInstance().getPossibleValueSource(possibleValueSourceName);

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // this looks complicated, but is just look for a label in the dropdown data and if found use it,                                                                                  //
            // otherwise look for label in PVS and if found use that, otherwise just use the PVS name                                                                                          //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            String pvsLabel = dropdownData.getLabel() != null ? dropdownData.getLabel() : (possibleValueSource.getLabel() != null ? possibleValueSource.getLabel() : possibleValueSourceName);
            pvsLabels.add(pvsLabel);
            pvsNames.add(possibleValueSourceName);

            SearchPossibleValueSourceInput pvsInput = new SearchPossibleValueSourceInput(input.getInstance());
            pvsInput.setSession(input.getSession());
            pvsInput.setPossibleValueSourceName(possibleValueSourceName);

            if(dropdownData.getForeignKeyFieldName() != null)
            {
               ////////////////////////////////////////
               // look for an id in the query params //
               ////////////////////////////////////////
               Integer id = null;
               if(input.getQueryParams() != null && input.getQueryParams().containsKey("id") && StringUtils.hasContent(input.getQueryParams().get("id")))
               {
                  id = Integer.parseInt(input.getQueryParams().get("id"));
               }
               if(id != null)
               {
                  pvsInput.setDefaultQueryFilter(new QQueryFilter().withCriteria(
                     new QFilterCriteria(
                        dropdownData.getForeignKeyFieldName(),
                        QCriteriaOperator.EQUALS,
                        id)));
               }
            }

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

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // because we know the dropdowns and what the field names will be when something is selected, we can make //
            // sure that something has been selected, and if not, display a message that a selection needs made       //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(dropdownData.getIsRequired())
            {
               if(!input.getQueryParams().containsKey(possibleValueSourceName) || !StringUtils.hasContent(input.getQueryParams().get(possibleValueSourceName)))
               {
                  missingRequiredSelections.add(pvsLabel);
               }
            }
         }

         widgetData.setDropdownNameList(pvsNames);
         widgetData.setDropdownLabelList(pvsLabels);
         widgetData.setDropdownDataList(pvsData);

         ////////////////////////////////////////////////////////////////////////////////
         // if there are any missing required dropdowns, build up a message to display //
         ////////////////////////////////////////////////////////////////////////////////
         if(missingRequiredSelections.size() > 0)
         {
            StringBuilder sb = new StringBuilder("Please select a ").append(StringUtils.joinWithCommasAndAnd(missingRequiredSelections));
            sb.append(" from the ").append(StringUtils.plural(missingRequiredSelections.size(), "dropdown", "dropdowns")).append(" above.");
            widgetData.setDropdownNeedsSelectedText(sb.toString());
         }
         else
         {
            widgetData.setChildWidgetNameList(metaData.getChildWidgetNameList());
         }

         return (new RenderWidgetOutput(widgetData));
      }
      catch(Exception e)
      {
         throw (new QException("Error rendering parent widget", e));
      }
   }

}
