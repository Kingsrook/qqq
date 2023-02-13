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


import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class WidgetQueryField extends AbstractWidgetValueSource
{
   private String       tableName;
   private String       selectFieldName;
   private QQueryFilter filter;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WidgetQueryField()
   {
      setType(getClass().getSimpleName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Object evaluate(Map<String, Object> context) throws QException
   {
      // todo - look for params in the filter (fields or values)
      //  make sure to update it in supplementContext below too!!
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(tableName);
      queryInput.setFilter(filter);
      queryInput.setLimit(1);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      if(CollectionUtils.nullSafeHasContents(queryOutput.getRecords()))
      {
         return (queryOutput.getRecords().get(0).getValue(selectFieldName));
      }

      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void supplementContext(Map<String, Object> context)
   {
      context.put(getName() + ".filter", filter);
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public WidgetQueryField withName(String name)
   {
      setName(name);
      return (this);
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
   public WidgetQueryField withTableName(String tableName)
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
   public WidgetQueryField withFilter(QQueryFilter filter)
   {
      this.filter = filter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for selectFieldName
    *******************************************************************************/
   public String getSelectFieldName()
   {
      return (this.selectFieldName);
   }



   /*******************************************************************************
    ** Setter for selectFieldName
    *******************************************************************************/
   public void setSelectFieldName(String selectFieldName)
   {
      this.selectFieldName = selectFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for selectFieldName
    *******************************************************************************/
   public WidgetQueryField withSelectFieldName(String selectFieldName)
   {
      this.selectFieldName = selectFieldName;
      return (this);
   }

}
