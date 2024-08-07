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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.memory;


import com.kingsrook.qqq.backend.core.actions.interfaces.AggregateInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.CountInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.QStorageInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;


/*******************************************************************************
 ** A simple (probably only valid for testing?) implementation of the QModuleInterface,
 ** that just stores its records in-memory.
 **
 ** In general, this class is intended to behave, as much as possible, like an RDBMS.
 **
 ** TODO - in future, if we need to - make configs for things like "case-insensitive",
 **  and "allow loose typing".
 *******************************************************************************/
public class MemoryBackendModule implements QBackendModuleInterface
{
   static
   {
      QBackendModuleDispatcher.registerBackendModule(new MemoryBackendModule());
   }


   /*******************************************************************************
    ** Method where a backend module must be able to provide its type (name).
    *******************************************************************************/
   @Override
   public String getBackendType()
   {
      return ("memory");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public CountInterface getCountInterface()
   {
      return new MemoryCountAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QueryInterface getQueryInterface()
   {
      return new MemoryQueryAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public AggregateInterface getAggregateInterface()
   {
      return new MemoryAggregateAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertInterface getInsertInterface()
   {
      return (new MemoryInsertAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public UpdateInterface getUpdateInterface()
   {
      return (new MemoryUpdateAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DeleteInterface getDeleteInterface()
   {
      return (new MemoryDeleteAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QStorageInterface getStorageInterface()
   {
      return (new MemoryStorageAction());
   }
}
