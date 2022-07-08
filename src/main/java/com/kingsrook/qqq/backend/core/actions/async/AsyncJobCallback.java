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

package com.kingsrook.qqq.backend.core.actions.async;


import java.util.UUID;
import com.kingsrook.qqq.backend.core.state.StateType;
import com.kingsrook.qqq.backend.core.state.UUIDAndTypeStateKey;


/*******************************************************************************
 ** Argument passed to an AsyncJob when it runs, which can be used to communicate
 ** data back out of the job.
 **
 ** TODO - future - allow cancellation to be indicated here?
 *******************************************************************************/
public class AsyncJobCallback
{
   private UUID           jobUUID;
   private AsyncJobStatus asyncJobStatus;



   /*******************************************************************************
    **
    *******************************************************************************/
   public AsyncJobCallback(UUID jobUUID, AsyncJobStatus asyncJobStatus)
   {
      this.jobUUID = jobUUID;
      this.asyncJobStatus = asyncJobStatus;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void updateStatus(String message)
   {
      this.asyncJobStatus.setMessage(message);
      storeUpdatedStatus();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void updateStatus(int current, int total)
   {
      this.asyncJobStatus.setCurrent(current);
      this.asyncJobStatus.setTotal(total);
      storeUpdatedStatus();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void updateStatus(String message, int current, int total)
   {
      this.asyncJobStatus.setMessage(message);
      this.asyncJobStatus.setCurrent(current);
      this.asyncJobStatus.setTotal(total);
      storeUpdatedStatus();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void storeUpdatedStatus()
   {
      AsyncJobManager.getStateProvider().put(new UUIDAndTypeStateKey(jobUUID, StateType.ASYNC_JOB_STATUS), asyncJobStatus);
   }

}
