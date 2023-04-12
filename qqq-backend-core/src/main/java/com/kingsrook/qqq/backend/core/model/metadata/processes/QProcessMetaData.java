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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.TopLevelMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppChildMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.MetaDataWithPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.basepull.BasepullConfiguration;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Meta-Data to define a process in a QQQ instance.
 **
 *******************************************************************************/
public class QProcessMetaData implements QAppChildMetaData, MetaDataWithPermissionRules, TopLevelMetaDataInterface
{
   private String                name;
   private String                label;
   private String                tableName;
   private boolean               isHidden = false;
   private BasepullConfiguration basepullConfiguration;
   private QPermissionRules      permissionRules;

   private List<QStepMetaData>        stepList; // these are the steps that are ran, by-default, in the order they are ran in
   private Map<String, QStepMetaData> steps; // this is the full map of possible steps

   private QIcon icon;

   private QScheduleMetaData schedule;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return ("QProcessMetaData[" + name + "]");
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
      if(stepList != null)
      {
         stepList.forEach(this::addStep);
      }

      return (this);
   }



   /*******************************************************************************
    ** add a step to the stepList and map
    **
    *******************************************************************************/
   public QProcessMetaData addStep(QStepMetaData step)
   {
      int index = 0;
      if(this.stepList != null)
      {
         index = this.stepList.size();
      }
      addStep(index, step);

      return (this);
   }



   /*******************************************************************************
    ** add a step to the stepList (at the specified index) and the step map
    **
    *******************************************************************************/
   public QProcessMetaData addStep(int index, QStepMetaData step)
   {
      if(this.stepList == null)
      {
         this.stepList = new ArrayList<>();
      }
      this.stepList.add(index, step);

      if(this.steps == null)
      {
         this.steps = new HashMap<>();
      }

      if(!StringUtils.hasContent(step.getName()))
      {
         throw (new IllegalArgumentException("Attempt to add a process step without a name"));
      }

      this.steps.put(step.getName(), step);

      return (this);
   }



   /*******************************************************************************
    ** add a step ONLY to the step map - NOT the list w/ default execution order.
    **
    *******************************************************************************/
   public QProcessMetaData addOptionalStep(QStepMetaData step)
   {
      if(this.steps == null)
      {
         this.steps = new HashMap<>();
      }

      if(!StringUtils.hasContent(step.getName()))
      {
         throw (new IllegalArgumentException("Attempt to add a process step without a name"));
      }

      this.steps.put(step.getName(), step);

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
      return (steps.get(stepName));
   }



   /*******************************************************************************
    ** Wrapper to getStep, that internally casts to BackendStepMetaData
    *******************************************************************************/
   public QBackendStepMetaData getBackendStep(String name)
   {
      return (QBackendStepMetaData) getStep(name);
   }



   /*******************************************************************************
    ** Wrapper to getStep, that internally casts to FrontendStepMetaData
    *******************************************************************************/
   public QFrontendStepMetaData getFrontendStep(String name)
   {
      return (QFrontendStepMetaData) getStep(name);
   }



   /*******************************************************************************
    ** Get a list of all the *unique* input fields used by all the steps in this process.
    *******************************************************************************/
   @JsonIgnore
   public List<QFieldMetaData> getInputFields()
   {
      Set<String>          usedFieldNames = new HashSet<>();
      List<QFieldMetaData> rs             = new ArrayList<>();
      if(steps != null)
      {
         for(QStepMetaData step : steps.values())
         {
            for(QFieldMetaData field : step.getInputFields())
            {
               if(!usedFieldNames.contains(field.getName()))
               {
                  rs.add(field);
                  usedFieldNames.add(field.getName());
               }
            }
         }
      }
      return (rs);
   }



   /*******************************************************************************
    ** Get a list of all the *unique* output fields used by all the steps in this process.
    *******************************************************************************/
   @JsonIgnore
   public List<QFieldMetaData> getOutputFields()
   {
      Set<String>          usedFieldNames = new HashSet<>();
      List<QFieldMetaData> rs             = new ArrayList<>();
      if(steps != null)
      {
         for(QStepMetaData step : steps.values())
         {
            for(QFieldMetaData field : step.getOutputFields())
            {
               if(!usedFieldNames.contains(field.getName()))
               {
                  rs.add(field);
                  usedFieldNames.add(field.getName());
               }
            }
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



   /*******************************************************************************
    ** Getter for schedule
    **
    *******************************************************************************/
   public QScheduleMetaData getSchedule()
   {
      return schedule;
   }



   /*******************************************************************************
    ** Setter for schedule
    **
    *******************************************************************************/
   public void setSchedule(QScheduleMetaData schedule)
   {
      this.schedule = schedule;
   }



   /*******************************************************************************
    ** Fluent setter for schedule
    **
    *******************************************************************************/
   public QProcessMetaData withSchedule(QScheduleMetaData schedule)
   {
      this.schedule = schedule;
      return (this);
   }



   /*******************************************************************************
    ** Getter for basepullConfiguration
    **
    *******************************************************************************/
   public BasepullConfiguration getBasepullConfiguration()
   {
      return basepullConfiguration;
   }



   /*******************************************************************************
    ** Setter for basepullConfiguration
    **
    *******************************************************************************/
   public void setBasepullConfiguration(BasepullConfiguration basepullConfiguration)
   {
      this.basepullConfiguration = basepullConfiguration;
   }



   /*******************************************************************************
    ** Fluent setter for basepullConfiguration
    **
    *******************************************************************************/
   public QProcessMetaData withBasepullConfiguration(BasepullConfiguration basepullConfiguration)
   {
      this.basepullConfiguration = basepullConfiguration;
      return (this);
   }



   /*******************************************************************************
    ** Getter for permissionRules
    *******************************************************************************/
   public QPermissionRules getPermissionRules()
   {
      return (this.permissionRules);
   }



   /*******************************************************************************
    ** Setter for permissionRules
    *******************************************************************************/
   public void setPermissionRules(QPermissionRules permissionRules)
   {
      this.permissionRules = permissionRules;
   }



   /*******************************************************************************
    ** Fluent setter for permissionRules
    *******************************************************************************/
   public QProcessMetaData withPermissionRules(QPermissionRules permissionRules)
   {
      this.permissionRules = permissionRules;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void addSelfToInstance(QInstance qInstance)
   {
      qInstance.addProcess(this);
   }

}
