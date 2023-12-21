/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.TopLevelMetaDataInterface;


/*******************************************************************************
 ** Interface for classes that know how to produce meta data objects.  Useful with
 ** MetaDataProducerHelper, to put point at a package full of these, and populate
 ** your whole QInstance.
 **
 ** See also MetaDataProducer - an implementer of this interface, which actually
 ** came first, and is fine to extend if producing a meta-data class is all your
 ** clas means to do (nice and "Single-responsibility principle").
 **
 ** But, in some applications you may want to, for example, have one class that
 ** defines a process step, and also produces the meta-data for that process, so
 ** your whole process can just be one class - so then just have your step class
 ** implement this interface.  or, same idea for a QRecordEntity that provides
 ** its own TableMetaData.
 *******************************************************************************/
public interface MetaDataProducerInterface<T extends TopLevelMetaDataInterface>
{
   int DEFAULT_SORT_ORDER = 500;


   /*******************************************************************************
    ** Produce the metaData object.  Generally, you don't want to add it to the instance
    ** yourself - but the instance is there in case you need it to get other metaData.
    *******************************************************************************/
   T produce(QInstance qInstance) throws QException;


   /*******************************************************************************
    ** In case this producer needs to run before (or after) others, this method
    ** can control influence that (e.g., if used by MetaDataProducerHelper).
    **
    ** Smaller values run first.
    *******************************************************************************/
   default int getSortOrder()
   {
      return (DEFAULT_SORT_ORDER);
   }


   /*******************************************************************************
    ** turn this producer on or off - e.g., maybe based on an env value.
    **
    *******************************************************************************/
   default boolean isEnabled()
   {
      return (true);
   }

}
