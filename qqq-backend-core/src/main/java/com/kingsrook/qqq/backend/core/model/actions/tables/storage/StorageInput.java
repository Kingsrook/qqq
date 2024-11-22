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

package com.kingsrook.qqq.backend.core.model.actions.tables.storage;


import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;


/*******************************************************************************
 ** Input for Storage actions.
 *******************************************************************************/
public class StorageInput extends AbstractTableActionInput
{
   private String reference;
   private String contentType;



   /*******************************************************************************
    **
    *******************************************************************************/
   public StorageInput(String storageTableName)
   {
      super();
      setTableName(storageTableName);
   }



   /*******************************************************************************
    ** Getter for reference
    *******************************************************************************/
   public String getReference()
   {
      return (this.reference);
   }



   /*******************************************************************************
    ** Setter for reference
    *******************************************************************************/
   public void setReference(String reference)
   {
      this.reference = reference;
   }



   /*******************************************************************************
    ** Fluent setter for reference
    *******************************************************************************/
   public StorageInput withReference(String reference)
   {
      this.reference = reference;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contentType
    *******************************************************************************/
   public String getContentType()
   {
      return (this.contentType);
   }



   /*******************************************************************************
    ** Setter for contentType
    *******************************************************************************/
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }



   /*******************************************************************************
    ** Fluent setter for contentType
    *******************************************************************************/
   public StorageInput withContentType(String contentType)
   {
      this.contentType = contentType;
      return (this);
   }

}
