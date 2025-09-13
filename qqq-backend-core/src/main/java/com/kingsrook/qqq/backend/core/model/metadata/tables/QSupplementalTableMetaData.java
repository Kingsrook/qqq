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


import java.util.HashSet;
import java.util.Set;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Base-class for table-level meta-data defined by some supplemental module, etc,
 ** outside of qqq core
 *******************************************************************************/
public abstract class QSupplementalTableMetaData implements Cloneable
{
   private static final QLogger LOG = QLogger.getLogger(QSupplementalTableMetaData.class);

   private static Set<Class<?>> warnedAboutMissingFinishClones = new HashSet<>();



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
    *
    * Rather than making this public and breaking all existing implementations
    * that don't have it - we're making it protected, with a one-time warning
    * if it isn't implemented in a subclass.
    ***************************************************************************/
   protected QSupplementalTableMetaData finishClone(QSupplementalTableMetaData abstractClone)
   {
      if(!warnedAboutMissingFinishClones.contains(abstractClone.getClass()))
      {
         LOG.warn("Missing finishClone method in a subclass of QSupplementalTableMetaData.", logPair("className", abstractClone.getClass().getName()));
         warnedAboutMissingFinishClones.add(abstractClone.getClass());
      }

      return (abstractClone);
   }

}
