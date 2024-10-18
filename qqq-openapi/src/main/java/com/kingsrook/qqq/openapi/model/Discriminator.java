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

package com.kingsrook.qqq.openapi.model;


import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class Discriminator
{
   private String              propertyName;
   private Map<String, String> mapping;



   /*******************************************************************************
    ** Getter for propertyName
    *******************************************************************************/
   public String getPropertyName()
   {
      return (this.propertyName);
   }



   /*******************************************************************************
    ** Setter for propertyName
    *******************************************************************************/
   public void setPropertyName(String propertyName)
   {
      this.propertyName = propertyName;
   }



   /*******************************************************************************
    ** Fluent setter for propertyName
    *******************************************************************************/
   public Discriminator withPropertyName(String propertyName)
   {
      this.propertyName = propertyName;
      return (this);
   }


   /*******************************************************************************
    ** Getter for mapping
    *******************************************************************************/
   public Map<String, String> getMapping()
   {
      return (this.mapping);
   }



   /*******************************************************************************
    ** Setter for mapping
    *******************************************************************************/
   public void setMapping(Map<String, String> mapping)
   {
      this.mapping = mapping;
   }



   /*******************************************************************************
    ** Fluent setter for mapping
    *******************************************************************************/
   public Discriminator withMapping(Map<String, String> mapping)
   {
      this.mapping = mapping;
      return (this);
   }


}
