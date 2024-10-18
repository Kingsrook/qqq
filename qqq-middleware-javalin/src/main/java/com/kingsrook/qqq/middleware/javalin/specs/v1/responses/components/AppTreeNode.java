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
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;


/***************************************************************************
 **
 ***************************************************************************/
public class AppTreeNode implements ToSchema
{
   @OpenAPIExclude()
   private com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public AppTreeNode(com.kingsrook.qqq.backend.core.model.metadata.frontend.AppTreeNode wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public AppTreeNode()
   {
   }


   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("The type of node (table, process, report, app)")
   public String getType()
   {
      return (this.wrapped.getType() == null ? null : this.wrapped.getType().name());
   }


   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Unique (within its type) name for this element.  e.g., for type = 'table', the table's name.")
   public String getName()
   {
      return (this.wrapped.getName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("User-facing name of the element.")
   public String getLabel()
   {
      return (this.wrapped.getLabel());
   }


   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Child elements.  Only applies for type='app', which contains additional apps under it")
   @OpenAPIListItems(value = AppTreeNode.class, useRef = true)
   public List<AppTreeNode> getChildren()
   {
      return (CollectionUtils.nonNullList(this.wrapped.getChildren()).stream().map(a -> new AppTreeNode(a)).toList());
   }


}
