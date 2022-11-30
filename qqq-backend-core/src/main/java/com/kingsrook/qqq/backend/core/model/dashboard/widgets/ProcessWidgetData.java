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


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Model containing datastructure expected by frontend process widget
 **
 *******************************************************************************/
public class ProcessWidgetData implements QWidget
{
   private QProcessMetaData          processMetaData;
   private Map<String, Serializable> defaultValues;



   /*******************************************************************************
    ** Getter for processMetaData
    **
    *******************************************************************************/
   public QProcessMetaData getProcessMetaData()
   {
      return processMetaData;
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.PROCESS.getType();
   }



   /*******************************************************************************
    ** Setter for processMetaData
    **
    *******************************************************************************/
   public void setProcessMetaData(QProcessMetaData processMetaData)
   {
      this.processMetaData = processMetaData;
   }



   /*******************************************************************************
    ** Fluent setter for processMetaData
    **
    *******************************************************************************/
   public ProcessWidgetData withProcessMetaData(QProcessMetaData processMetaData)
   {
      this.processMetaData = processMetaData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultValues
    **
    *******************************************************************************/
   public Map<String, Serializable> getDefaultValues()
   {
      return defaultValues;
   }



   /*******************************************************************************
    ** Setter for defaultValues
    **
    *******************************************************************************/
   public void setDefaultValues(Map<String, Serializable> defaultValues)
   {
      this.defaultValues = defaultValues;
   }



   /*******************************************************************************
    ** Fluent setter for defaultValues
    **
    *******************************************************************************/
   public ProcessWidgetData withDefaultValues(Map<String, Serializable> defaultValues)
   {
      this.defaultValues = defaultValues;
      return (this);
   }

}
