/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** A step for a state-machine flow based Process.
 **
 ** Consists of 1 or 2 sub-steps, which are frontend and/or backend.
 *******************************************************************************/
public class QStateMachineStep extends QStepMetaData
{
   private List<QStepMetaData> subSteps = new ArrayList<>();

   private String defaultNextStepName;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   private QStateMachineStep(List<QStepMetaData> subSteps)
   {
      setStepType("stateMachine");
      this.subSteps.addAll(subSteps);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static QStateMachineStep frontendOnly(String name, QFrontendStepMetaData frontendStepMetaData)
   {
      if(!StringUtils.hasContent(frontendStepMetaData.getName()))
      {
         frontendStepMetaData.setName(name + ".frontend");
      }

      return (new QStateMachineStep(List.of(frontendStepMetaData)).withName(name));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static QStateMachineStep backendOnly(String name, QBackendStepMetaData backendStepMetaData)
   {
      if(!StringUtils.hasContent(backendStepMetaData.getName()))
      {
         backendStepMetaData.setName(name + ".backend");
      }

      return (new QStateMachineStep(List.of(backendStepMetaData)).withName(name));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static QStateMachineStep frontendThenBackend(String name, QFrontendStepMetaData frontendStepMetaData, QBackendStepMetaData backendStepMetaData)
   {
      if(!StringUtils.hasContent(frontendStepMetaData.getName()))
      {
         frontendStepMetaData.setName(name + ".frontend");
      }

      if(!StringUtils.hasContent(backendStepMetaData.getName()))
      {
         backendStepMetaData.setName(name + ".backend");
      }

      return (new QStateMachineStep(List.of(frontendStepMetaData, backendStepMetaData)).withName(name));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QStateMachineStep withName(String name)
   {
      super.withName(name);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QStateMachineStep withLabel(String label)
   {
      super.withLabel(label);
      return (this);
   }



   /*******************************************************************************
    ** Getter for subSteps
    **
    *******************************************************************************/
   public List<QStepMetaData> getSubSteps()
   {
      return subSteps;
   }



   /*******************************************************************************
    ** Getter for defaultNextStepName
    *******************************************************************************/
   public String getDefaultNextStepName()
   {
      return (this.defaultNextStepName);
   }



   /*******************************************************************************
    ** Setter for defaultNextStepName
    *******************************************************************************/
   public void setDefaultNextStepName(String defaultNextStepName)
   {
      this.defaultNextStepName = defaultNextStepName;
   }



   /*******************************************************************************
    ** Fluent setter for defaultNextStepName
    *******************************************************************************/
   public QStateMachineStep withDefaultNextStepName(String defaultNextStepName)
   {
      this.defaultNextStepName = defaultNextStepName;
      return (this);
   }



   /*******************************************************************************
    ** Get a list of all of the input fields used by this step (all of its sub-steps)
    *******************************************************************************/
   @JsonIgnore
   @Override
   public List<QFieldMetaData> getInputFields()
   {
      List<QFieldMetaData> rs = new ArrayList<>();
      for(QStepMetaData subStep : subSteps)
      {
         rs.addAll(subStep.getInputFields());
      }
      return (rs);
   }

}
