/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.data;


import java.lang.reflect.Method;


/*******************************************************************************
 ** Reflective information about an association in a QRecordEntity
 *******************************************************************************/
public class QRecordEntityAssociation
{
   private final String fieldName;
   private final Method getter;
   private final Method setter;

   private final Class<? extends QRecordEntity> associatedType;

   private final QAssociation associationAnnotation;



   /*******************************************************************************
    ** Constructor.
    *******************************************************************************/
   public QRecordEntityAssociation(String fieldName, Method getter, Method setter, Class<? extends QRecordEntity> associatedType, QAssociation associationAnnotation)
   {
      this.fieldName = fieldName;
      this.getter = getter;
      this.setter = setter;
      this.associatedType = associatedType;
      this.associationAnnotation = associationAnnotation;
   }



   /*******************************************************************************
    ** Getter for fieldName
    **
    *******************************************************************************/
   public String getFieldName()
   {
      return fieldName;
   }



   /*******************************************************************************
    ** Getter for getter
    **
    *******************************************************************************/
   public Method getGetter()
   {
      return getter;
   }



   /*******************************************************************************
    ** Getter for setter
    **
    *******************************************************************************/
   public Method getSetter()
   {
      return setter;
   }



   /*******************************************************************************
    ** Getter for associatedType
    **
    *******************************************************************************/
   public Class<? extends QRecordEntity> getAssociatedType()
   {
      return associatedType;
   }



   /*******************************************************************************
    ** Getter for associationAnnotation
    **
    *******************************************************************************/
   public QAssociation getAssociationAnnotation()
   {
      return associationAnnotation;
   }

}
