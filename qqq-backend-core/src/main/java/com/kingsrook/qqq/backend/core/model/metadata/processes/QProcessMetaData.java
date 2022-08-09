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

package com.kingsrook.qqq.backend.core.model.metadata.processes;


import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;


/*******************************************************************************
 ** Meta-Data to define a process in a QQQ instance.
 **
 *******************************************************************************/
public class QProcessMetaData implements QAppChildMetaData
{
   private String  name;
   private String  label;
   private String  tableName;
   private boolean isHidden = false;

   private List<QStepMetaData> stepList;

   private String parentAppName;
   private QIcon  icon;



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
    ** Setter for name
    **
    *******************************************************************************/
   public QProcessMetaData withName(String name)
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
    ** Setter for label
    **
    *******************************************************************************/
   public QProcessMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public QProcessMetaData withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for stepList
    **
    *******************************************************************************/
   public List<QStepMetaData> getStepList()
   {
      return stepList;
   }



   /*******************************************************************************
    ** Setter for stepList
    **
    *******************************************************************************/
   public QProcessMetaData withStepList(List<QStepMetaData> stepList)
   {
      this.stepList = stepList;
      return (this);
   }



   /*******************************************************************************
    ** Setter for stepList
    **
    *******************************************************************************/
   public QProcessMetaData addStep(QStepMetaData step)
   {
      if(this.stepList == null)
      {
         this.stepList = new ArrayList<>();
      }
      this.stepList.add(step);
      return (this);
   }



   /*******************************************************************************
    ** Setter for stepList
    **
    *******************************************************************************/
   public void setStepList(List<QStepMetaData> stepList)
   {
      this.stepList = stepList;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QStepMetaData getStep(String stepName)
   {
      for(QStepMetaData step : stepList)
      {
         if(step.getName().equals(stepName))
         {
            return (step);
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Wrapper to getStep, that internally casts to BackendStepMetaData
    *******************************************************************************/
   public QBackendStepMetaData getBackendStep(String name)
   {
      return (QBackendStepMetaData) getStep(name);
   }



   /*******************************************************************************
    ** Get a list of all of the input fields used by all the steps in this process.
    *******************************************************************************/
   @JsonIgnore
   public List<QFieldMetaData> getInputFields()
   {
      List<QFieldMetaData> rs = new ArrayList<>();
      if(stepList != null)
      {
         for(QStepMetaData step : stepList)
         {
            rs.addAll(step.getInputFields());
         }
      }
      return (rs);
   }



   /*******************************************************************************
    ** Get a list of all of the output fields used by all the steps in this process.
    *******************************************************************************/
   @JsonIgnore
   public List<QFieldMetaData> getOutputFields()
   {
      List<QFieldMetaData> rs = new ArrayList<>();
      if(stepList != null)
      {
         for(QStepMetaData step : stepList)
         {
            rs.addAll(step.getOutputFields());
         }
      }
      return (rs);
   }



   /*******************************************************************************
    ** Getter for isHidden
    **
    *******************************************************************************/
   public boolean getIsHidden()
   {
      return (isHidden);
   }



   /*******************************************************************************
    ** Setter for isHidden
    **
    *******************************************************************************/
   public void setIsHidden(boolean isHidden)
   {
      this.isHidden = isHidden;
   }



   /*******************************************************************************
    ** Fluent Setter for isHidden
    **
    *******************************************************************************/
   public QProcessMetaData withIsHidden(boolean isHidden)
   {
      this.isHidden = isHidden;
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
   public QProcessMetaData withIcon(QIcon icon)
   {
      this.icon = icon;
      return (this);
   }

}
