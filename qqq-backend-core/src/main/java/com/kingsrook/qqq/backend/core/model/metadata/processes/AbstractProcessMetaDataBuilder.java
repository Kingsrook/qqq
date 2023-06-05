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


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.basepull.BasepullConfiguration;


/*******************************************************************************
 **
 *******************************************************************************/
public class AbstractProcessMetaDataBuilder
{
   protected QProcessMetaData processMetaData;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public AbstractProcessMetaDataBuilder(QProcessMetaData processMetaData)
   {
      this.processMetaData = processMetaData;
   }



   /*******************************************************************************
    ** Getter for processMetaData
    **
    *******************************************************************************/
   public QProcessMetaData getProcessMetaData()
   {
      return processMetaData;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void setInputFieldDefaultValue(String fieldName, Serializable value)
   {
      processMetaData.getInputFields().stream()
         .filter(f -> f.getName().equals(fieldName)).findFirst()
         .ifPresent(f -> f.setDefaultValue(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractProcessMetaDataBuilder withInputFieldDefaultValue(String fieldName, Serializable value)
   {
      setInputFieldDefaultValue(fieldName, value);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractProcessMetaDataBuilder withBasepullConfiguration(BasepullConfiguration basepullConfiguration)
   {
      processMetaData.setBasepullConfiguration(basepullConfiguration);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractProcessMetaDataBuilder withSchedule(QScheduleMetaData schedule)
   {
      processMetaData.setSchedule(schedule);
      return (this);
   }

}
