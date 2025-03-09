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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;


/*******************************************************************************
 ** annotation to go on a QRecordEntity class, which you would like to be
 ** processed by MetaDataProducerHelper, to automatically produce some meta-data
 ** objects.  Specifically supports:
 **
 ** - Making a possible-value-source out of the table.
 ** - Processing child tables to create joins and childRecordList widgets
 *******************************************************************************/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public @interface QMetaDataProducingEntity
{
   boolean produceTableMetaData() default false;
   Class<? extends MetaDataCustomizerInterface> tableMetaDataCustomizer() default MetaDataCustomizerInterface.NoopMetaDataCustomizer.class;

   boolean producePossibleValueSource() default false;
   ChildTable[] childTables() default { };

}
