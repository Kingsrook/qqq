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

package com.kingsrook.qqq.backend.core.model.metadata.frontend;


import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 * Version of QAppMetaData that's meant for transmitting to a frontend.
 *
 *******************************************************************************/
@JsonInclude(Include.NON_NULL)
public class QFrontendAppMetaData
{
   private String name;
   private String label;

   private List<AppTreeNode> children = new ArrayList<>();
   private List<String>      widgets  = new ArrayList<>();

   private String iconName;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFrontendAppMetaData(QAppMetaData appMetaData)
   {
      this.name = appMetaData.getName();
      this.label = appMetaData.getLabel();

      if(appMetaData.getIcon() != null)
      {
         this.iconName = appMetaData.getIcon().getName();
      }

      if(CollectionUtils.nullSafeHasContents(appMetaData.getWidgets()))
      {
         this.widgets = appMetaData.getWidgets();
      }
   }



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
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
    ** Getter for children
    **
    *******************************************************************************/
   public List<AppTreeNode> getChildren()
   {
      return children;
   }



   /*******************************************************************************
    ** Getter for iconName
    **
    *******************************************************************************/
   public String getIconName()
   {
      return iconName;
   }



   /*******************************************************************************
    ** Setter for iconName
    **
    *******************************************************************************/
   public void setIconName(String iconName)
   {
      this.iconName = iconName;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addChild(AppTreeNode childAppTreeNode)
   {
      if(children == null)
      {
         children = new ArrayList<>();
      }
      children.add(childAppTreeNode);
   }



   /*******************************************************************************
    ** Getter for widgets
    **
    *******************************************************************************/
   public List<String> getWidgets()
   {
      return widgets;
   }
}
