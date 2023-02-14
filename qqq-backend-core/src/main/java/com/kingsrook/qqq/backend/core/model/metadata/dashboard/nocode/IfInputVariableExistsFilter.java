/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode;


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MutableList;


/*******************************************************************************
 **
 *******************************************************************************/
public class IfInputVariableExistsFilter extends AbstractConditionalFilter
{
   private String       inputVariableName;
   private QQueryFilter filter;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public IfInputVariableExistsFilter()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public IfInputVariableExistsFilter(String inputVariableName, QQueryFilter filter)
   {
      this.inputVariableName = inputVariableName;
      this.filter = filter;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean testCondition(RenderWidgetInput renderWidgetInput)
   {
      return (renderWidgetInput.getQueryParams().get(inputVariableName) != null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QQueryFilter getFilter(RenderWidgetInput renderWidgetInput)
   {
      for(QFilterCriteria criterion : CollectionUtils.nonNullList(filter.getCriteria()))
      {
         if(criterion.getValues() != null)
         {
            criterion.setValues(new MutableList<>(criterion.getValues()));
            for(int i = 0; i < criterion.getValues().size(); i++)
            {
               Serializable value = criterion.getValues().get(i);
               if(value instanceof String valueString && valueString.equals("${input." + inputVariableName + "}"))
               {
                  criterion.getValues().set(i, renderWidgetInput.getQueryParams().get(inputVariableName));
               }
            }
         }
      }

      return (filter);
   }
}
