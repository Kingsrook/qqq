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


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendAppMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIMapValueType;


/***************************************************************************
 **
 ***************************************************************************/
public class AppMetaData implements ToSchema
{
   @OpenAPIExclude()
   private QFrontendAppMetaData wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public AppMetaData(QFrontendAppMetaData wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public AppMetaData()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Unique name for this app within the QQQ Instance")
   public String getName()
   {
      return (this.wrapped.getName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("User-facing name for this app")
   public String getLabel()
   {
      return (this.wrapped.getLabel());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Name of an icon for the app, from the material UI icon set")
   public String getIconName()
   {
      return (this.wrapped.getIconName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("List of widgets names that are part of this app.  These strings should be keys to the widgets map in the QQQ Instance.")
   @OpenAPIListItems(value = String.class)
   public List<String> getWidgets()
   {
      return (this.wrapped.getWidgets());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("List of other apps, tables, process, and reports, which are contained within this app.")
   @OpenAPIListItems(value = AppTreeNode.class, useRef = true)
   public List<AppTreeNode> getChildren()
   {
      return (CollectionUtils.nonNullList(this.wrapped.getChildren()).stream().map(a -> new AppTreeNode(a)).toList());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Map of other apps, tables, process, and reports, which are contained within this app.  Same contents as the children list, just structured as a map.")
   @OpenAPIMapValueType(value = AppTreeNode.class, useRef = true)
   public Map<String, AppTreeNode> getChildMap()
   {
      return (CollectionUtils.nonNullMap(this.wrapped.getChildMap()).entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new AppTreeNode(e.getValue()))));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("List of sections - sub-divisions of the app, to further organize its children.")
   @OpenAPIListItems(value = AppSection.class, useRef = true) // todo local type
   public List<AppSection> getSections()
   {
      return (CollectionUtils.nonNullList(this.wrapped.getSections()).stream().map(s -> new AppSection(s)).toList());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("Additional meta-data describing the app, which may not be known to the QQQ backend core module.")
   public Map<String, Object> getSupplementalAppMetaData()
   {
      return (new LinkedHashMap<>(CollectionUtils.nonNullMap(this.wrapped.getSupplementalAppMetaData())));
   }

}
