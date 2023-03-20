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


/*******************************************************************************
 **
 *******************************************************************************/
public class Path
{
   private Method get;
   private Method patch;
   private Method put;
   private Method delete;
   private Method post;



   /*******************************************************************************
    ** Getter for get
    *******************************************************************************/
   public Method getGet()
   {
      return (this.get);
   }



   /*******************************************************************************
    ** Setter for get
    *******************************************************************************/
   public void setGet(Method get)
   {
      this.get = get;
   }



   /*******************************************************************************
    ** Fluent setter for get
    *******************************************************************************/
   public Path withGet(Method get)
   {
      this.get = get;
      return (this);
   }



   /*******************************************************************************
    ** Getter for post
    *******************************************************************************/
   public Method getPost()
   {
      return (this.post);
   }



   /*******************************************************************************
    ** Setter for post
    *******************************************************************************/
   public void setPost(Method post)
   {
      this.post = post;
   }



   /*******************************************************************************
    ** Fluent setter for post
    *******************************************************************************/
   public Path withPost(Method post)
   {
      this.post = post;
      return (this);
   }



   /*******************************************************************************
    ** Getter for put
    *******************************************************************************/
   public Method getPut()
   {
      return (this.put);
   }



   /*******************************************************************************
    ** Setter for put
    *******************************************************************************/
   public void setPut(Method put)
   {
      this.put = put;
   }



   /*******************************************************************************
    ** Fluent setter for put
    *******************************************************************************/
   public Path withPut(Method put)
   {
      this.put = put;
      return (this);
   }



   /*******************************************************************************
    ** Getter for patch
    *******************************************************************************/
   public Method getPatch()
   {
      return (this.patch);
   }



   /*******************************************************************************
    ** Setter for patch
    *******************************************************************************/
   public void setPatch(Method patch)
   {
      this.patch = patch;
   }



   /*******************************************************************************
    ** Fluent setter for patch
    *******************************************************************************/
   public Path withPatch(Method patch)
   {
      this.patch = patch;
      return (this);
   }



   /*******************************************************************************
    ** Getter for delete
    *******************************************************************************/
   public Method getDelete()
   {
      return (this.delete);
   }



   /*******************************************************************************
    ** Setter for delete
    *******************************************************************************/
   public void setDelete(Method delete)
   {
      this.delete = delete;
   }



   /*******************************************************************************
    ** Fluent setter for delete
    *******************************************************************************/
   public Path withDelete(Method delete)
   {
      this.delete = delete;
      return (this);
   }

}
