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
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 * Version of QProcessMetaData that's meant for transmitting to a frontend.
 * e.g., it excludes backend-only details.
 *
 *******************************************************************************/
@JsonInclude(Include.NON_NULL)
public class QFrontendProcessMetaData
{
   private String  name;
   private String  label;
   private String  tableName;
   private boolean isHidden;

   private String iconName;

   private List<QFrontendStepMetaData> frontendSteps;

   //////////////////////////////////////////////////////////////////////////////////
   // do not add setters.  take values from the source-object in the constructor!! //
   //////////////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFrontendProcessMetaData(QProcessMetaData processMetaData, boolean includeSteps)
   {
      this.name = processMetaData.getName();
      this.label = processMetaData.getLabel();
      this.tableName = processMetaData.getTableName();
      this.isHidden = processMetaData.getIsHidden();

      if(includeSteps)
      {
         if(CollectionUtils.nullSafeHasContents(processMetaData.getStepList()))
         {
            this.frontendSteps = processMetaData.getStepList().stream()
               .filter(QFrontendStepMetaData.class::isInstance)
               .map(QFrontendStepMetaData.class::cast)
               .collect(Collectors.toList());
         }
         else
         {
            frontendSteps = new ArrayList<>();
         }
      }

      if(processMetaData.getIcon() != null)
      {
         this.iconName = processMetaData.getIcon().getName();
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
    ** Getter for primaryKeyField
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Getter for frontendSteps
    **
    *******************************************************************************/
   public List<QFrontendStepMetaData> getFrontendSteps()
   {
      return frontendSteps;
   }



   /*******************************************************************************
    ** Setter for frontendSteps
    **
    *******************************************************************************/
   public void setFrontendSteps(List<QFrontendStepMetaData> frontendSteps)
   {
      this.frontendSteps = frontendSteps;
   }



   /*******************************************************************************
    ** Getter for isHidden
    **
    *******************************************************************************/
   public boolean getIsHidden()
   {
      return isHidden;
   }



   /*******************************************************************************
    ** Getter for iconName
    **
    *******************************************************************************/
   public String getIconName()
   {
      return iconName;
   }

}
