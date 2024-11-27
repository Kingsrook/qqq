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

package com.kingsrook.qqq.middleware.javalin.executors.io;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;


/*******************************************************************************
 **
 *******************************************************************************/
public class ProcessInitOrStepInput extends AbstractMiddlewareInput
{
   private String       processName;
   private Integer      stepTimeoutMillis = 3000;
   private QQueryFilter recordsFilter;

   private Map<String, Serializable> values = new LinkedHashMap<>();

   /////////////////////////////////////
   // used only for 'step' (not init) //
   /////////////////////////////////////
   private String processUUID;
   private String startAfterStep;
   // todo - add (in next version?) startAtStep (for back)

   private RunProcessInput.FrontendStepBehavior frontendStepBehavior = RunProcessInput.FrontendStepBehavior.BREAK;

   // todo - file??



   /***************************************************************************
    **
    ***************************************************************************/
   public enum RecordsParam
   {
      FILTER_JSON("filterJSON"),
      RECORD_IDS("recordIds");


      private final String value;



      /***************************************************************************
       **
       ***************************************************************************/
      RecordsParam(String value)
      {
         this.value = value;
      }
   }



   /*******************************************************************************
    ** Getter for processName
    *******************************************************************************/
   public String getProcessName()
   {
      return (this.processName);
   }



   /*******************************************************************************
    ** Setter for processName
    *******************************************************************************/
   public void setProcessName(String processName)
   {
      this.processName = processName;
   }



   /*******************************************************************************
    ** Fluent setter for processName
    *******************************************************************************/
   public ProcessInitOrStepInput withProcessName(String processName)
   {
      this.processName = processName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for stepTimeoutMillis
    *******************************************************************************/
   public Integer getStepTimeoutMillis()
   {
      return (this.stepTimeoutMillis);
   }



   /*******************************************************************************
    ** Setter for stepTimeoutMillis
    *******************************************************************************/
   public void setStepTimeoutMillis(Integer stepTimeoutMillis)
   {
      this.stepTimeoutMillis = stepTimeoutMillis;
   }



   /*******************************************************************************
    ** Fluent setter for stepTimeoutMillis
    *******************************************************************************/
   public ProcessInitOrStepInput withStepTimeoutMillis(Integer stepTimeoutMillis)
   {
      this.stepTimeoutMillis = stepTimeoutMillis;
      return (this);
   }




   /*******************************************************************************
    ** Getter for recordsFilter
    *******************************************************************************/
   public QQueryFilter getRecordsFilter()
   {
      return (this.recordsFilter);
   }



   /*******************************************************************************
    ** Setter for recordsFilter
    *******************************************************************************/
   public void setRecordsFilter(QQueryFilter recordsFilter)
   {
      this.recordsFilter = recordsFilter;
   }



   /*******************************************************************************
    ** Fluent setter for recordsFilter
    *******************************************************************************/
   public ProcessInitOrStepInput withRecordsFilter(QQueryFilter recordsFilter)
   {
      this.recordsFilter = recordsFilter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return (this.values);
   }



   /*******************************************************************************
    ** Setter for values
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Fluent setter for values
    *******************************************************************************/
   public ProcessInitOrStepInput withValues(Map<String, Serializable> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    ** Getter for frontendStepBehavior
    *******************************************************************************/
   public RunProcessInput.FrontendStepBehavior getFrontendStepBehavior()
   {
      return (this.frontendStepBehavior);
   }



   /*******************************************************************************
    ** Setter for frontendStepBehavior
    *******************************************************************************/
   public void setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior frontendStepBehavior)
   {
      this.frontendStepBehavior = frontendStepBehavior;
   }



   /*******************************************************************************
    ** Fluent setter for frontendStepBehavior
    *******************************************************************************/
   public ProcessInitOrStepInput withFrontendStepBehavior(RunProcessInput.FrontendStepBehavior frontendStepBehavior)
   {
      this.frontendStepBehavior = frontendStepBehavior;
      return (this);
   }



   /*******************************************************************************
    ** Getter for processUUID
    *******************************************************************************/
   public String getProcessUUID()
   {
      return (this.processUUID);
   }



   /*******************************************************************************
    ** Setter for processUUID
    *******************************************************************************/
   public void setProcessUUID(String processUUID)
   {
      this.processUUID = processUUID;
   }



   /*******************************************************************************
    ** Fluent setter for processUUID
    *******************************************************************************/
   public ProcessInitOrStepInput withProcessUUID(String processUUID)
   {
      this.processUUID = processUUID;
      return (this);
   }



   /*******************************************************************************
    ** Getter for startAfterStep
    *******************************************************************************/
   public String getStartAfterStep()
   {
      return (this.startAfterStep);
   }



   /*******************************************************************************
    ** Setter for startAfterStep
    *******************************************************************************/
   public void setStartAfterStep(String startAfterStep)
   {
      this.startAfterStep = startAfterStep;
   }



   /*******************************************************************************
    ** Fluent setter for startAfterStep
    *******************************************************************************/
   public ProcessInitOrStepInput withStartAfterStep(String startAfterStep)
   {
      this.startAfterStep = startAfterStep;
      return (this);
   }

}
