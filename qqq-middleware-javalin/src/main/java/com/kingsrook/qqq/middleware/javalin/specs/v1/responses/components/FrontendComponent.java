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
import java.util.EnumSet;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.ToSchema;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIEnumSubSet;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIMapKnownEntries;


/*******************************************************************************
 **
 *******************************************************************************/
public class FrontendComponent implements ToSchema
{
   @OpenAPIExclude()
   private QFrontendComponentMetaData wrapped;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FrontendComponent(QFrontendComponentMetaData wrapped)
   {
      this.wrapped = wrapped;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FrontendComponent()
   {
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class QComponentTypeSubSet implements OpenAPIEnumSubSet.EnumSubSet<QComponentType>
   {
      private static EnumSet<QComponentType> subSet = null;

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public EnumSet<QComponentType> getSubSet()
      {
         if(subSet == null)
         {
            EnumSet<QComponentType> subSet = EnumSet.allOf(QComponentType.class);
            subSet.remove(QComponentType.BULK_LOAD_FILE_MAPPING_FORM);
            subSet.remove(QComponentType.BULK_LOAD_VALUE_MAPPING_FORM);
            subSet.remove(QComponentType.BULK_LOAD_PROFILE_FORM);
            QComponentTypeSubSet.subSet = subSet;
         }

         return(subSet);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @OpenAPIDescription("The type of this component.  e.g., what kind of UI element(s) should be presented to the user.")
   @OpenAPIEnumSubSet(QComponentTypeSubSet.class)
   public QComponentType getType()
   {
      return (this.wrapped.getType());
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   @OpenAPIDescription("Name-value pairs specific to the type of component.")
   @OpenAPIMapKnownEntries(value = FrontendComponentValues.class, useRef = true)
   public Map<String, Serializable> getValues()
   {
      return (this.wrapped.getValues() == null ? null : new FrontendComponentValues(this.wrapped.getValues()).toMap());
   }

}
