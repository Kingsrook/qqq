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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 ** Base-class for table-level meta-data defined by some supplemental module, etc,
 ** outside of qqq core
 *******************************************************************************/
public abstract class QSupplementalTableMetaData implements Cloneable
{


   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean includeInPartialFrontendMetaData()
   {
      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean includeInFullFrontendMetaData()
   {
      return (false);
   }



   /*******************************************************************************
    ** Getter for type
    *******************************************************************************/
   public abstract String getType();



   /*******************************************************************************
    **
    *******************************************************************************/
   public void enrich(QInstance qInstance, QTableMetaData table)
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void validate(QInstance qInstance, QTableMetaData tableMetaData, QInstanceValidator qInstanceValidator)
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /***************************************************************************
    * adding cloneable to this type hierarchy - subclasses need to implement
    * finishClone to copy ther specific state.
    ***************************************************************************/
   @Override
   public final QSupplementalTableMetaData clone()
   {
      try
      {
         QSupplementalTableMetaData clone = (QSupplementalTableMetaData) super.clone();
         finishClone(clone);
         return clone;
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }



   /***************************************************************************
    * finish the cloning operation started in the base class. copy all state
    * from the subclass into the input clone (which can be safely casted to
    * the subclass's type, as it was obtained by super.clone())
    ***************************************************************************/
   protected abstract QSupplementalTableMetaData finishClone(QSupplementalTableMetaData abstractClone);

}
