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

package com.kingsrook.qqq.backend.core.model.metadata.dashboard;


/*******************************************************************************
 ** inner class for specifying details about dropdown fields on a parent widget
 **
 *******************************************************************************/
public class WidgetDropdownData
{
   private String  possibleValueSourceName;
   private String  foreignKeyFieldName;
   private String  label;
   private boolean isRequired;



   /*******************************************************************************
    ** Getter for possibleValueSourceName
    **
    *******************************************************************************/
   public String getPossibleValueSourceName()
   {
      return possibleValueSourceName;
   }



   /*******************************************************************************
    ** Setter for possibleValueSourceName
    **
    *******************************************************************************/
   public void setPossibleValueSourceName(String possibleValueSourceName)
   {
      this.possibleValueSourceName = possibleValueSourceName;
   }



   /*******************************************************************************
    ** Fluent setter for possibleValueSourceName
    **
    *******************************************************************************/
   public WidgetDropdownData withPossibleValueSourceName(String possibleValueSourceName)
   {
      this.possibleValueSourceName = possibleValueSourceName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for foreignKeyFieldName
    **
    *******************************************************************************/
   public String getForeignKeyFieldName()
   {
      return foreignKeyFieldName;
   }



   /*******************************************************************************
    ** Setter for foreignKeyFieldName
    **
    *******************************************************************************/
   public void setForeignKeyFieldName(String foreignKeyFieldName)
   {
      this.foreignKeyFieldName = foreignKeyFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for foreignKeyFieldName
    **
    *******************************************************************************/
   public WidgetDropdownData withForeignKeyFieldName(String foreignKeyFieldName)
   {
      this.foreignKeyFieldName = foreignKeyFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    **
    *******************************************************************************/
   public WidgetDropdownData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isRequired
    **
    *******************************************************************************/
   public boolean getIsRequired()
   {
      return isRequired;
   }



   /*******************************************************************************
    ** Setter for isRequired
    **
    *******************************************************************************/
   public void setIsRequired(boolean isRequired)
   {
      this.isRequired = isRequired;
   }



   /*******************************************************************************
    ** Fluent setter for isRequired
    **
    *******************************************************************************/
   public WidgetDropdownData withIsRequired(boolean isRequired)
   {
      this.isRequired = isRequired;
      return (this);
   }

}
