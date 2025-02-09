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

package com.kingsrook.qqq.backend.core.model.metadata.producers.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;


/***************************************************************************
 ** value that goes inside a QMetadataProducingEntity annotation, to control
 ** the generation of a QWidgetMetaData - for a ChildRecordList widget.
 ***************************************************************************/
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public @interface ChildRecordListWidget
{
   boolean enabled();

   String label() default "";

   int maxRows() default 20;

   boolean canAddChildRecords() default false;

   String manageAssociationName() default "";

   Class<? extends MetaDataCustomizerInterface> widgetMetaDataCustomizer() default MetaDataCustomizerInterface.NoopMetaDataCustomizer.class;
}
