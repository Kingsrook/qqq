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


/*******************************************************************************
 ** Meta-Data to define the Output View for a QQQ Function
 **
 *******************************************************************************/
public class QOutputView
{
   private String messageField;
   private QRecordListView recordListView;



   /*******************************************************************************
    ** Getter for message
    **
    *******************************************************************************/
   public String getMessageField()
   {
      return messageField;
   }



   /*******************************************************************************
    ** Setter for message
    **
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.messageField = message;
   }



   /*******************************************************************************
    ** Setter for message
    **
    *******************************************************************************/
   public QOutputView withMessageField(String messageField)
   {
      this.messageField = messageField;
      return(this);
   }



   /*******************************************************************************
    ** Getter for recordListView
    **
    *******************************************************************************/
   public QRecordListView getRecordListView()
   {
      return recordListView;
   }



   /*******************************************************************************
    ** Setter for recordListView
    **
    *******************************************************************************/
   public void setRecordListView(QRecordListView recordListView)
   {
      this.recordListView = recordListView;
   }



   /*******************************************************************************
    ** Setter for recordListView
    **
    *******************************************************************************/
   public QOutputView withRecordListView(QRecordListView recordListView)
   {
      this.recordListView = recordListView;
      return(this);
   }


}
