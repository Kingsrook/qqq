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

package com.kingsrook.qqq.backend.core.model.metadata.code;


import java.io.Serializable;


/*******************************************************************************
 ** Pointer to code to be ran by the qqq framework, e.g., for custom behavior -
 ** maybe process steps, maybe customization to a table, etc.
 *******************************************************************************/
public class QCodeReference implements Serializable, Cloneable
{
   private String    name;
   private QCodeType codeType;

   private String inlineCode;



   /*******************************************************************************
    ** Default empty constructor
    *******************************************************************************/
   public QCodeReference()
   {
   }



   /*******************************************************************************
    ** Constructor that takes all args
    *******************************************************************************/
   public QCodeReference(String name, QCodeType codeType)
   {
      this.name = name;
      this.codeType = codeType;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QCodeReference clone()
   {
      try
      {
         QCodeReference clone = (QCodeReference) super.clone();
         return clone;
      }
      catch(CloneNotSupportedException e)
      {
         throw new AssertionError();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "QCodeReference{name='" + name + "'}";
   }



   /*******************************************************************************
    ** Constructor that just takes a java class, and infers the other fields.
    *******************************************************************************/
   public QCodeReference(Class<?> javaClass)
   {
      this.name = javaClass.getName();
      this.codeType = QCodeType.JAVA;
   }



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
    ** Getter for inlineCode
    **
    *******************************************************************************/
   public String getInlineCode()
   {
      return inlineCode;
   }



   /*******************************************************************************
    ** Setter for inlineCode
    **
    *******************************************************************************/
   public void setInlineCode(String inlineCode)
   {
      this.inlineCode = inlineCode;
   }



   /*******************************************************************************
    ** Fluent setter for inlineCode
    **
    *******************************************************************************/
   public QCodeReference withInlineCode(String inlineCode)
   {
      this.inlineCode = inlineCode;
      return (this);
   }
}
