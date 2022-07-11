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

package com.kingsrook.qqq.backend.core.model.metadata.processes;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;


/*******************************************************************************
 ** Meta-Data to define a front-end step in a process in a QQQ instance (e.g.,
 ** a screen presented to a user).
 **
 *******************************************************************************/
public class QFrontendStepMetaData extends QStepMetaData
{
   private List<QFieldMetaData> formFields;
   private List<QFieldMetaData> viewFields;
   private List<QFieldMetaData> recordListFields;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFrontendStepMetaData()
   {
      setStepType("frontend");
   }



   /*******************************************************************************
    ** Getter for formFields
    **
    *******************************************************************************/
   public List<QFieldMetaData> getFormFields()
   {
      return formFields;
   }



   /*******************************************************************************
    ** Setter for formFields
    **
    *******************************************************************************/
   public void setFormFields(List<QFieldMetaData> formFields)
   {
      this.formFields = formFields;
   }



   /*******************************************************************************
    ** fluent setter to add a single form field
    **
    *******************************************************************************/
   public QFrontendStepMetaData withFormField(QFieldMetaData formField)
   {
      if(this.formFields == null)
      {
         this.formFields = new ArrayList<>();
      }
      this.formFields.add(formField);
      return (this);
   }



   /*******************************************************************************
    ** fluent setter for formFields
    **
    *******************************************************************************/
   public QFrontendStepMetaData withFormFields(List<QFieldMetaData> formFields)
   {
      this.formFields = formFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for viewFields
    **
    *******************************************************************************/
   public List<QFieldMetaData> getViewFields()
   {
      return viewFields;
   }



   /*******************************************************************************
    ** Setter for viewFields
    **
    *******************************************************************************/
   public void setViewFields(List<QFieldMetaData> viewFields)
   {
      this.viewFields = viewFields;
   }



   /*******************************************************************************
    ** fluent setter to add a single view field
    **
    *******************************************************************************/
   public QFrontendStepMetaData withViewField(QFieldMetaData viewField)
   {
      if(this.viewFields == null)
      {
         this.viewFields = new ArrayList<>();
      }
      this.viewFields.add(viewField);
      return (this);
   }



   /*******************************************************************************
    ** fluent setter for viewFields
    **
    *******************************************************************************/
   public QFrontendStepMetaData withViewFields(List<QFieldMetaData> viewFields)
   {
      this.viewFields = viewFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordListFields
    **
    *******************************************************************************/
   public List<QFieldMetaData> getRecordListFields()
   {
      return recordListFields;
   }



   /*******************************************************************************
    ** Setter for recordListFields
    **
    *******************************************************************************/
   public void setRecordListFields(List<QFieldMetaData> recordListFields)
   {
      this.recordListFields = recordListFields;
   }



   /*******************************************************************************
    ** fluent setter to add a single recordList field
    **
    *******************************************************************************/
   public QFrontendStepMetaData withRecordListField(QFieldMetaData recordListField)
   {
      if(this.recordListFields == null)
      {
         this.recordListFields = new ArrayList<>();
      }
      this.recordListFields.add(recordListField);
      return (this);
   }



   /*******************************************************************************
    ** fluent setter for recordListFields
    **
    *******************************************************************************/
   public QFrontendStepMetaData withRecordListFields(List<QFieldMetaData> recordListFields)
   {
      this.recordListFields = recordListFields;
      return (this);
   }



   /*******************************************************************************
    ** fluent setter for name
    **
    *******************************************************************************/
   @Override
   public QFrontendStepMetaData withName(String name)
   {
      setName(name);
      return (this);
   }



   /*******************************************************************************
    ** fluent setter for label
    **
    *******************************************************************************/
   @Override
   public QFrontendStepMetaData withLabel(String label)
   {
      setLabel(label);
      return (this);
   }

}
