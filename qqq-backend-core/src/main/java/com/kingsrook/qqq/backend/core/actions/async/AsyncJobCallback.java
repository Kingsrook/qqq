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
    ** Setter for asyncJobStatus
    **
    *******************************************************************************/
   public void setAsyncJobStatus(AsyncJobStatus asyncJobStatus)
   {
      this.asyncJobStatus = asyncJobStatus;
   }



   /*******************************************************************************
    ** Update the message
    *******************************************************************************/
   public void updateStatus(String message)
   {
      this.asyncJobStatus.setMessage(message);
      storeUpdatedStatus();
   }



   /*******************************************************************************
    ** Update all 3 status fields
    *******************************************************************************/
   public void updateStatus(String message, int current, int total)
   {
      this.asyncJobStatus.setMessage(message);
      updateStatus(current, total); // this call will storeUpdatedStatus.
   }



   /*******************************************************************************
    ** Update the current and total fields (e.g., 1 of 2, 2 of 2, 3 of 2)
    *******************************************************************************/
   public void updateStatus(int current, int total)
   {
      this.asyncJobStatus.setCurrent(current > total ? total : current);
      this.asyncJobStatus.setTotal(total);
      storeUpdatedStatus();
   }



   /*******************************************************************************
    ** Update the current and total fields, but ONLY if the new values are
    ** both >= the previous values.
    *******************************************************************************/
   public void updateStatusOnlyUpwards(int current, int total)
   {
      boolean currentIsOkay = (this.asyncJobStatus.getCurrent() == null || this.asyncJobStatus.getCurrent() <= current);
      boolean totalIsOkay   = (this.asyncJobStatus.getTotal() == null || this.asyncJobStatus.getTotal() <= total);

      if(currentIsOkay && totalIsOkay)
      {
         updateStatus(current, total);
      }
   }



   /*******************************************************************************
    ** Increase the 'current' value in the '1 of 2' sense.
    *******************************************************************************/
   public void incrementCurrent()
   {
      incrementCurrent(1);
   }



   /*******************************************************************************
    ** Increase the 'current' value in the '1 of 2' sense.
    *******************************************************************************/
   public void incrementCurrent(int amount)
   {
      if(asyncJobStatus.getCurrent() != null)
      {
         if(asyncJobStatus.getTotal() != null && asyncJobStatus.getCurrent() + amount > asyncJobStatus.getTotal())
         {
            /////////////////////////////////////////////////////
            // make sure we don't ever make current > total... //
            /////////////////////////////////////////////////////
            asyncJobStatus.setCurrent(asyncJobStatus.getTotal());
         }
         else
         {
            asyncJobStatus.setCurrent(asyncJobStatus.getCurrent() + amount);
         }

         storeUpdatedStatus();
      }
   }



   /*******************************************************************************
    ** Remove the values from the current & total fields
    *******************************************************************************/
   public void clearCurrentAndTotal()
   {
      this.asyncJobStatus.setCurrent(null);
      this.asyncJobStatus.setTotal(null);
      storeUpdatedStatus();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void storeUpdatedStatus()
   {
      AsyncJobManager.getStateProvider().put(new UUIDAndTypeStateKey(jobUUID, StateType.ASYNC_JOB_STATUS), asyncJobStatus);
   }



   /*******************************************************************************
    ** Check if the asyncJobStatus had a cancellation requested.
    **
    ** TODO - concern about multiple threads writing this object to a non-in-memory
    **  state provider, and this value getting lost...
    *******************************************************************************/
   public boolean wasCancelRequested()
   {
      return (this.asyncJobStatus.getCancelRequested());
   }

}
