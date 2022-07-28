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
import cloud.localstack.awssdkv1.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.AbstractS3Action;
import com.kingsrook.qqq.backend.module.filesystem.s3.utils.S3Utils;


/*******************************************************************************
 ** Subclass of the S3Backend module, meant for use in unit tests, if/where we
 ** need to make sure we use the localstack version of the S3 client.
 *******************************************************************************/
public class S3BackendModuleSubclassForTest extends S3BackendModule
{

   /*******************************************************************************
    ** Seed the AbstractS3Action with an s3Utils object that has the localstack
    ** s3 client in it
    *******************************************************************************/
   @Override
   public AbstractBaseFilesystemAction<S3ObjectSummary> getActionBase()
   {
      AbstractS3Action actionBase = (AbstractS3Action) super.getActionBase();
      S3Utils          s3Utils    = new S3Utils();
      s3Utils.setAmazonS3(TestUtils.getClientS3());
      actionBase.setS3Utils(s3Utils);
      return (actionBase);
   }

}
