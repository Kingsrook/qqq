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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 **
 *******************************************************************************/
public class Script extends QRecordEntity
{
   public static final String TABLE_NAME = "script";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField()
   private String name;

   @QField(possibleValueSourceName = "scriptType")
   private Integer scriptTypeId;

   @QField(possibleValueSourceName = "scriptRevision")
   private Integer currentScriptRevisionId;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public Script()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public Script(QRecord qRecord) throws QException
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
   public Script withId(Integer id)
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
   public Script withCreateDate(Instant createDate)
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
   public Script withModifyDate(Instant modifyDate)
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
   public Script withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptTypeId
    **
    *******************************************************************************/
   public Integer getScriptTypeId()
   {
      return scriptTypeId;
   }



   /*******************************************************************************
    ** Setter for scriptTypeId
    **
    *******************************************************************************/
   public void setScriptTypeId(Integer scriptTypeId)
   {
      this.scriptTypeId = scriptTypeId;
   }



   /*******************************************************************************
    ** Fluent setter for scriptTypeId
    **
    *******************************************************************************/
   public Script withScriptTypeId(Integer scriptTypeId)
   {
      this.scriptTypeId = scriptTypeId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for currentScriptRevisionId
    **
    *******************************************************************************/
   public Integer getCurrentScriptRevisionId()
   {
      return currentScriptRevisionId;
   }



   /*******************************************************************************
    ** Setter for currentScriptRevisionId
    **
    *******************************************************************************/
   public void setCurrentScriptRevisionId(Integer currentScriptRevisionId)
   {
      this.currentScriptRevisionId = currentScriptRevisionId;
   }



   /*******************************************************************************
    ** Fluent setter for currentScriptRevisionId
    **
    *******************************************************************************/
   public Script withCurrentScriptRevisionId(Integer currentScriptRevisionId)
   {
      this.currentScriptRevisionId = currentScriptRevisionId;
      return (this);
   }

}
