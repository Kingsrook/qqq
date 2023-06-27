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

package com.kingsrook.qqq.backend.core.model.scripts;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 **
 *******************************************************************************/
public class ScriptType extends QRecordEntity
{
   public static final String TABLE_NAME = "scriptType";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField()
   private String name;

   @QField()
   private String helpText;

   @QField()
   private String sampleCode;

   @QField(possibleValueSourceName = ScriptTypeFileMode.NAME)
   private Integer fileMode;

   @QField()
   private String testScriptInterfaceName;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ScriptType()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ScriptType(QRecord qRecord)
   {
      populateFromQRecord(qRecord);
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public Integer getId()
   {
      return id;
   }



   /*******************************************************************************
    ** Setter for id
    **
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    **
    *******************************************************************************/
   public ScriptType withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate
    **
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return createDate;
   }



   /*******************************************************************************
    ** Setter for createDate
    **
    *******************************************************************************/
   public void setCreateDate(Instant createDate)
   {
      this.createDate = createDate;
   }



   /*******************************************************************************
    ** Fluent setter for createDate
    **
    *******************************************************************************/
   public ScriptType withCreateDate(Instant createDate)
   {
      this.createDate = createDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate
    **
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return modifyDate;
   }



   /*******************************************************************************
    ** Setter for modifyDate
    **
    *******************************************************************************/
   public void setModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
   }



   /*******************************************************************************
    ** Fluent setter for modifyDate
    **
    *******************************************************************************/
   public ScriptType withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
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
   public ScriptType withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for helpText
    **
    *******************************************************************************/
   public String getHelpText()
   {
      return helpText;
   }



   /*******************************************************************************
    ** Setter for helpText
    **
    *******************************************************************************/
   public void setHelpText(String helpText)
   {
      this.helpText = helpText;
   }



   /*******************************************************************************
    ** Fluent setter for helpText
    **
    *******************************************************************************/
   public ScriptType withHelpText(String helpText)
   {
      this.helpText = helpText;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sampleCode
    **
    *******************************************************************************/
   public String getSampleCode()
   {
      return sampleCode;
   }



   /*******************************************************************************
    ** Setter for sampleCode
    **
    *******************************************************************************/
   public void setSampleCode(String sampleCode)
   {
      this.sampleCode = sampleCode;
   }



   /*******************************************************************************
    ** Fluent setter for sampleCode
    **
    *******************************************************************************/
   public ScriptType withSampleCode(String sampleCode)
   {
      this.sampleCode = sampleCode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fileMode
    *******************************************************************************/
   public Integer getFileMode()
   {
      return (this.fileMode);
   }



   /*******************************************************************************
    ** Setter for fileMode
    *******************************************************************************/
   public void setFileMode(Integer fileMode)
   {
      this.fileMode = fileMode;
   }



   /*******************************************************************************
    ** Fluent setter for fileMode
    *******************************************************************************/
   public ScriptType withFileMode(Integer fileMode)
   {
      this.fileMode = fileMode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for testScriptInterfaceName
    *******************************************************************************/
   public String getTestScriptInterfaceName()
   {
      return (this.testScriptInterfaceName);
   }



   /*******************************************************************************
    ** Setter for testScriptInterfaceName
    *******************************************************************************/
   public void setTestScriptInterfaceName(String testScriptInterfaceName)
   {
      this.testScriptInterfaceName = testScriptInterfaceName;
   }



   /*******************************************************************************
    ** Fluent setter for testScriptInterfaceName
    *******************************************************************************/
   public ScriptType withTestScriptInterfaceName(String testScriptInterfaceName)
   {
      this.testScriptInterfaceName = testScriptInterfaceName;
      return (this);
   }

}
