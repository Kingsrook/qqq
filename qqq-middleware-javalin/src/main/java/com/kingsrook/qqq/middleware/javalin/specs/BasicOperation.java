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

package com.kingsrook.qqq.middleware.javalin.specs;


import com.kingsrook.qqq.openapi.model.HttpMethod;


/*******************************************************************************
 ** Basic definition of an operation (e.g., an endpoint exposed in the API).
 *******************************************************************************/
public class BasicOperation
{
   private String        path;
   private HttpMethod    httpMethod;
   private TagsInterface tag;
   private String        shortSummary;
   private String        longDescription;



   /*******************************************************************************
    ** Getter for path
    *******************************************************************************/
   public String getPath()
   {
      return (this.path);
   }



   /*******************************************************************************
    ** Setter for path
    *******************************************************************************/
   public void setPath(String path)
   {
      this.path = path;
   }



   /*******************************************************************************
    ** Fluent setter for path
    *******************************************************************************/
   public BasicOperation withPath(String path)
   {
      this.path = path;
      return (this);
   }



   /*******************************************************************************
    ** Getter for httpMethod
    *******************************************************************************/
   public HttpMethod getHttpMethod()
   {
      return (this.httpMethod);
   }



   /*******************************************************************************
    ** Setter for httpMethod
    *******************************************************************************/
   public void setHttpMethod(HttpMethod httpMethod)
   {
      this.httpMethod = httpMethod;
   }



   /*******************************************************************************
    ** Fluent setter for httpMethod
    *******************************************************************************/
   public BasicOperation withHttpMethod(HttpMethod httpMethod)
   {
      this.httpMethod = httpMethod;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tag
    *******************************************************************************/
   public TagsInterface getTag()
   {
      return (this.tag);
   }



   /*******************************************************************************
    ** Setter for tag
    *******************************************************************************/
   public void setTag(TagsInterface tag)
   {
      this.tag = tag;
   }



   /*******************************************************************************
    ** Fluent setter for tag
    *******************************************************************************/
   public BasicOperation withTag(TagsInterface tag)
   {
      this.tag = tag;
      return (this);
   }



   /*******************************************************************************
    ** Getter for shortSummary
    *******************************************************************************/
   public String getShortSummary()
   {
      return (this.shortSummary);
   }



   /*******************************************************************************
    ** Setter for shortSummary
    *******************************************************************************/
   public void setShortSummary(String shortSummary)
   {
      this.shortSummary = shortSummary;
   }



   /*******************************************************************************
    ** Fluent setter for shortSummary
    *******************************************************************************/
   public BasicOperation withShortSummary(String shortSummary)
   {
      this.shortSummary = shortSummary;
      return (this);
   }



   /*******************************************************************************
    ** Getter for longDescription
    *******************************************************************************/
   public String getLongDescription()
   {
      return (this.longDescription);
   }



   /*******************************************************************************
    ** Setter for longDescription
    *******************************************************************************/
   public void setLongDescription(String longDescription)
   {
      this.longDescription = longDescription;
   }



   /*******************************************************************************
    ** Fluent setter for longDescription
    *******************************************************************************/
   public BasicOperation withLongDescription(String longDescription)
   {
      this.longDescription = longDescription;
      return (this);
   }

}
