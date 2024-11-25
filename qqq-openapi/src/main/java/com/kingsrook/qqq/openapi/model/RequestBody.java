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

package com.kingsrook.qqq.openapi.model;


import java.util.LinkedHashMap;
import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class RequestBody
{
   private Boolean              required = false;
   private String               description;
   private Map<String, Content> content;



   /*******************************************************************************
    ** Getter for required
    *******************************************************************************/
   public Boolean getRequired()
   {
      return (this.required);
   }



   /*******************************************************************************
    ** Setter for required
    *******************************************************************************/
   public void setRequired(Boolean required)
   {
      this.required = required;
   }



   /*******************************************************************************
    ** Fluent setter for required
    *******************************************************************************/
   public RequestBody withRequired(Boolean required)
   {
      this.required = required;
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
   public RequestBody withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for content
    *******************************************************************************/
   public Map<String, Content> getContent()
   {
      return (this.content);
   }



   /*******************************************************************************
    ** Setter for content
    *******************************************************************************/
   public void setContent(Map<String, Content> content)
   {
      this.content = content;
   }



   /*******************************************************************************
    ** Fluent setter for content
    *******************************************************************************/
   public RequestBody withContent(Map<String, Content> content)
   {
      this.content = content;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for content
    *******************************************************************************/
   public RequestBody withContent(String key, Content content)
   {
      if(this.content == null)
      {
         this.content = new LinkedHashMap<>();
      }
      this.content.put(key, content);
      return (this);
   }

}
