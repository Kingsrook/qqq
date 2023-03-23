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
public class SecurityScheme
{
   private String type;
   private String scheme;
   private String bearerFormat;



   /*******************************************************************************
    ** Getter for type
    *******************************************************************************/
   public String getType()
   {
      return (this.type);
   }



   /*******************************************************************************
    ** Setter for type
    *******************************************************************************/
   public void setType(String type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    *******************************************************************************/
   public SecurityScheme withType(String type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scheme
    *******************************************************************************/
   public String getScheme()
   {
      return (this.scheme);
   }



   /*******************************************************************************
    ** Setter for scheme
    *******************************************************************************/
   public void setScheme(String scheme)
   {
      this.scheme = scheme;
   }



   /*******************************************************************************
    ** Fluent setter for scheme
    *******************************************************************************/
   public SecurityScheme withScheme(String scheme)
   {
      this.scheme = scheme;
      return (this);
   }



   /*******************************************************************************
    ** Getter for bearerFormat
    *******************************************************************************/
   public String getBearerFormat()
   {
      return (this.bearerFormat);
   }



   /*******************************************************************************
    ** Setter for bearerFormat
    *******************************************************************************/
   public void setBearerFormat(String bearerFormat)
   {
      this.bearerFormat = bearerFormat;
   }



   /*******************************************************************************
    ** Fluent setter for bearerFormat
    *******************************************************************************/
   public SecurityScheme withBearerFormat(String bearerFormat)
   {
      this.bearerFormat = bearerFormat;
      return (this);
   }

}
