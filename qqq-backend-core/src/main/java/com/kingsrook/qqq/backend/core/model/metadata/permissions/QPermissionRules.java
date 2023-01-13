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

package com.kingsrook.qqq.backend.core.model.metadata.permissions;


import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 **
 *******************************************************************************/
public class QPermissionRules implements Cloneable
{
   private PermissionLevel level;
   private DenyBehavior    denyBehavior;
   private String          permissionBaseName;

   private QCodeReference customPermissionChecker;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QPermissionRules defaultInstance()
   {
      return new QPermissionRules()
         .withLevel(PermissionLevel.NOT_PROTECTED)
         .withDenyBehavior(DenyBehavior.HIDDEN);
   }



   /*******************************************************************************
    ** Getter for level
    *******************************************************************************/
   public PermissionLevel getLevel()
   {
      return (this.level);
   }



   /*******************************************************************************
    ** Setter for level
    *******************************************************************************/
   public void setLevel(PermissionLevel level)
   {
      this.level = level;
   }



   /*******************************************************************************
    ** Fluent setter for level
    *******************************************************************************/
   public QPermissionRules withLevel(PermissionLevel level)
   {
      this.level = level;
      return (this);
   }



   /*******************************************************************************
    ** Getter for denyBehavior
    *******************************************************************************/
   public DenyBehavior getDenyBehavior()
   {
      return (this.denyBehavior);
   }



   /*******************************************************************************
    ** Setter for denyBehavior
    *******************************************************************************/
   public void setDenyBehavior(DenyBehavior denyBehavior)
   {
      this.denyBehavior = denyBehavior;
   }



   /*******************************************************************************
    ** Fluent setter for denyBehavior
    *******************************************************************************/
   public QPermissionRules withDenyBehavior(DenyBehavior denyBehavior)
   {
      this.denyBehavior = denyBehavior;
      return (this);
   }



   /*******************************************************************************
    ** Getter for permissionBaseName
    *******************************************************************************/
   public String getPermissionBaseName()
   {
      return (this.permissionBaseName);
   }



   /*******************************************************************************
    ** Setter for permissionBaseName
    *******************************************************************************/
   public void setPermissionBaseName(String permissionBaseName)
   {
      this.permissionBaseName = permissionBaseName;
   }



   /*******************************************************************************
    ** Fluent setter for permissionBaseName
    *******************************************************************************/
   public QPermissionRules withPermissionBaseName(String permissionBaseName)
   {
      this.permissionBaseName = permissionBaseName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QPermissionRules clone()
   {
      try
      {
         QPermissionRules clone = (QPermissionRules) super.clone();
         return clone;
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }



   /*******************************************************************************
    ** Getter for customPermissionChecker
    *******************************************************************************/
   public QCodeReference getCustomPermissionChecker()
   {
      return (this.customPermissionChecker);
   }



   /*******************************************************************************
    ** Setter for customPermissionChecker
    *******************************************************************************/
   public void setCustomPermissionChecker(QCodeReference customPermissionChecker)
   {
      this.customPermissionChecker = customPermissionChecker;
   }



   /*******************************************************************************
    ** Fluent setter for customPermissionChecker
    *******************************************************************************/
   public QPermissionRules withCustomPermissionChecker(QCodeReference customPermissionChecker)
   {
      this.customPermissionChecker = customPermissionChecker;
      return (this);
   }

}
