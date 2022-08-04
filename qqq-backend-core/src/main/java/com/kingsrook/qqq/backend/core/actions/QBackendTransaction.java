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

package com.kingsrook.qqq.backend.core.actions;


import java.io.IOException;
import com.kingsrook.qqq.backend.core.exceptions.QException;


/*******************************************************************************
 ** Container wherein backend modules can track data and/or objects that are
 ** part of a transaction.
 **
 ** Most obvious use-case would be a JDBC Connection.  See subclass in rdbms module.
 *******************************************************************************/
public class QBackendTransaction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public void commit() throws QException
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void rollback() throws QException
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    * Closes this stream and releases any system resources associated
    * with it. If the stream is already closed then invoking this
    * method has no effect.
    *
    * <p> As noted in {@link AutoCloseable#close()}, cases where the
    * close may fail require careful attention. It is strongly advised
    * to relinquish the underlying resources and to internally
    * <em>mark</em> the {@code Closeable} as closed, prior to throwing
    * the {@code IOException}.
    *
    * @throws IOException
    *    if an I/O error occurs
    *******************************************************************************/
   public void close()
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }
}
