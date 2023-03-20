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

package com.kingsrook.qqq.api.model.openapi;


import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;


/*******************************************************************************
 **
 *******************************************************************************/
public class OpenAPI
{
   private String            openapi = "3.0.3"; // todo not version
   private Info              info;
   private ExternalDocs      externalDocs;
   private List<Server>      servers;
   private List<Tag>         tags;
   private Map<String, Path> paths;
   private Components        components;



   /*******************************************************************************
    ** Getter for version
    *******************************************************************************/
   public String getOpenapi()
   {
      return (this.openapi);
   }



   /*******************************************************************************
    ** Getter for version
    *******************************************************************************/
   @JsonIgnore
   public String getVersion()
   {
      return (this.openapi);
   }



   /*******************************************************************************
    ** Setter for version
    *******************************************************************************/
   public void setVersion(String version)
   {
      this.openapi = version;
   }



   /*******************************************************************************
    ** Fluent setter for version
    *******************************************************************************/
   public OpenAPI withVersion(String version)
   {
      this.openapi = version;
      return (this);
   }



   /*******************************************************************************
    ** Getter for info
    *******************************************************************************/
   public Info getInfo()
   {
      return (this.info);
   }



   /*******************************************************************************
    ** Setter for info
    *******************************************************************************/
   public void setInfo(Info info)
   {
      this.info = info;
   }



   /*******************************************************************************
    ** Fluent setter for info
    *******************************************************************************/
   public OpenAPI withInfo(Info info)
   {
      this.info = info;
      return (this);
   }



   /*******************************************************************************
    ** Getter for externalDocs
    *******************************************************************************/
   public ExternalDocs getExternalDocs()
   {
      return (this.externalDocs);
   }



   /*******************************************************************************
    ** Setter for externalDocs
    *******************************************************************************/
   public void setExternalDocs(ExternalDocs externalDocs)
   {
      this.externalDocs = externalDocs;
   }



   /*******************************************************************************
    ** Fluent setter for externalDocs
    *******************************************************************************/
   public OpenAPI withExternalDocs(ExternalDocs externalDocs)
   {
      this.externalDocs = externalDocs;
      return (this);
   }



   /*******************************************************************************
    ** Getter for servers
    *******************************************************************************/
   public List<Server> getServers()
   {
      return (this.servers);
   }



   /*******************************************************************************
    ** Setter for servers
    *******************************************************************************/
   public void setServers(List<Server> servers)
   {
      this.servers = servers;
   }



   /*******************************************************************************
    ** Fluent setter for servers
    *******************************************************************************/
   public OpenAPI withServers(List<Server> servers)
   {
      this.servers = servers;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tags
    *******************************************************************************/
   public List<Tag> getTags()
   {
      return (this.tags);
   }



   /*******************************************************************************
    ** Setter for tags
    *******************************************************************************/
   public void setTags(List<Tag> tags)
   {
      this.tags = tags;
   }



   /*******************************************************************************
    ** Fluent setter for tags
    *******************************************************************************/
   public OpenAPI withTags(List<Tag> tags)
   {
      this.tags = tags;
      return (this);
   }



   /*******************************************************************************
    ** Getter for components
    *******************************************************************************/
   public Components getComponents()
   {
      return (this.components);
   }



   /*******************************************************************************
    ** Setter for components
    *******************************************************************************/
   public void setComponents(Components components)
   {
      this.components = components;
   }



   /*******************************************************************************
    ** Fluent setter for components
    *******************************************************************************/
   public OpenAPI withComponents(Components components)
   {
      this.components = components;
      return (this);
   }



   /*******************************************************************************
    ** Getter for paths
    *******************************************************************************/
   public Map<String, Path> getPaths()
   {
      return (this.paths);
   }



   /*******************************************************************************
    ** Setter for paths
    *******************************************************************************/
   public void setPaths(Map<String, Path> paths)
   {
      this.paths = paths;
   }



   /*******************************************************************************
    ** Fluent setter for paths
    *******************************************************************************/
   public OpenAPI withPaths(Map<String, Path> paths)
   {
      this.paths = paths;
      return (this);
   }

}
