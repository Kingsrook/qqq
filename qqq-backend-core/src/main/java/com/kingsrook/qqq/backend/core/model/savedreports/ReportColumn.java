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

package com.kingsrook.qqq.backend.core.model.savedreports;


import java.io.Serializable;


/*******************************************************************************
 ** single entry in ReportColumns object - as part of SavedReport
 *******************************************************************************/
public class ReportColumn implements Serializable
{
   private String  name;
   private Boolean isVisible;



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public ReportColumn withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isVisible
    *******************************************************************************/
   public Boolean getIsVisible()
   {
      return (this.isVisible);
   }



   /*******************************************************************************
    ** Setter for isVisible
    *******************************************************************************/
   public void setIsVisible(Boolean isVisible)
   {
      this.isVisible = isVisible;
   }



   /*******************************************************************************
    ** Fluent setter for isVisible
    *******************************************************************************/
   public ReportColumn withIsVisible(Boolean isVisible)
   {
      this.isVisible = isVisible;
      return (this);
   }

}
