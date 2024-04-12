/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.helpcontent;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpRole;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;


/*******************************************************************************
 ** HelpContentRole - possible value enum
 *******************************************************************************/
public enum HelpContentRole implements PossibleValueEnum<String>
{
   ALL_SCREENS(QHelpRole.ALL_SCREENS.name(), "All Screens"),
   READ_SCREENS(QHelpRole.READ_SCREENS.name(), "Query & View Screens"),
   WRITE_SCREENS(QHelpRole.WRITE_SCREENS.name(), "Insert & Edit Screens"),
   QUERY_SCREEN(QHelpRole.QUERY_SCREEN.name(), "Query Screen Only"),
   VIEW_SCREEN(QHelpRole.VIEW_SCREEN.name(), "View Screen Only"),
   EDIT_SCREEN(QHelpRole.EDIT_SCREEN.name(), "Edit Screen Only"),
   INSERT_SCREEN(QHelpRole.INSERT_SCREEN.name(), "Insert Screen Only"),
   PROCESS_SCREEN(QHelpRole.PROCESS_SCREEN.name(), "Process Screens");


   private final String id;
   private final String label;

   public static final String NAME = "HelpContentRole";



   /*******************************************************************************
    **
    *******************************************************************************/
   HelpContentRole(String id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    **
    *******************************************************************************/
   public static HelpContentRole getById(String id)
   {
      if(id == null)
      {
         return (null);
      }

      for(HelpContentRole value : HelpContentRole.values())
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
   public String getId()
   {
      return id;
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
}
