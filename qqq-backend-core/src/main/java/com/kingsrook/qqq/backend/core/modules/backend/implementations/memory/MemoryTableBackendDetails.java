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


import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;


/*******************************************************************************
 **
 *******************************************************************************/
public class MemoryTableBackendDetails extends QTableBackendDetails
{
   private boolean cloneUponStore = false;



   /*******************************************************************************
    ** Getter for cloneUponStore
    **
    *******************************************************************************/
   public boolean getCloneUponStore()
   {
      return cloneUponStore;
   }



   /*******************************************************************************
    ** Setter for cloneUponStore
    **
    *******************************************************************************/
   public void setCloneUponStore(boolean cloneUponStore)
   {
      this.cloneUponStore = cloneUponStore;
   }



   /*******************************************************************************
    ** Fluent setter for cloneUponStore
    **
    *******************************************************************************/
   public MemoryTableBackendDetails withCloneUponStore(boolean cloneUponStore)
   {
      this.cloneUponStore = cloneUponStore;
      return (this);
   }

}
