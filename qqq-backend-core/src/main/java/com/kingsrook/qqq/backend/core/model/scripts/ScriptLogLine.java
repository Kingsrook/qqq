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
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 **
 *******************************************************************************/
public class ScriptLogLine extends QRecordEntity
{
   public static final String TABLE_NAME = "scriptLogLine";

   @QField()
   private Integer id;

   @QField()
   private Instant createDate;

   @QField()
   private Instant modifyDate;

   @QField()
   private Integer scriptLogId;

   @QField()
   private Instant timestamp;

   @QField()
   private String text;



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
   public ScriptLogLine withId(Integer id)
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
   public ScriptLogLine withCreateDate(Instant createDate)
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
   public ScriptLogLine withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptLogId
    **
    *******************************************************************************/
   public Integer getScriptLogId()
   {
      return scriptLogId;
   }



   /*******************************************************************************
    ** Setter for scriptLogId
    **
    *******************************************************************************/
   public void setScriptLogId(Integer scriptLogId)
   {
      this.scriptLogId = scriptLogId;
   }



   /*******************************************************************************
    ** Fluent setter for scriptLogId
    **
    *******************************************************************************/
   public ScriptLogLine withScriptLogId(Integer scriptLogId)
   {
      this.scriptLogId = scriptLogId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for timestamp
    **
    *******************************************************************************/
   public Instant getTimestamp()
   {
      return timestamp;
   }



   /*******************************************************************************
    ** Setter for timestamp
    **
    *******************************************************************************/
   public void setTimestamp(Instant timestamp)
   {
      this.timestamp = timestamp;
   }



   /*******************************************************************************
    ** Fluent setter for timestamp
    **
    *******************************************************************************/
   public ScriptLogLine withTimestamp(Instant timestamp)
   {
      this.timestamp = timestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for text
    **
    *******************************************************************************/
   public String getText()
   {
      return text;
   }



   /*******************************************************************************
    ** Setter for text
    **
    *******************************************************************************/
   public void setText(String text)
   {
      this.text = text;
   }



   /*******************************************************************************
    ** Fluent setter for text
    **
    *******************************************************************************/
   public ScriptLogLine withText(String text)
   {
      this.text = text;
      return (this);
   }

}
