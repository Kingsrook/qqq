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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling;


import java.util.Iterator;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkLoadFileRow;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractIteratorBasedFileToRows<E> implements FileToRowsInterface
{
   private Iterator<E> iterator;

   private boolean         useLast = false;
   private BulkLoadFileRow last;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean hasNext()
   {
      if(iterator == null)
      {
         throw new IllegalStateException("Object was not init'ed");
      }

      if(useLast)
      {
         return true;
      }

      return iterator.hasNext();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public BulkLoadFileRow next()
   {
      if(iterator == null)
      {
         throw new IllegalStateException("Object was not init'ed");
      }

      if(useLast)
      {
         useLast = false;
         return (this.last);
      }

      E e = iterator.next();

      BulkLoadFileRow row = makeRow(e);

      this.last = row;
      return (this.last);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public abstract BulkLoadFileRow makeRow(E e);



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void unNext()
   {
      useLast = true;
   }



   /*******************************************************************************
    ** Getter for iterator
    *******************************************************************************/
   public Iterator<E> getIterator()
   {
      return (this.iterator);
   }



   /*******************************************************************************
    ** Setter for iterator
    *******************************************************************************/
   public void setIterator(Iterator<E> iterator)
   {
      this.iterator = iterator;
   }

}
