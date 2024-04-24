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
 ** Model containing datastructure expected by frontend alert widget
 **
 *******************************************************************************/
public class AlertData extends QWidgetData
{
   public enum AlertType
   {
      ERROR,
      SUCCESS,
      WARNING
   }



   private String    html;
   private AlertType alertType;



   /*******************************************************************************
    **
    *******************************************************************************/
   public AlertData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AlertData(AlertType alertType, String html)
   {
      setHtml(html);
      setAlertType(alertType);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.ALERT.getType();
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
   public AlertData withHtml(String html)
   {
      this.html = html;
      return (this);
   }



   /*******************************************************************************
    ** Getter for alertType
    *******************************************************************************/
   public AlertType getAlertType()
   {
      return (this.alertType);
   }



   /*******************************************************************************
    ** Setter for alertType
    *******************************************************************************/
   public void setAlertType(AlertType alertType)
   {
      this.alertType = alertType;
   }



   /*******************************************************************************
    ** Fluent setter for alertType
    *******************************************************************************/
   public AlertData withAlertType(AlertType alertType)
   {
      this.alertType = alertType;
      return (this);
   }

}
