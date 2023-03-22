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


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.kingsrook.qqq.api.ApiMiddlewareType;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QMiddlewareInstanceMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiInstanceMetaData extends QMiddlewareInstanceMetaData
{
   private String name;
   private String description;
   private String contactEmail;

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
   public void validate(QInstance qInstance, QInstanceValidator validator)
   {
      validator.assertCondition(StringUtils.hasContent(name), "Missing name for instance api");
      validator.assertCondition(StringUtils.hasContent(description), "Missing description for instance api");
      validator.assertCondition(StringUtils.hasContent(contactEmail), "Missing contactEmail for instance api");

      Set<APIVersion> allVersions = new HashSet<>();

      if(validator.assertCondition(currentVersion != null, "Missing currentVersion for instance api"))
      {
         allVersions.add(currentVersion);
      }

      if(validator.assertCondition(supportedVersions != null, "Missing supportedVersions for instance api"))
      {
         validator.assertCondition(supportedVersions.contains(currentVersion), "supportedVersions [" + supportedVersions + "] does not contain currentVersion [" + currentVersion + "] for instance api");
         allVersions.addAll(supportedVersions);
      }

      for(APIVersion pastVersion : CollectionUtils.nonNullList(pastVersions))
      {
         validator.assertCondition(pastVersion.compareTo(currentVersion) < 0, "pastVersion [" + pastVersion + "] is not lexicographically before currentVersion [" + currentVersion + "] for instance api");
         allVersions.add(pastVersion);
      }

      for(APIVersion futureVersion : CollectionUtils.nonNullList(futureVersions))
      {
         validator.assertCondition(futureVersion.compareTo(currentVersion) > 0, "futureVersion [" + futureVersion + "] is not lexicographically after currentVersion [" + currentVersion + "] for instance api");
         allVersions.add(futureVersion);
      }

      /////////////////////////////////
      // validate all table versions //
      /////////////////////////////////
      for(QTableMetaData table : qInstance.getTables().values())
      {
         ApiTableMetaData apiTableMetaData = ApiMiddlewareType.getApiTableMetaData(table);
         if(apiTableMetaData != null)
         {
            validator.assertCondition(allVersions.contains(new APIVersion(apiTableMetaData.getInitialVersion())), "Table " + table.getName() + "'s initial API version is not a recognized version.");
         }
      }

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



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public ApiInstanceMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    *******************************************************************************/
   public String getDescription()
   {
      return (this.description);
   }



   /*******************************************************************************
    ** Setter for description
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    *******************************************************************************/
   public ApiInstanceMetaData withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contactEmail
    *******************************************************************************/
   public String getContactEmail()
   {
      return (this.contactEmail);
   }



   /*******************************************************************************
    ** Setter for contactEmail
    *******************************************************************************/
   public void setContactEmail(String contactEmail)
   {
      this.contactEmail = contactEmail;
   }



   /*******************************************************************************
    ** Fluent setter for contactEmail
    *******************************************************************************/
   public ApiInstanceMetaData withContactEmail(String contactEmail)
   {
      this.contactEmail = contactEmail;
      return (this);
   }

}
