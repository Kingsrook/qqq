/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.qbits;


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.TopLevelMetaDataInterface;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Meta-data to define an active QBit in a QQQ Instance.
 **
 ** The unique "name" for the QBit is composed of its groupId and artifactId
 ** (maven style).  There is also a version - but it is not part of the unique
 ** name.  But - there is also a namespace attribute, which IS part of the
 ** unique name.  This will (eventually?) allow us to have multiple instances
 ** of the same qbit in a qInstance at the same time (e.g., 2 versions of some
 ** table, which should be namespace-prefixed);
 **
 ** QBitMetaData also retains the QBitConfig that was used to produce the QBit.
 **
 ** Some meta-data objects are aware of the fact that they may have come from a
 ** QBit - see SourceQBitAware interface.  These objects can get their source
 ** QBitMetaData (this object) and its config,via that interface.
 *******************************************************************************/
public class QBitMetaData implements TopLevelMetaDataInterface
{
   private String groupId;
   private String artifactId;
   private String version;
   private String namespace;

   private QBitConfig config;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String getName()
   {
      String name = groupId + ":" + artifactId;
      if(StringUtils.hasContent(namespace))
      {
         name += ":" + namespace;
      }
      return name;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void addSelfToInstance(QInstance qInstance)
   {
      qInstance.addQBit(this);
   }



   /*******************************************************************************
    ** Getter for config
    *******************************************************************************/
   public QBitConfig getConfig()
   {
      return (this.config);
   }



   /*******************************************************************************
    ** Setter for config
    *******************************************************************************/
   public void setConfig(QBitConfig config)
   {
      this.config = config;
   }



   /*******************************************************************************
    ** Fluent setter for config
    *******************************************************************************/
   public QBitMetaData withConfig(QBitConfig config)
   {
      this.config = config;
      return (this);
   }



   /*******************************************************************************
    ** Getter for groupId
    *******************************************************************************/
   public String getGroupId()
   {
      return (this.groupId);
   }



   /*******************************************************************************
    ** Setter for groupId
    *******************************************************************************/
   public void setGroupId(String groupId)
   {
      this.groupId = groupId;
   }



   /*******************************************************************************
    ** Fluent setter for groupId
    *******************************************************************************/
   public QBitMetaData withGroupId(String groupId)
   {
      this.groupId = groupId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for artifactId
    *******************************************************************************/
   public String getArtifactId()
   {
      return (this.artifactId);
   }



   /*******************************************************************************
    ** Setter for artifactId
    *******************************************************************************/
   public void setArtifactId(String artifactId)
   {
      this.artifactId = artifactId;
   }



   /*******************************************************************************
    ** Fluent setter for artifactId
    *******************************************************************************/
   public QBitMetaData withArtifactId(String artifactId)
   {
      this.artifactId = artifactId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for version
    *******************************************************************************/
   public String getVersion()
   {
      return (this.version);
   }



   /*******************************************************************************
    ** Setter for version
    *******************************************************************************/
   public void setVersion(String version)
   {
      this.version = version;
   }



   /*******************************************************************************
    ** Fluent setter for version
    *******************************************************************************/
   public QBitMetaData withVersion(String version)
   {
      this.version = version;
      return (this);
   }



   /*******************************************************************************
    ** Getter for namespace
    *******************************************************************************/
   public String getNamespace()
   {
      return (this.namespace);
   }



   /*******************************************************************************
    ** Setter for namespace
    *******************************************************************************/
   public void setNamespace(String namespace)
   {
      this.namespace = namespace;
   }



   /*******************************************************************************
    ** Fluent setter for namespace
    *******************************************************************************/
   public QBitMetaData withNamespace(String namespace)
   {
      this.namespace = namespace;
      return (this);
   }

}
