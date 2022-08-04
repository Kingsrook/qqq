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
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 * Version of QAppMetaData that's meant for transmitting to a frontend.
 *
 *******************************************************************************/
@JsonInclude(Include.NON_NULL)
public class QFrontendAppMetaData
{
   private AppTreeNodeType type;

   private String name;
   private String label;

   private List<QFrontendAppMetaData> children = new ArrayList<>();

   private String iconName;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFrontendAppMetaData(QAppChildMetaData appChildMetaData)
   {
      this.name = appChildMetaData.getName();
      this.label = appChildMetaData.getLabel();

      if(appChildMetaData.getClass().equals(QTableMetaData.class))
      {
         this.type = AppTreeNodeType.TABLE;
      }
      else if(appChildMetaData.getClass().equals(QProcessMetaData.class))
      {
         this.type = AppTreeNodeType.PROCESS;
      }
      else if(appChildMetaData.getClass().equals(QAppMetaData.class))
      {
         this.type = AppTreeNodeType.APP;
      }
      else
      {
         throw (new IllegalStateException("Unrecognized class for app child meta data: " + appChildMetaData.getClass()));
      }

      if(appChildMetaData.getIcon() != null && StringUtils.hasContent(appChildMetaData.getIcon().getName()))
      {
         this.iconName = appChildMetaData.getIcon().getName();
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
    ** Getter for type
    **
    *******************************************************************************/
   public AppTreeNodeType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Getter for children
    **
    *******************************************************************************/
   public List<QFrontendAppMetaData> getChildren()
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
   public void addChild(QFrontendAppMetaData qFrontendAppMetaData)
   {
      if(children == null)
      {
         children = new ArrayList<>();
      }
      children.add(qFrontendAppMetaData);
   }
}
