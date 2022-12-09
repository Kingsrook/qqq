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
 ** Model containing datastructure expected by frontend bar raw html widget
 **
 *******************************************************************************/
public class RawHTML extends QWidgetData
{
   private String title;
   private String html;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RawHTML()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public RawHTML(String title, String html)
   {
      setTitle(title);
      setHtml(html);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.HTML.getType();
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
   public RawHTML withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for html
    **
    *******************************************************************************/
   public String getHtml()
   {
      return html;
   }



   /*******************************************************************************
    ** Setter for html
    **
    *******************************************************************************/
   public void setHtml(String html)
   {
      this.html = html;
   }



   /*******************************************************************************
    ** Fluent setter for html
    **
    *******************************************************************************/
   public RawHTML withHtml(String html)
   {
      this.html = html;
      return (this);
   }

}
