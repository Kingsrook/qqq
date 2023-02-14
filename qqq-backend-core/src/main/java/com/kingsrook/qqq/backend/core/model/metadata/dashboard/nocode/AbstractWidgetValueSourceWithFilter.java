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


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractWidgetValueSourceWithFilter extends AbstractWidgetValueSource
{
   protected String       tableName;
   protected QQueryFilter filter;

   protected List<AbstractConditionalFilter> conditionalFilterList;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QQueryFilter getEffectiveFilter(RenderWidgetInput input)
   {
      QQueryFilter effectiveFilter;
      if(filter == null)
      {
         effectiveFilter = new QQueryFilter();
      }
      else
      {
         effectiveFilter = filter.clone();
      }

      for(AbstractConditionalFilter conditionalFilter : CollectionUtils.nonNullList(conditionalFilterList))
      {
         if(conditionalFilter.testCondition(input))
         {
            QQueryFilter additionalFilter = conditionalFilter.getFilter(input);
            for(QFilterCriteria criterion : additionalFilter.getCriteria())
            {
               effectiveFilter.addCriteria(criterion);
            }
         }
      }

      return (effectiveFilter);
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public AbstractWidgetValueSourceWithFilter withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filter
    *******************************************************************************/
   public QQueryFilter getFilter()
   {
      return (this.filter);
   }



   /*******************************************************************************
    ** Setter for filter
    *******************************************************************************/
   public void setFilter(QQueryFilter filter)
   {
      this.filter = filter;
   }



   /*******************************************************************************
    ** Fluent setter for filter
    *******************************************************************************/
   public AbstractWidgetValueSourceWithFilter withFilter(QQueryFilter filter)
   {
      this.filter = filter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for conditionalFilterList
    *******************************************************************************/
   public List<AbstractConditionalFilter> getConditionalFilterList()
   {
      return (this.conditionalFilterList);
   }



   /*******************************************************************************
    ** Setter for conditionalFilterList
    *******************************************************************************/
   public void setConditionalFilterList(List<AbstractConditionalFilter> conditionalFilterList)
   {
      this.conditionalFilterList = conditionalFilterList;
   }



   /*******************************************************************************
    ** Fluent setter for conditionalFilterList
    *******************************************************************************/
   public AbstractWidgetValueSourceWithFilter withConditionalFilterList(List<AbstractConditionalFilter> conditionalFilterList)
   {
      this.conditionalFilterList = conditionalFilterList;
      return (this);
   }

}
