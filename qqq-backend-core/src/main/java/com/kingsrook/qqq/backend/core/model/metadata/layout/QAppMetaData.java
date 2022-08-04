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

package com.kingsrook.qqq.backend.core.model.metadata.layout;


import java.util.ArrayList;
import java.util.List;


/*******************************************************************************
 **
 *******************************************************************************/
public class QAppMetaData implements QAppChildMetaData
{
   private String name;
   private String label;

   private List<QAppChildMetaData> children;

   private String parentAppName;
   private QIcon  icon;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAppMetaData()
   {
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
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    **
    *******************************************************************************/
   public QAppMetaData withName(String name)
   {
      this.name = name;
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
   public QAppMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for children
    **
    *******************************************************************************/
   public List<QAppChildMetaData> getChildren()
   {
      return children;
   }



   /*******************************************************************************
    ** Setter for children
    **
    *******************************************************************************/
   public void setChildren(List<QAppChildMetaData> children)
   {
      this.children = children;
   }



   /*******************************************************************************
    ** Add a child to this app.
    **
    *******************************************************************************/
   public void addChild(QAppChildMetaData child)
   {
      if(this.children == null)
      {
         this.children = new ArrayList<>();
      }
      this.children.add(child);
      child.setParentAppName(this.getName());
   }



   /*******************************************************************************
    ** Fluently add a child to this app.
    **
    *******************************************************************************/
   public QAppMetaData withChild(QAppChildMetaData child)
   {
      addChild(child);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for children
    **
    *******************************************************************************/
   public QAppMetaData withChildren(List<QAppChildMetaData> children)
   {
      this.children = children;
      return (this);
   }



   /*******************************************************************************
    ** Getter for parentAppName
    **
    *******************************************************************************/
   @Override
   public String getParentAppName()
   {
      return parentAppName;
   }



   /*******************************************************************************
    ** Setter for parentAppName
    **
    *******************************************************************************/
   @Override
   public void setParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
   }


   /*******************************************************************************
    ** Getter for icon
    **
    *******************************************************************************/
   public QIcon getIcon()
   {
      return icon;
   }



   /*******************************************************************************
    ** Setter for icon
    **
    *******************************************************************************/
   public void setIcon(QIcon icon)
   {
      this.icon = icon;
   }


   /*******************************************************************************
    ** Fluent setter for icon
    **
    *******************************************************************************/
   public QAppMetaData withIcon(QIcon icon)
   {
      this.icon = icon;
      return (this);
   }

}
