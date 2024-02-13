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

package com.kingsrook.qqq.backend.core.model.actions.messaging;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;


/*******************************************************************************
 **
 *******************************************************************************/
public class SendMessageInput extends AbstractActionInput
{
   private String           messagingProviderName;
   private Party            to;
   private Party            from;
   private String           subject;
   private List<Content>    contentList;
   private List<Attachment> attachmentList;



   /*******************************************************************************
    ** Getter for to
    *******************************************************************************/
   public Party getTo()
   {
      return (this.to);
   }



   /*******************************************************************************
    ** Setter for to
    *******************************************************************************/
   public void setTo(Party to)
   {
      this.to = to;
   }



   /*******************************************************************************
    ** Fluent setter for to
    *******************************************************************************/
   public SendMessageInput withTo(Party to)
   {
      this.to = to;
      return (this);
   }



   /*******************************************************************************
    ** Getter for from
    *******************************************************************************/
   public Party getFrom()
   {
      return (this.from);
   }



   /*******************************************************************************
    ** Setter for from
    *******************************************************************************/
   public void setFrom(Party from)
   {
      this.from = from;
   }



   /*******************************************************************************
    ** Fluent setter for from
    *******************************************************************************/
   public SendMessageInput withFrom(Party from)
   {
      this.from = from;
      return (this);
   }



   /*******************************************************************************
    ** Getter for subject
    *******************************************************************************/
   public String getSubject()
   {
      return (this.subject);
   }



   /*******************************************************************************
    ** Setter for subject
    *******************************************************************************/
   public void setSubject(String subject)
   {
      this.subject = subject;
   }



   /*******************************************************************************
    ** Fluent setter for subject
    *******************************************************************************/
   public SendMessageInput withSubject(String subject)
   {
      this.subject = subject;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contentList
    *******************************************************************************/
   public List<Content> getContentList()
   {
      return (this.contentList);
   }



   /*******************************************************************************
    ** Setter for contentList
    *******************************************************************************/
   public void setContentList(List<Content> contentList)
   {
      this.contentList = contentList;
   }



   /*******************************************************************************
    ** Fluent setter for contentList
    *******************************************************************************/
   public SendMessageInput withContentList(List<Content> contentList)
   {
      this.contentList = contentList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for attachmentList
    *******************************************************************************/
   public List<Attachment> getAttachmentList()
   {
      return (this.attachmentList);
   }



   /*******************************************************************************
    ** Setter for attachmentList
    *******************************************************************************/
   public void setAttachmentList(List<Attachment> attachmentList)
   {
      this.attachmentList = attachmentList;
   }



   /*******************************************************************************
    ** Fluent setter for attachmentList
    *******************************************************************************/
   public SendMessageInput withAttachmentList(List<Attachment> attachmentList)
   {
      this.attachmentList = attachmentList;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public SendMessageInput withContent(Content content)
   {
      addContent(content);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addContent(Content content)
   {
      if(this.contentList == null)
      {
         this.contentList = new ArrayList<>();
      }
      this.contentList.add(content);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public SendMessageInput withAttachment(Attachment attachment)
   {
      addAttachment(attachment);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addAttachment(Attachment attachment)
   {
      if(this.attachmentList == null)
      {
         this.attachmentList = new ArrayList<>();
      }
      this.attachmentList.add(attachment);
   }



   /*******************************************************************************
    ** Getter for messagingProviderName
    *******************************************************************************/
   public String getMessagingProviderName()
   {
      return (this.messagingProviderName);
   }



   /*******************************************************************************
    ** Setter for messagingProviderName
    *******************************************************************************/
   public void setMessagingProviderName(String messagingProviderName)
   {
      this.messagingProviderName = messagingProviderName;
   }



   /*******************************************************************************
    ** Fluent setter for messagingProviderName
    *******************************************************************************/
   public SendMessageInput withMessagingProviderName(String messagingProviderName)
   {
      this.messagingProviderName = messagingProviderName;
      return (this);
   }

}
