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

package com.kingsrook.qqq.backend.core.actions.customizers;


/*******************************************************************************
 ** Enum definition of possible table customizers - "roles" for custom code that
 ** can be applied to tables.
 **
 ** Works with TableCustomizer (singular version of this name) objects, during
 ** instance validation, to provide validation of the referenced code (and to
 ** make such validation from sub-backend-modules possible in the future).
 **
 ** The idea of the 3rd argument here is to provide a way that we can enforce
 ** the type-parameters for the custom code.  E.g., if it's a Function - how
 ** can we check at run-time that the type-params are correct?  We couldn't find
 ** how to do this "reflectively", so we can instead try to run the custom code,
 ** passing it objects of the type that this customizer expects, and a validation
 ** error will raise upon ClassCastException...  This maybe could improve!
 *******************************************************************************/
public enum TableCustomizers
{
   POST_QUERY_RECORD(new TableCustomizer("postQueryRecord", AbstractPostQueryCustomizer.class)),
   POST_INSERT_RECORD(new TableCustomizer("postInsertRecord", AbstractPostInsertCustomizer.class));


   private final TableCustomizer tableCustomizer;



   /*******************************************************************************
    **
    *******************************************************************************/
   TableCustomizers(TableCustomizer tableCustomizer)
   {
      this.tableCustomizer = tableCustomizer;
   }



   /*******************************************************************************
    ** Get the TableCustomer for a given role (e.g., the role used in meta-data, not
    ** the enum-constant name).
    *******************************************************************************/
   public static TableCustomizers forRole(String name)
   {
      for(TableCustomizers value : values())
      {
         if(value.tableCustomizer.getRole().equals(name))
         {
            return (value);
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Getter for tableCustomizer
    **
    *******************************************************************************/
   public TableCustomizer getTableCustomizer()
   {
      return tableCustomizer;
   }



   /*******************************************************************************
    ** get the role from the tableCustomizer
    **
    *******************************************************************************/
   public String getRole()
   {
      return (tableCustomizer.getRole());
   }

}
