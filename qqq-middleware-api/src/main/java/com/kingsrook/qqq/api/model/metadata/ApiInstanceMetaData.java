/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.api.model.metadata;


import java.util.List;
import com.kingsrook.qqq.api.ApiMiddlewareType;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QMiddlewareInstanceMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiInstanceMetaData extends QMiddlewareInstanceMetaData
{
   private APIVersion       currentVersion;
   private List<APIVersion> supportedVersions;
   private List<APIVersion> pastVersions;
   private List<APIVersion> futureVersions;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ApiInstanceMetaData()
   {
      setType(ApiMiddlewareType.NAME);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void validate(QInstance qInstance)
   {
      // todo - version is set
      // todo - past versions all < current < all future
      // todo - any version specified anywhere is one of the known
   }



   /*******************************************************************************
    ** Getter for currentVersion
    *******************************************************************************/
   public APIVersion getCurrentVersion()
   {
      return (this.currentVersion);
   }



   /*******************************************************************************
    ** Setter for currentVersion
    *******************************************************************************/
   public void setCurrentVersion(APIVersion currentVersion)
   {
      this.currentVersion = currentVersion;
   }



   /*******************************************************************************
    ** Fluent setter for currentVersion
    *******************************************************************************/
   public ApiInstanceMetaData withCurrentVersion(APIVersion currentVersion)
   {
      this.currentVersion = currentVersion;
      return (this);
   }



   /*******************************************************************************
    ** Getter for pastVersions
    *******************************************************************************/
   public List<APIVersion> getPastVersions()
   {
      return (this.pastVersions);
   }



   /*******************************************************************************
    ** Setter for pastVersions
    *******************************************************************************/
   public void setPastVersions(List<APIVersion> pastVersions)
   {
      this.pastVersions = pastVersions;
   }



   /*******************************************************************************
    ** Fluent setter for pastVersions
    *******************************************************************************/
   public ApiInstanceMetaData withPastVersions(List<APIVersion> pastVersions)
   {
      this.pastVersions = pastVersions;
      return (this);
   }



   /*******************************************************************************
    ** Getter for futureVersions
    *******************************************************************************/
   public List<APIVersion> getFutureVersions()
   {
      return (this.futureVersions);
   }



   /*******************************************************************************
    ** Setter for futureVersions
    *******************************************************************************/
   public void setFutureVersions(List<APIVersion> futureVersions)
   {
      this.futureVersions = futureVersions;
   }



   /*******************************************************************************
    ** Fluent setter for futureVersions
    *******************************************************************************/
   public ApiInstanceMetaData withFutureVersions(List<APIVersion> futureVersions)
   {
      this.futureVersions = futureVersions;
      return (this);
   }



   /*******************************************************************************
    ** Getter for supportedVersions
    *******************************************************************************/
   public List<APIVersion> getSupportedVersions()
   {
      return (this.supportedVersions);
   }



   /*******************************************************************************
    ** Setter for supportedVersions
    *******************************************************************************/
   public void setSupportedVersions(List<APIVersion> supportedVersions)
   {
      this.supportedVersions = supportedVersions;
   }



   /*******************************************************************************
    ** Fluent setter for supportedVersions
    *******************************************************************************/
   public ApiInstanceMetaData withSupportedVersions(List<APIVersion> supportedVersions)
   {
      this.supportedVersions = supportedVersions;
      return (this);
   }

}
