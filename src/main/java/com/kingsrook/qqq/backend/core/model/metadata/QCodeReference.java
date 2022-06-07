/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.model.metadata;


/*******************************************************************************
 **
 *******************************************************************************/
public class QCodeReference
{
   private String name;
   private QCodeType codeType;
   private QCodeUsage codeUsage;



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public QCodeReference withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for codeType
    **
    *******************************************************************************/
   public QCodeType getCodeType()
   {
      return codeType;
   }



   /*******************************************************************************
    ** Setter for codeType
    **
    *******************************************************************************/
   public void setCodeType(QCodeType codeType)
   {
      this.codeType = codeType;
   }



   /*******************************************************************************
    ** Setter for codeType
    **
    *******************************************************************************/
   public QCodeReference withCodeType(QCodeType codeType)
   {
      this.codeType = codeType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for codeUsage
    **
    *******************************************************************************/
   public QCodeUsage getCodeUsage()
   {
      return codeUsage;
   }



   /*******************************************************************************
    ** Setter for codeUsage
    **
    *******************************************************************************/
   public void setCodeUsage(QCodeUsage codeUsage)
   {
      this.codeUsage = codeUsage;
   }



   /*******************************************************************************
    ** Setter for codeUsage
    **
    *******************************************************************************/
   public QCodeReference withCodeUsage(QCodeUsage codeUsage)
   {
      this.codeUsage = codeUsage;
      return (this);
   }

}
