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

package com.kingsrook.qqq.backend.core.actions.automation;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;


/*******************************************************************************
 ** enum of possible values for a record's Automation Status.
 *******************************************************************************/
public enum AutomationStatus implements PossibleValueEnum<Integer>
{
   PENDING_INSERT_AUTOMATIONS(1, "Pending Insert Automations"),
   RUNNING_INSERT_AUTOMATIONS(2, "Running Insert Automations"),
   FAILED_INSERT_AUTOMATIONS(3, "Failed Insert Automations"),
   PENDING_UPDATE_AUTOMATIONS(4, "Pending Update Automations"),
   RUNNING_UPDATE_AUTOMATIONS(5, "Running Update Automations"),
   FAILED_UPDATE_AUTOMATIONS(6, "Failed Update Automations"),
   OK(7, "OK");


   private final Integer id;
   private final String  label;



   /*******************************************************************************
    **
    *******************************************************************************/
   AutomationStatus(int id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    **
    *******************************************************************************/
   public static AutomationStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(AutomationStatus value : AutomationStatus.values())
      {
         if(Objects.equals(value.id, id))
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
   public Integer getId()
   {
      return (id);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return (label);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer getPossibleValueId()
   {
      return (getId());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getPossibleValueLabel()
   {
      return (getLabel());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:indentation")
   public String getInsertOrUpdate()
   {
      return switch(this)
      {
         case PENDING_INSERT_AUTOMATIONS, RUNNING_INSERT_AUTOMATIONS, FAILED_INSERT_AUTOMATIONS -> "Insert";
         case PENDING_UPDATE_AUTOMATIONS, RUNNING_UPDATE_AUTOMATIONS, FAILED_UPDATE_AUTOMATIONS -> "Update";
         case OK -> "";
      };
   }
}
