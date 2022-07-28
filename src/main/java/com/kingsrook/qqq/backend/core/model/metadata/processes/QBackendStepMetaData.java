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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;


/*******************************************************************************
 ** Meta-Data to define a backend-step in a process in a QQQ instance.  e.g.,
 ** code that runs on a server/backend, to do something to some data.
 **
 *******************************************************************************/
public class QBackendStepMetaData extends QStepMetaData
{
   private QFunctionInputMetaData  inputMetaData;
   private QFunctionOutputMetaData outputMetaData;
   private QCodeReference          code;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendStepMetaData()
   {
      setStepType("backend");
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   @Override
   public QBackendStepMetaData withName(String name)
   {
      setName(name);
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputData
    **
    *******************************************************************************/
   public QFunctionInputMetaData getInputMetaData()
   {
      return inputMetaData;
   }



   /*******************************************************************************
    ** Setter for inputData
    **
    *******************************************************************************/
   public void setInputMetaData(QFunctionInputMetaData inputMetaData)
   {
      this.inputMetaData = inputMetaData;
   }



   /*******************************************************************************
    ** Setter for inputData
    **
    *******************************************************************************/
   public QBackendStepMetaData withInputData(QFunctionInputMetaData inputData)
   {
      this.inputMetaData = inputData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outputData
    **
    *******************************************************************************/
   public QFunctionOutputMetaData getOutputMetaData()
   {
      return outputMetaData;
   }



   /*******************************************************************************
    ** Setter for outputData
    **
    *******************************************************************************/
   public void setOutputMetaData(QFunctionOutputMetaData outputMetaData)
   {
      this.outputMetaData = outputMetaData;
   }



   /*******************************************************************************
    ** Setter for outputData
    **
    *******************************************************************************/
   public QBackendStepMetaData withOutputMetaData(QFunctionOutputMetaData outputMetaData)
   {
      this.outputMetaData = outputMetaData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for code
    **
    *******************************************************************************/
   public QCodeReference getCode()
   {
      return code;
   }



   /*******************************************************************************
    ** Setter for code
    **
    *******************************************************************************/
   public void setCode(QCodeReference code)
   {
      this.code = code;
   }



   /*******************************************************************************
    ** Setter for code
    **
    *******************************************************************************/
   public QBackendStepMetaData withCode(QCodeReference code)
   {
      this.code = code;
      return (this);
   }



   /*******************************************************************************
    ** Get a list of all of the input fields used by this function
    *******************************************************************************/
   @JsonIgnore // because this is a computed property - we don't want it in our json.
   @Override
   public List<QFieldMetaData> getInputFields()
   {
      List<QFieldMetaData> rs = new ArrayList<>();
      if(inputMetaData != null && inputMetaData.getFieldList() != null)
      {
         rs.addAll(inputMetaData.getFieldList());
      }
      return (rs);
   }



   /*******************************************************************************
    ** Get a list of all of the output fields used by this function
    *******************************************************************************/
   @JsonIgnore // because this is a computed property - we don't want it in our json.
   @Override
   public List<QFieldMetaData> getOutputFields()
   {
      List<QFieldMetaData> rs = new ArrayList<>();
      if(outputMetaData != null && outputMetaData.getFieldList() != null)
      {
         rs.addAll(outputMetaData.getFieldList());
      }
      return (rs);
   }

}
