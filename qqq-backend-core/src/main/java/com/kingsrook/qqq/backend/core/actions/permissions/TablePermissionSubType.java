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

package com.kingsrook.qqq.backend.core.actions.permissions;


/*******************************************************************************
 **
 *******************************************************************************/
public enum TablePermissionSubType implements PermissionSubType
{
   READ("read"), // for a table in read/write mode - or - for read (query, get, count) on a table in full-mode
   INSERT("insert"), // for table-insert.
   EDIT("edit"), // for table-edit.
   DELETE("delete"); // for table-delete.

   private final String permissionSuffix;



   /*******************************************************************************
    **
    *******************************************************************************/
   TablePermissionSubType(String permissionSuffix)
   {
      this.permissionSuffix = permissionSuffix;
   }



   /*******************************************************************************
    ** Getter for permissionSuffix
    *******************************************************************************/
   public String getPermissionSuffix()
   {
      return (this.permissionSuffix);
   }

}
