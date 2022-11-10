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
public class ScriptRevision extends QRecordEntity
{
   public static final String TABLE_NAME = "scriptRevision";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField(possibleValueSourceName = "script")
   private Integer scriptId;

   @QField()
   private String contents;

   @QField()
   private Integer sequenceNo;

   @QField()
   private String commitMessage;

   @QField()
   private String author;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ScriptRevision()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ScriptRevision(QRecord qRecord) throws QException
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
   public ScriptRevision withId(Integer id)
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
   public ScriptRevision withCreateDate(Instant createDate)
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
   public ScriptRevision withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptId
    **
    *******************************************************************************/
   public Integer getScriptId()
   {
      return scriptId;
   }



   /*******************************************************************************
    ** Setter for scriptId
    **
    *******************************************************************************/
   public void setScriptId(Integer scriptId)
   {
      this.scriptId = scriptId;
   }



   /*******************************************************************************
    ** Fluent setter for scriptId
    **
    *******************************************************************************/
   public ScriptRevision withScriptId(Integer scriptId)
   {
      this.scriptId = scriptId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contents
    **
    *******************************************************************************/
   public String getContents()
   {
      return contents;
   }



   /*******************************************************************************
    ** Setter for contents
    **
    *******************************************************************************/
   public void setContents(String contents)
   {
      this.contents = contents;
   }



   /*******************************************************************************
    ** Fluent setter for contents
    **
    *******************************************************************************/
   public ScriptRevision withContents(String contents)
   {
      this.contents = contents;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sequenceNo
    **
    *******************************************************************************/
   public Integer getSequenceNo()
   {
      return sequenceNo;
   }



   /*******************************************************************************
    ** Setter for sequenceNo
    **
    *******************************************************************************/
   public void setSequenceNo(Integer sequenceNo)
   {
      this.sequenceNo = sequenceNo;
   }



   /*******************************************************************************
    ** Fluent setter for sequenceNo
    **
    *******************************************************************************/
   public ScriptRevision withSequenceNo(Integer sequenceNo)
   {
      this.sequenceNo = sequenceNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for commitMessage
    **
    *******************************************************************************/
   public String getCommitMessage()
   {
      return commitMessage;
   }



   /*******************************************************************************
    ** Setter for commitMessage
    **
    *******************************************************************************/
   public void setCommitMessage(String commitMessage)
   {
      this.commitMessage = commitMessage;
   }



   /*******************************************************************************
    ** Fluent setter for commitMessage
    **
    *******************************************************************************/
   public ScriptRevision withCommitMessage(String commitMessage)
   {
      this.commitMessage = commitMessage;
      return (this);
   }



   /*******************************************************************************
    ** Getter for author
    **
    *******************************************************************************/
   public String getAuthor()
   {
      return author;
   }



   /*******************************************************************************
    ** Setter for author
    **
    *******************************************************************************/
   public void setAuthor(String author)
   {
      this.author = author;
   }



   /*******************************************************************************
    ** Fluent setter for author
    **
    *******************************************************************************/
   public ScriptRevision withAuthor(String author)
   {
      this.author = author;
      return (this);
   }

}
