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

package com.kingsrook.qqq.backend.core.model.actions.scripts;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestScriptInput extends AbstractTableActionInput
{
   private Serializable        recordPrimaryKey;
   private String              code;
   private Serializable        scriptTypeId;
   private Map<String, String> inputValues;



   /*******************************************************************************
    **
    *******************************************************************************/
   public TestScriptInput(QInstance qInstance)
   {
      super(qInstance);
   }



   /*******************************************************************************
    ** Getter for recordPrimaryKey
    **
    *******************************************************************************/
   public Serializable getRecordPrimaryKey()
   {
      return recordPrimaryKey;
   }



   /*******************************************************************************
    ** Setter for recordPrimaryKey
    **
    *******************************************************************************/
   public void setRecordPrimaryKey(Serializable recordPrimaryKey)
   {
      this.recordPrimaryKey = recordPrimaryKey;
   }



   /*******************************************************************************
    ** Fluent setter for recordPrimaryKey
    **
    *******************************************************************************/
   public TestScriptInput withRecordPrimaryKey(Serializable recordPrimaryKey)
   {
      this.recordPrimaryKey = recordPrimaryKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputValues
    **
    *******************************************************************************/
   public Map<String, String> getInputValues()
   {
      return inputValues;
   }



   /*******************************************************************************
    ** Setter for inputValues
    **
    *******************************************************************************/
   public void setInputValues(Map<String, String> inputValues)
   {
      this.inputValues = inputValues;
   }



   /*******************************************************************************
    ** Fluent setter for inputValues
    **
    *******************************************************************************/
   public TestScriptInput withInputValues(Map<String, String> inputValues)
   {
      this.inputValues = inputValues;
      return (this);
   }



   /*******************************************************************************
    ** Getter for code
    **
    *******************************************************************************/
   public String getCode()
   {
      return code;
   }



   /*******************************************************************************
    ** Setter for code
    **
    *******************************************************************************/
   public void setCode(String code)
   {
      this.code = code;
   }



   /*******************************************************************************
    ** Fluent setter for code
    **
    *******************************************************************************/
   public TestScriptInput withCode(String code)
   {
      this.code = code;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptTypeId
    **
    *******************************************************************************/
   public Serializable getScriptTypeId()
   {
      return scriptTypeId;
   }



   /*******************************************************************************
    ** Setter for scriptTypeId
    **
    *******************************************************************************/
   public void setScriptTypeId(Serializable scriptTypeId)
   {
      this.scriptTypeId = scriptTypeId;
   }



   /*******************************************************************************
    ** Fluent setter for scriptTypeId
    **
    *******************************************************************************/
   public TestScriptInput withScriptTypeId(Serializable scriptTypeId)
   {
      this.scriptTypeId = scriptTypeId;
      return (this);
   }

}
