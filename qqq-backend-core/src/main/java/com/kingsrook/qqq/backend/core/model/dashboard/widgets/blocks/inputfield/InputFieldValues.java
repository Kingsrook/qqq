/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.inputfield;


import com.kingsrook.qqq.backend.core.model.dashboard.widgets.blocks.BlockValuesInterface;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class InputFieldValues implements BlockValuesInterface
{
   private QFieldMetaData fieldMetaData;
   private Boolean        autoFocus;
   private Boolean        submitOnEnter;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public InputFieldValues()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public InputFieldValues(QFieldMetaData fieldMetaData)
   {
      setFieldMetaData(fieldMetaData);
   }



   /*******************************************************************************
    ** Getter for fieldMetaData
    *******************************************************************************/
   public QFieldMetaData getFieldMetaData()
   {
      return (this.fieldMetaData);
   }



   /*******************************************************************************
    ** Setter for fieldMetaData
    *******************************************************************************/
   public void setFieldMetaData(QFieldMetaData fieldMetaData)
   {
      this.fieldMetaData = fieldMetaData;
   }



   /*******************************************************************************
    ** Fluent setter for fieldMetaData
    *******************************************************************************/
   public InputFieldValues withFieldMetaData(QFieldMetaData fieldMetaData)
   {
      this.fieldMetaData = fieldMetaData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for autoFocus
    *******************************************************************************/
   public Boolean getAutoFocus()
   {
      return (this.autoFocus);
   }



   /*******************************************************************************
    ** Setter for autoFocus
    *******************************************************************************/
   public void setAutoFocus(Boolean autoFocus)
   {
      this.autoFocus = autoFocus;
   }



   /*******************************************************************************
    ** Fluent setter for autoFocus
    *******************************************************************************/
   public InputFieldValues withAutoFocus(Boolean autoFocus)
   {
      this.autoFocus = autoFocus;
      return (this);
   }



   /*******************************************************************************
    ** Getter for submitOnEnter
    *******************************************************************************/
   public Boolean getSubmitOnEnter()
   {
      return (this.submitOnEnter);
   }



   /*******************************************************************************
    ** Setter for submitOnEnter
    *******************************************************************************/
   public void setSubmitOnEnter(Boolean submitOnEnter)
   {
      this.submitOnEnter = submitOnEnter;
   }



   /*******************************************************************************
    ** Fluent setter for submitOnEnter
    *******************************************************************************/
   public InputFieldValues withSubmitOnEnter(Boolean submitOnEnter)
   {
      this.submitOnEnter = submitOnEnter;
      return (this);
   }

}
