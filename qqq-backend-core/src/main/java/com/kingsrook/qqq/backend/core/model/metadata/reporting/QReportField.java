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

package com.kingsrook.qqq.backend.core.model.metadata.reporting;


import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;


/*******************************************************************************
 ** Field within a report
 *******************************************************************************/
public class QReportField
{
   private String     name;
   private String     label;
   private QFieldType type;
   private String     formula;
   private String     displayFormat;

   ///////////////////////////////////////////////////////////////////////////
   // Noew: new attributes added here probably belong in the toField method //
   ///////////////////////////////////////////////////////////////////////////

   private boolean isVirtual = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldMetaData toField()
   {
      return new QFieldMetaData()
         .withName(name)
         .withLabel(label)
         .withType(type)
         .withDisplayFormat(displayFormat);
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
    ** Fluent setter for name
    **
    *******************************************************************************/
   public QReportField withName(String name)
   {
      this.name = name;
      return (this);
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
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    **
    *******************************************************************************/
   public QReportField withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public QFieldType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(QFieldType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    **
    *******************************************************************************/
   public QReportField withType(QFieldType type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for formula
    **
    *******************************************************************************/
   public String getFormula()
   {
      return formula;
   }



   /*******************************************************************************
    ** Setter for formula
    **
    *******************************************************************************/
   public void setFormula(String formula)
   {
      this.formula = formula;
   }



   /*******************************************************************************
    ** Fluent setter for formula
    **
    *******************************************************************************/
   public QReportField withFormula(String formula)
   {
      this.formula = formula;
      return (this);
   }



   /*******************************************************************************
    ** Getter for displayFormat
    **
    *******************************************************************************/
   public String getDisplayFormat()
   {
      return displayFormat;
   }



   /*******************************************************************************
    ** Setter for displayFormat
    **
    *******************************************************************************/
   public void setDisplayFormat(String displayFormat)
   {
      this.displayFormat = displayFormat;
   }



   /*******************************************************************************
    ** Fluent setter for displayFormat
    **
    *******************************************************************************/
   public QReportField withDisplayFormat(String displayFormat)
   {
      this.displayFormat = displayFormat;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isVirtual
    **
    *******************************************************************************/
   public boolean getIsVirtual()
   {
      return isVirtual;
   }



   /*******************************************************************************
    ** Setter for isVirtual
    **
    *******************************************************************************/
   public void setIsVirtual(boolean isVirtual)
   {
      this.isVirtual = isVirtual;
   }



   /*******************************************************************************
    ** Fluent setter for isVirtual
    **
    *******************************************************************************/
   public QReportField withIsVirtual(boolean isVirtual)
   {
      this.isVirtual = isVirtual;
      return (this);
   }
}
