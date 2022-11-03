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
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;


/*******************************************************************************
 ** Pointer to code to be ran by the qqq framework, e.g., for custom behavior -
 ** maybe process steps, maybe customization to a table, etc.
 *******************************************************************************/
public class QCodeReference implements Serializable
{
   private String     name;
   private QCodeType  codeType;
   private QCodeUsage codeUsage;

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
   public QCodeReference(String name, QCodeType codeType, QCodeUsage codeUsage)
   {
      this.name = name;
      this.codeType = codeType;
      this.codeUsage = codeUsage;
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

      if(BackendStep.class.isAssignableFrom(javaClass))
      {
         this.codeUsage = QCodeUsage.BACKEND_STEP;
      }
      else if(QCustomPossibleValueProvider.class.isAssignableFrom(javaClass))
      {
         this.codeUsage = QCodeUsage.POSSIBLE_VALUE_PROVIDER;
      }
      else if(RecordAutomationHandler.class.isAssignableFrom(javaClass))
      {
         this.codeUsage = QCodeUsage.RECORD_AUTOMATION_HANDLER;
      }
      else
      {
         throw (new IllegalStateException("Unable to infer code usage type for class: " + javaClass.getName()));
      }
   }



   /*******************************************************************************
    ** Constructor that just takes a java class and code usage.
    *******************************************************************************/
   public QCodeReference(Class<?> javaClass, QCodeUsage codeUsage)
   {
      this.name = javaClass.getName();
      this.codeType = QCodeType.JAVA;
      this.codeUsage = codeUsage;
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
