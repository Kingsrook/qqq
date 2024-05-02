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

package com.kingsrook.qqq.backend.core.model.actions.messaging;


/*******************************************************************************
 **
 *******************************************************************************/
public class Content
{
   private String      body;
   private ContentRole contentRole;



   /*******************************************************************************
    ** Getter for body
    *******************************************************************************/
   public String getBody()
   {
      return (this.body);
   }



   /*******************************************************************************
    ** Setter for body
    *******************************************************************************/
   public void setBody(String body)
   {
      this.body = body;
   }



   /*******************************************************************************
    ** Fluent setter for body
    *******************************************************************************/
   public Content withBody(String body)
   {
      this.body = body;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contentRole
    *******************************************************************************/
   public ContentRole getContentRole()
   {
      return (this.contentRole);
   }



   /*******************************************************************************
    ** Setter for contentRole
    *******************************************************************************/
   public void setContentRole(ContentRole contentRole)
   {
      this.contentRole = contentRole;
   }



   /*******************************************************************************
    ** Fluent setter for contentRole
    *******************************************************************************/
   public Content withContentRole(ContentRole contentRole)
   {
      this.contentRole = contentRole;
      return (this);
   }

}
