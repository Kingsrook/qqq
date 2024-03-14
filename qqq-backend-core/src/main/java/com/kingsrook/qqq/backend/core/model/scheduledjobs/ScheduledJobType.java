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

package com.kingsrook.qqq.backend.core.model.scheduledjobs;


import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;


/*******************************************************************************
 **
 *******************************************************************************/
public enum ScheduledJobType implements PossibleValueEnum<String>
{
   PROCESS,
   QUEUE_PROCESSOR,
   TABLE_AUTOMATIONS,
   // todo - future - USER_REPORT
   ;

   public static final String NAME = "scheduledJobType";

   private final String label;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   ScheduledJobType()
   {
      this.label = QInstanceEnricher.nameToLabel(QInstanceEnricher.inferNameFromBackendName(name()));
   }



   /*******************************************************************************
    ** Get instance by id
    **
    *******************************************************************************/
   public static ScheduledJobType getById(String id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ScheduledJobType value : ScheduledJobType.values())
      {
         if(value.name().equals(id))
         {
            return (value);
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public String getId()
   {
      return name();
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
    **
    *******************************************************************************/
   @Override
   public String getPossibleValueId()
   {
      return name();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getPossibleValueLabel()
   {
      return (label);
   }

}
