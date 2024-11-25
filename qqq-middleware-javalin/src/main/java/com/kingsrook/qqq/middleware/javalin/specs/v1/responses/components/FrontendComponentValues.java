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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.AbstractBlockWidgetData;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIHasAdditionalProperties;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;


/*******************************************************************************
 **
 *******************************************************************************/
@OpenAPIDescription("""
   These are the known values that can appear in the values map under a FrontendComponent, to control
   how that component should be presented to the user.
   
   Note that additional properties may appear as well.
   
   In addition, components are expected to use values from an active process's `values` map (e.g., as included in
   a `ProcessStepComplete` object), with the following contract between component-types and expected values:
   
   - For component type=`HTML`, there will be a process value with key=`${stepName}.html` (e.g., `resultScreen.html`),
   whose value is the HTML to display on that screen.
   - For component type=`HELP_TEXT`:  There will be a process value with key=`text`, whose value is the text to display on that screen.
   There may also be a process value with key=`previewText`, which, if present, can be shown before the full text is shown, 
   e.g., with a toggle control to hide/show the `text` value.
   """)
@OpenAPIHasAdditionalProperties()
public class FrontendComponentValues implements ToSchema
{
   @OpenAPIExclude()
   private Map<String, Serializable> wrapped;

   @OpenAPIDescription("""
      Components of type=`WIDGET`, which do not reference a widget defined in the QQQ Instance, but instead,
      are defined as a list of blocks within a frontend step component, will have a this value set to true.""")
   private Boolean isAdHocWidget;

   @OpenAPIDescription("""
      Components of type=`WIDGET`, which are set as `isAdHocWidget=true`, should include a list of WidgetBlocks in this value.""")
   @OpenAPIListItems(value = WidgetBlock.class, useRef = true)
   private List<WidgetBlock> blocks;

   @OpenAPIDescription("""
      Components of type=`WIDGET`, which should render a widget defined in the QQQ instance, this value specifies
      the name of that widget.  Contrast with ad-hoc widgets.
      """)
   private String widgetName;

   @OpenAPIDescription("""
      Components of type=`EDIT_FORM` can specify a subset of field names to include.  This can be used to break a form up into
      sections, by including multiple EDIT_FORM components, with different lists of `includeFieldNames`.
      """)
   @OpenAPIListItems(String.class)
   private List<String> includeFieldNames;

   @OpenAPIDescription("""
      Components of type=`EDIT_FORM` can specify a user-facing text label to show on screen.
      """)
   private String sectionLabel;


   /***************************************************************************
    **
    ***************************************************************************/
   public FrontendComponentValues(Map<String, Serializable> values)
   {
      this.wrapped = values;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FrontendComponentValues()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public Map<String, Serializable> toMap()
   {
      if(wrapped == null)
      {
         return (null);
      }

      Map<String, Serializable> rs = new HashMap<>();
      for(Map.Entry<String, Serializable> entry : wrapped.entrySet())
      {
         String       key   = entry.getKey();
         Serializable value = entry.getValue();

         if(key.equals("blocks"))
         {
            ArrayList<WidgetBlock> resultList = new ArrayList<>();

            List<AbstractBlockWidgetData<?, ?, ?, ?>> sourceList = (List<AbstractBlockWidgetData<?, ?, ?, ?>>) value;
            for(AbstractBlockWidgetData<?, ?, ?, ?> abstractBlockWidgetData : sourceList)
            {
               resultList.add(new WidgetBlock(abstractBlockWidgetData));
            }

            value = resultList;
         }

         rs.put(key, value);
      }

      return (rs);
   }
}
