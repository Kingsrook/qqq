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

package com.kingsrook.qqq.backend.module.mongodb.actions;


import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.logging.QLogger;


/*******************************************************************************
 ** Helper to cancel statements that timeout.
 *******************************************************************************/
public class TimeoutCanceller implements Runnable
{
   private static final QLogger              LOG = QLogger.getLogger(TimeoutCanceller.class);
   private final        MongoClientContainer mongoClientContainer;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TimeoutCanceller(MongoClientContainer mongoClientContainer)
   {
      this.mongoClientContainer = mongoClientContainer;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run()
   {
      try
      {
         mongoClientContainer.closeIfNeeded();
         LOG.info("Cancelled timed out query");
      }
      catch(Exception e)
      {
         LOG.warn("Error trying to cancel statement after timeout", e);
      }

      throw (new QRuntimeException("Statement timed out and was cancelled."));
   }
}
