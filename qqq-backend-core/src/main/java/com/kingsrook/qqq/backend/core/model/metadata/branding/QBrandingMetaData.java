/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.branding;


/*******************************************************************************
 ** Meta-Data to define branding in a QQQ instance.
 **
 *******************************************************************************/
public class QBrandingMetaData
{
   private String companyName;
   private String logo;
   private String icon;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return ("QBrandingMetaData[" + companyName + "]");
   }



   /*******************************************************************************
    ** Getter for companyName
    **
    *******************************************************************************/
   public String getCompanyName()
   {
      return companyName;
   }



   /*******************************************************************************
    ** Setter for companyName
    **
    *******************************************************************************/
   public void setCompanyName(String companyName)
   {
      this.companyName = companyName;
   }



   /*******************************************************************************
    ** Fluent setter for companyName
    **
    *******************************************************************************/
   public QBrandingMetaData withCompanyName(String companyName)
   {
      this.companyName = companyName;
      return this;
   }



   /*******************************************************************************
    ** Getter for logo
    **
    *******************************************************************************/
   public String getLogo()
   {
      return logo;
   }



   /*******************************************************************************
    ** Setter for logo
    **
    *******************************************************************************/
   public void setLogo(String logo)
   {
      this.logo = logo;
   }



   /*******************************************************************************
    ** Fluent setter for logo
    **
    *******************************************************************************/
   public QBrandingMetaData withLogo(String logo)
   {
      this.logo = logo;
      return this;
   }



   /*******************************************************************************
    ** Getter for icon
    **
    *******************************************************************************/
   public String getIcon()
   {
      return icon;
   }



   /*******************************************************************************
    ** Setter for icon
    **
    *******************************************************************************/
   public void setIcon(String icon)
   {
      this.icon = icon;
   }



   /*******************************************************************************
    ** Fluent setter for icon
    **
    *******************************************************************************/
   public QBrandingMetaData withIcon(String icon)
   {
      this.icon = icon;
      return this;
   }

}