/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.model.actions.shared.mapping;


/*******************************************************************************
 ** For bulk-loads, define where a QField comes from in an input data source.
 **
 *******************************************************************************/
public abstract class AbstractQFieldMapping<T>
{

   /*******************************************************************************
    ** Enum to define the types of sources a mapping may use
    **
    *******************************************************************************/
   @SuppressWarnings("rawtypes")
   public enum SourceType
   {
      KEY(String.class),
      INDEX(Integer.class);

      private Class sourceClass;



      /*******************************************************************************
       ** enum constructor
       *******************************************************************************/
      SourceType(Class sourceClass)
      {
         this.sourceClass = sourceClass;
      }



      /*******************************************************************************
       ** Getter for sourceClass
       **
       *******************************************************************************/
      public Class getSourceClass()
      {
         return sourceClass;
      }
   }



   /*******************************************************************************
    ** For a given field, return its source - a key (e.g., from a json object or csv
    ** with a header row) or an index (for a csv w/o a header)
    **
    *******************************************************************************/
   public abstract T getFieldSource(String fieldName);


   /*******************************************************************************
    ** for a mapping instance, get what its source-type is
    **
    *******************************************************************************/
   public abstract SourceType getSourceType();
}
