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

package com.kingsrook.qqq.middleware.javalin.specs.v1.responses.components;


import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;


/***************************************************************************
 **
 ***************************************************************************/
public class FilterCriteria implements ToSchema
{
   @OpenAPIExclude()
   private QFilterCriteria wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FilterCriteria(QFilterCriteria wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FilterCriteria()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Field name that this criteria applies to")
   public String getFieldName()
   {
      return (this.wrapped.getFieldName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Logical operator that applies to this criteria")
   public QCriteriaOperator getOperator()
   {
      return (this.wrapped.getOperator());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Values to apply via the operator to the field")
   @OpenAPIListItems(value = String.class)
   public List<String> getValues()
   {
      return (CollectionUtils.nonNullList(this.wrapped.getValues()).stream().map(String::valueOf).collect(Collectors.toList()));
   }

}
