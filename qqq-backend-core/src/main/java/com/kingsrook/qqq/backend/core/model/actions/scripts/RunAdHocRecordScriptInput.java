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
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.QCodeExecutionLoggerInterface;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.AdHocScriptCodeReference;


/*******************************************************************************
 **
 *******************************************************************************/
public class RunAdHocRecordScriptInput extends AbstractTableActionInput
{
   private AdHocScriptCodeReference      codeReference;
   private Map<String, Serializable>     inputValues;
   private List<Serializable>            recordPrimaryKeyList; // can either supply recordList, or recordPrimaryKeyList
   private List<QRecord>                 recordList;
   private String                        tableName;
   private QCodeExecutionLoggerInterface logger;

   private Serializable outputObject;

   private Serializable scriptUtils;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunAdHocRecordScriptInput()
   {
   }



   /*******************************************************************************
    ** Getter for inputValues
    **
    *******************************************************************************/
   public Map<String, Serializable> getInputValues()
   {
      return inputValues;
   }



   /*******************************************************************************
    ** Setter for inputValues
    **
    *******************************************************************************/
   public void setInputValues(Map<String, Serializable> inputValues)
   {
      this.inputValues = inputValues;
   }



   /*******************************************************************************
    ** Fluent setter for inputValues
    **
    *******************************************************************************/
   public RunAdHocRecordScriptInput withInputValues(Map<String, Serializable> inputValues)
   {
      this.inputValues = inputValues;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outputObject
    **
    *******************************************************************************/
   public Serializable getOutputObject()
   {
      return outputObject;
   }



   /*******************************************************************************
    ** Setter for outputObject
    **
    *******************************************************************************/
   public void setOutputObject(Serializable outputObject)
   {
      this.outputObject = outputObject;
   }



   /*******************************************************************************
    ** Fluent setter for outputObject
    **
    *******************************************************************************/
   public RunAdHocRecordScriptInput withOutputObject(Serializable outputObject)
   {
      this.outputObject = outputObject;
      return (this);
   }



   /*******************************************************************************
    ** Getter for logger
    *******************************************************************************/
   public QCodeExecutionLoggerInterface getLogger()
   {
      return (this.logger);
   }



   /*******************************************************************************
    ** Setter for logger
    *******************************************************************************/
   public void setLogger(QCodeExecutionLoggerInterface logger)
   {
      this.logger = logger;
   }



   /*******************************************************************************
    ** Fluent setter for logger
    *******************************************************************************/
   public RunAdHocRecordScriptInput withLogger(QCodeExecutionLoggerInterface logger)
   {
      this.logger = logger;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptUtils
    **
    *******************************************************************************/
   public Serializable getScriptUtils()
   {
      return scriptUtils;
   }



   /*******************************************************************************
    ** Setter for scriptUtils
    **
    *******************************************************************************/
   public void setScriptUtils(Serializable scriptUtils)
   {
      this.scriptUtils = scriptUtils;
   }



   /*******************************************************************************
    ** Getter for codeReference
    *******************************************************************************/
   public AdHocScriptCodeReference getCodeReference()
   {
      return (this.codeReference);
   }



   /*******************************************************************************
    ** Setter for codeReference
    *******************************************************************************/
   public void setCodeReference(AdHocScriptCodeReference codeReference)
   {
      this.codeReference = codeReference;
   }



   /*******************************************************************************
    ** Fluent setter for codeReference
    *******************************************************************************/
   public RunAdHocRecordScriptInput withCodeReference(AdHocScriptCodeReference codeReference)
   {
      this.codeReference = codeReference;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public RunAdHocRecordScriptInput withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordList
    *******************************************************************************/
   public List<QRecord> getRecordList()
   {
      return (this.recordList);
   }



   /*******************************************************************************
    ** Setter for recordList
    *******************************************************************************/
   public void setRecordList(List<QRecord> recordList)
   {
      this.recordList = recordList;
   }



   /*******************************************************************************
    ** Fluent setter for recordList
    *******************************************************************************/
   public RunAdHocRecordScriptInput withRecordList(List<QRecord> recordList)
   {
      this.recordList = recordList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordPrimaryKeyList
    *******************************************************************************/
   public List<Serializable> getRecordPrimaryKeyList()
   {
      return (this.recordPrimaryKeyList);
   }



   /*******************************************************************************
    ** Setter for recordPrimaryKeyList
    *******************************************************************************/
   public void setRecordPrimaryKeyList(List<Serializable> recordPrimaryKeyList)
   {
      this.recordPrimaryKeyList = recordPrimaryKeyList;
   }



   /*******************************************************************************
    ** Fluent setter for recordPrimaryKeyList
    *******************************************************************************/
   public RunAdHocRecordScriptInput withRecordPrimaryKeyList(List<Serializable> recordPrimaryKeyList)
   {
      this.recordPrimaryKeyList = recordPrimaryKeyList;
      return (this);
   }

}
