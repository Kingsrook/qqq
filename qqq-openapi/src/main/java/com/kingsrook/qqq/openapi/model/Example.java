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


import com.fasterxml.jackson.annotation.JsonGetter;


/*******************************************************************************
 **
 *******************************************************************************/
public class Example
{
   private String summary;
   private String ref;
   private Object value;



   /*******************************************************************************
    ** Getter for summary
    *******************************************************************************/
   public String getSummary()
   {
      return (this.summary);
   }



   /*******************************************************************************
    ** Setter for summary
    *******************************************************************************/
   public void setSummary(String summary)
   {
      this.summary = summary;
   }



   /*******************************************************************************
    ** Fluent setter for summary
    *******************************************************************************/
   public Example withSummary(String summary)
   {
      this.summary = summary;
      return (this);
   }



   /*******************************************************************************
    ** Getter for ref
    *******************************************************************************/
   @JsonGetter("$ref")
   public String getRef()
   {
      return (this.ref);
   }



   /*******************************************************************************
    ** Setter for ref
    *******************************************************************************/
   public void setRef(String ref)
   {
      this.ref = ref;
   }



   /*******************************************************************************
    ** Fluent setter for ref
    *******************************************************************************/
   public Example withRef(String ref)
   {
      this.ref = ref;
      return (this);
   }


   /*******************************************************************************
    ** Getter for value
    *******************************************************************************/
   public Object getValue()
   {
      return (this.value);
   }



   /*******************************************************************************
    ** Setter for value
    *******************************************************************************/
   public void setValue(Object value)
   {
      this.value = value;
   }



   /*******************************************************************************
    ** Fluent setter for value
    *******************************************************************************/
   public Example withValue(Object value)
   {
      this.value = value;
      return (this);
   }


}
