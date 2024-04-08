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

package com.kingsrook.qqq.backend.core.actions.async;


import java.util.UUID;
import com.kingsrook.qqq.backend.core.logging.QLogger;


/*******************************************************************************
 ** subclass designed to be used when we want there to be an instance (so code
 ** doesn't have to all be null-tolerant), but there's no one who will ever be
 ** reading the status data, so we don't need to store the object in a
 ** state provider.
 *******************************************************************************/
public class NonPersistedAsyncJobCallback extends AsyncJobCallback
{
   private static final QLogger LOG = QLogger.getLogger(NonPersistedAsyncJobCallback.class);
   private final AsyncJobStatus asyncJobStatus;



   /*******************************************************************************
    **
    *******************************************************************************/
   public NonPersistedAsyncJobCallback(UUID jobUUID, AsyncJobStatus asyncJobStatus)
   {
      super(jobUUID, asyncJobStatus);
      this.asyncJobStatus = asyncJobStatus;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected void storeUpdatedStatus()
   {
      ///////////////////////////////////////////////////////////
      // todo - downgrade or remove this before merging to dev //
      ///////////////////////////////////////////////////////////
      LOG.info("Not persisting status from a NonPersistedAsyncJobCallback: " + asyncJobStatus.getJobName() + " / " + asyncJobStatus.getMessage());
   }

}
