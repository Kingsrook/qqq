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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets;


/*******************************************************************************
 ** Model containing datastructure expected by frontend location widget
 **
 *******************************************************************************/
public class LocationData implements QWidget
{
   private String imageUrl;
   private String title;
   private String description;
   private String footerText;
   private String location;



   /*******************************************************************************
    **
    *******************************************************************************/
   public LocationData(String imageUrl, String title, String description, String location, String footerText)
   {
      this.imageUrl = imageUrl;
      this.title = title;
      this.description = description;
      this.location = location;
      this.footerText = footerText;
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.LOCATION.getType();
   }



   /*******************************************************************************
    ** Getter for imageUrl
    **
    *******************************************************************************/
   public String getImageUrl()
   {
      return imageUrl;
   }



   /*******************************************************************************
    ** Setter for imageUrl
    **
    *******************************************************************************/
   public void setImageUrl(String imageUrl)
   {
      this.imageUrl = imageUrl;
   }



   /*******************************************************************************
    ** Fluent setter for imageUrl
    **
    *******************************************************************************/
   public LocationData withImageUrl(String imageUrl)
   {
      this.imageUrl = imageUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    **
    *******************************************************************************/
   public String getDescription()
   {
      return description;
   }



   /*******************************************************************************
    ** Setter for description
    **
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    **
    *******************************************************************************/
   public LocationData withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for footerText
    **
    *******************************************************************************/
   public String getFooterText()
   {
      return footerText;
   }



   /*******************************************************************************
    ** Setter for footerText
    **
    *******************************************************************************/
   public void setFooterText(String footerText)
   {
      this.footerText = footerText;
   }



   /*******************************************************************************
    ** Fluent setter for footerText
    **
    *******************************************************************************/
   public LocationData withFooterText(String footerText)
   {
      this.footerText = footerText;
      return (this);
   }



   /*******************************************************************************
    ** Getter for title
    **
    *******************************************************************************/
   public String getTitle()
   {
      return title;
   }



   /*******************************************************************************
    ** Setter for title
    **
    *******************************************************************************/
   public void setTitle(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Fluent setter for title
    **
    *******************************************************************************/
   public LocationData withTitle(String title)
   {
      this.title = title;
      return (this);

   }



   /*******************************************************************************
    ** Getter for location
    **
    *******************************************************************************/
   public String getLocation()
   {
      return location;
   }



   /*******************************************************************************
    ** Setter for location
    **
    *******************************************************************************/
   public void setLocation(String location)
   {
      this.location = location;
   }



   /*******************************************************************************
    ** Fluent setter for location
    **
    *******************************************************************************/
   public LocationData withLocation(String location)
   {
      this.location = location;
      return (this);
   }

}
