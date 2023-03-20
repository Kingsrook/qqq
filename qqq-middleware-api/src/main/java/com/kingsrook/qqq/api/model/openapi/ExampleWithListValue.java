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


/*******************************************************************************
 **
 *******************************************************************************/
public class ExampleWithListValue extends Example
{
   private List<String> value;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ExampleWithListValue withSummary(String summary)
   {
      super.withSummary(summary);
      return (this);
   }



   /*******************************************************************************
    ** Getter for value
    *******************************************************************************/
   public List<String> getValue()
   {
      return (this.value);
   }



   /*******************************************************************************
    ** Setter for value
    *******************************************************************************/
   public void setValue(List<String> value)
   {
      this.value = value;
   }



   /*******************************************************************************
    ** Fluent setter for value
    *******************************************************************************/
   public ExampleWithListValue withValue(List<String> value)
   {
      this.value = value;
      return (this);
   }

}
