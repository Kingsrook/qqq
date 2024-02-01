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

package com.kingsrook.qqq.backend.module.filesystem.s3;


import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kingsrook.qqq.backend.core.actions.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.actions.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.AbstractS3Action;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.S3DeleteAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.S3InsertAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.S3QueryAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.S3UpdateAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3BackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3TableBackendDetails;


/*******************************************************************************
 ** QQQ Backend module for working with AWS S3 filesystems
 *******************************************************************************/
public class S3BackendModule implements QBackendModuleInterface, FilesystemBackendModuleInterface
{
   public static final String BACKEND_TYPE = "s3";


   /*******************************************************************************
    ** For filesystem backends, get the module-specific action base-class, that helps
    ** with functions like listing and deleting files.
    *******************************************************************************/
   @Override
   public AbstractBaseFilesystemAction<S3ObjectSummary> getActionBase()
   {
      return (new AbstractS3Action());
   }



   /*******************************************************************************
    ** Method where a backend module must be able to provide its type (name).
    *******************************************************************************/
   @Override
   public String getBackendType()
   {
      return (BACKEND_TYPE);
   }



   /*******************************************************************************
    ** Method to identify the class used for backend meta data for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QBackendMetaData> getBackendMetaDataClass()
   {
      return (S3BackendMetaData.class);
   }



   /*******************************************************************************
    ** Method to identify the class used for table-backend details for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QTableBackendDetails> getTableBackendDetailsClass()
   {
      return (S3TableBackendDetails.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QueryInterface getQueryInterface()
   {
      return new S3QueryAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertInterface getInsertInterface()
   {
      return (new S3InsertAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public UpdateInterface getUpdateInterface()
   {
      return (new S3UpdateAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DeleteInterface getDeleteInterface()
   {
      return (new S3DeleteAction());
   }

}
