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

package com.kingsrook.qqq.backend.core.state;


import java.io.Serializable;
import java.util.Optional;


/*******************************************************************************
 ** QQQ state provider interface.  Provides standard interface for various
 ** implementations of how to store & retrieve user/process state data, like
 ** sessions, or process data.  Not like permanent record data - that is done in
 ** Backend modules.
 **
 ** Different implementations may be:  in-memory (non-persistent!!), or on-disk
 ** (with the tradeoffs that has), in-database, in-cache-system, etc.
 **
 ** Things which probably haven't been thought about here include:
 ** - multi-layering.  e.g., always have an in-memory layer on top of a more
 **   persistent backend, but then how to avoid staleness in-memory?
 *  - cleanup.  when do we ever purge things to avoid running out of memory/storage?
 *******************************************************************************/
public interface StateProviderInterface
{

   /*******************************************************************************
    ** Put a block of data, under a key, into the state store.
    *******************************************************************************/
   <T extends Serializable> void put(AbstractStateKey key, T data);

   /*******************************************************************************
    ** Get a block of data, under a key, from the state store.
    *******************************************************************************/
   <T extends Serializable> Optional<T> get(Class<? extends T> type, AbstractStateKey key);

   /*******************************************************************************
    ** Remove a block of data, under a key, from the state store.
    *******************************************************************************/
   void remove(AbstractStateKey key);

}
