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
public class Info
{
   private String  title;
   private String  description;
   private String  termsOfService;
   private Contact contact;
   private String  version;



   /*******************************************************************************
    ** Getter for title
    *******************************************************************************/
   public String getTitle()
   {
      return (this.title);
   }



   /*******************************************************************************
    ** Setter for title
    *******************************************************************************/
   public void setTitle(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Fluent setter for title
    *******************************************************************************/
   public Info withTitle(String title)
   {
      this.title = title;
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
   public Info withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for termsOfService
    *******************************************************************************/
   public String getTermsOfService()
   {
      return (this.termsOfService);
   }



   /*******************************************************************************
    ** Setter for termsOfService
    *******************************************************************************/
   public void setTermsOfService(String termsOfService)
   {
      this.termsOfService = termsOfService;
   }



   /*******************************************************************************
    ** Fluent setter for termsOfService
    *******************************************************************************/
   public Info withTermsOfService(String termsOfService)
   {
      this.termsOfService = termsOfService;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contact
    *******************************************************************************/
   public Contact getContact()
   {
      return (this.contact);
   }



   /*******************************************************************************
    ** Setter for contact
    *******************************************************************************/
   public void setContact(Contact contact)
   {
      this.contact = contact;
   }



   /*******************************************************************************
    ** Fluent setter for contact
    *******************************************************************************/
   public Info withContact(Contact contact)
   {
      this.contact = contact;
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
   public Info withVersion(String version)
   {
      this.version = version;
      return (this);
   }

}
