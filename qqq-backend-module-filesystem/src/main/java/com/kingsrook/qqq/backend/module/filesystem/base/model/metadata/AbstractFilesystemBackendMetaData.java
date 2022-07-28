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

package com.kingsrook.qqq.backend.module.filesystem.base.model.metadata;


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;


/*******************************************************************************
 ** Base class for all BackendMetaData for all filesystem-style backend modules.
 *******************************************************************************/
public class AbstractFilesystemBackendMetaData extends QBackendMetaData
{
   private String basePath;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public AbstractFilesystemBackendMetaData()
   {
      super();
   }



   /*******************************************************************************
    ** Getter for basePath
    **
    *******************************************************************************/
   public String getBasePath()
   {
      return (basePath);
   }



   /*******************************************************************************
    ** Setter for basePath
    **
    *******************************************************************************/
   public void setBasePath(String basePath)
   {
      this.basePath = basePath;
   }



   /*******************************************************************************
    ** Fluent setter for basePath
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends AbstractFilesystemBackendMetaData> T withBasePath(String basePath)
   {
      this.basePath = basePath;
      return (T) this;
   }

}
