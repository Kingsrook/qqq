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

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;


/*******************************************************************************
 ** Model a file that a user uploaded (or otherwise submitted to the qqq backend).
 *******************************************************************************/
public class QUploadedFile implements Serializable
{
   private String filename;
   private byte[] bytes;



   /*******************************************************************************
    ** Getter for filename
    **
    *******************************************************************************/
   public String getFilename()
   {
      return filename;
   }



   /*******************************************************************************
    ** Setter for filename
    **
    *******************************************************************************/
   public void setFilename(String filename)
   {
      this.filename = filename;
   }



   /*******************************************************************************
    ** Getter for bytes
    **
    *******************************************************************************/
   public byte[] getBytes()
   {
      return bytes;
   }



   /*******************************************************************************
    ** Setter for bytes
    **
    *******************************************************************************/
   public void setBytes(byte[] bytes)
   {
      this.bytes = bytes;
   }
}
