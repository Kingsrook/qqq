/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.code;


import java.io.Serializable;
import java.util.Map;
import java.util.Objects;


/*******************************************************************************
 ** a code reference that also has a map of properties.  This object (with the
 ** properties) will be passed in to the referenced object, if it implements
 ** InitializableViaCodeReference.
 *******************************************************************************/
public class QCodeReferenceWithProperties extends QCodeReference
{
   private final Map<String, Serializable> properties;



   /***************************************************************************
    **
    ***************************************************************************/
   public QCodeReferenceWithProperties(Class<?> javaClass, Map<String, Serializable> properties)
   {
      super(javaClass);
      this.properties = properties;
   }



   /*******************************************************************************
    ** Getter for properties
    **
    *******************************************************************************/
   public Map<String, Serializable> getProperties()
   {
      return properties;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public boolean equals(Object o)
   {
      if(o == null || getClass() != o.getClass())
      {
         return false;
      }
      if(!super.equals(o))
      {
         return false;
      }
      QCodeReferenceWithProperties that = (QCodeReferenceWithProperties) o;
      return Objects.equals(properties, that.properties);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(super.hashCode(), properties);
   }
}
