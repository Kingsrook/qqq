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

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Base-class for instance-level meta-data defined by some supplemental module, etc,
 ** outside of qqq core
 *******************************************************************************/
public interface QSupplementalInstanceMetaData extends TopLevelMetaDataInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   default void enrich(QTableMetaData table)
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   default void validate(QInstance qInstance, QInstanceValidator validator)
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   default void addSelfToInstance(QInstance qInstance)
   {
      qInstance.withSupplementalMetaData(this);
   }


   /***************************************************************************
    **
    ***************************************************************************/
   static <S extends QSupplementalInstanceMetaData> S of(QInstance qInstance, String name)
   {
      return ((S) qInstance.getSupplementalMetaData(name));
   }


   /***************************************************************************
    **
    ***************************************************************************/
   static <S extends QSupplementalInstanceMetaData> S ofOrWithNew(QInstance qInstance, String name, Supplier<S> supplier)
   {
      S s = (S) qInstance.getSupplementalMetaData(name);
      if(s == null)
      {
         s = supplier.get();
         s.addSelfToInstance(qInstance);
      }
      return (s);
   }

}
