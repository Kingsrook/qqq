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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DynamicDefaultValueBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;


/*******************************************************************************
 ** Annotation to place onto fields in a QRecordEntity, to add additional attributes
 ** for propagating down into the corresponding QFieldMetaData
 **
 *******************************************************************************/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QField
{
   /*******************************************************************************
    **
    *******************************************************************************/
   String label() default "";

   /*******************************************************************************
    **
    *******************************************************************************/
   String backendName() default "";

   /*******************************************************************************
    **
    *******************************************************************************/
   boolean isRequired() default false;

   /*******************************************************************************
    **
    *******************************************************************************/
   boolean isEditable() default true;

   /*******************************************************************************
    **
    *******************************************************************************/
   boolean isHidden() default false;

   /*******************************************************************************
    **
    *******************************************************************************/
   String defaultValue() default "";

   /*******************************************************************************
    **
    *******************************************************************************/
   String displayFormat() default "";

   /*******************************************************************************
    **
    *******************************************************************************/
   String possibleValueSourceName() default "";

   /*******************************************************************************
    **
    *******************************************************************************/
   int maxLength() default Integer.MAX_VALUE;

   /*******************************************************************************
    **
    *******************************************************************************/
   ValueTooLongBehavior valueTooLongBehavior() default ValueTooLongBehavior.PASS_THROUGH;

   /*******************************************************************************
    **
    *******************************************************************************/
   DynamicDefaultValueBehavior dynamicDefaultValueBehavior() default DynamicDefaultValueBehavior.NONE;

   //////////////////////////////////////////////////////////////////////////////////////////
   // new attributes here likely need implementation in QFieldMetaData.constructFromGetter //
   //////////////////////////////////////////////////////////////////////////////////////////
}
