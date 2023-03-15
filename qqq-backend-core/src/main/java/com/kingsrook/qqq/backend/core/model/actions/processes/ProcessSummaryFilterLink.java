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

package com.kingsrook.qqq.backend.core.model.actions.processes;


import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Simple process summary result object, that lets you give a link to a filter
 ** on a table.  e.g., if your process built such a records, give a link to it.
 *******************************************************************************/
public class ProcessSummaryFilterLink implements ProcessSummaryLineInterface
{
   private Status       status;
   private String       tableName;
   private QQueryFilter filter;
   private String       linkPreText;
   private String       linkText;
   private String       linkPostText;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ProcessSummaryFilterLink()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public LogPair toLogPair()
   {
      return (logPair("ProcessSummary", logPair("status", status), logPair("tableName", tableName),
         logPair("linkPreText", linkPreText), logPair("linkText", linkText), logPair("linkPostText", linkPostText)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryFilterLink(Status status, String tableName, QQueryFilter filter)
   {
      this.status = status;
      this.tableName = tableName;
      this.filter = filter;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessSummaryFilterLink(Status status, String tableName, QQueryFilter filter, String linkText)
   {
      this.status = status;
      this.tableName = tableName;
      this.filter = filter;
      this.linkText = linkText;
   }



   /*******************************************************************************
    ** Getter for status
    **
    *******************************************************************************/
   public Status getStatus()
   {
      return status;
   }



   /*******************************************************************************
    ** Setter for status
    **
    *******************************************************************************/
   public void setStatus(Status status)
   {
      this.status = status;
   }



   /*******************************************************************************
    ** Fluent setter for status
    **
    *******************************************************************************/
   public ProcessSummaryFilterLink withStatus(Status status)
   {
      this.status = status;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    **
    *******************************************************************************/
   public ProcessSummaryFilterLink withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for linkPreText
    **
    *******************************************************************************/
   public String getLinkPreText()
   {
      return linkPreText;
   }



   /*******************************************************************************
    ** Setter for linkPreText
    **
    *******************************************************************************/
   public void setLinkPreText(String linkPreText)
   {
      this.linkPreText = linkPreText;
   }



   /*******************************************************************************
    ** Fluent setter for linkPreText
    **
    *******************************************************************************/
   public ProcessSummaryFilterLink withLinkPreText(String linkPreText)
   {
      this.linkPreText = linkPreText;
      return (this);
   }



   /*******************************************************************************
    ** Getter for linkText
    **
    *******************************************************************************/
   public String getLinkText()
   {
      return linkText;
   }



   /*******************************************************************************
    ** Setter for linkText
    **
    *******************************************************************************/
   public void setLinkText(String linkText)
   {
      this.linkText = linkText;
   }



   /*******************************************************************************
    ** Fluent setter for linkText
    **
    *******************************************************************************/
   public ProcessSummaryFilterLink withLinkText(String linkText)
   {
      this.linkText = linkText;
      return (this);
   }



   /*******************************************************************************
    ** Getter for linkPostText
    **
    *******************************************************************************/
   public String getLinkPostText()
   {
      return linkPostText;
   }



   /*******************************************************************************
    ** Setter for linkPostText
    **
    *******************************************************************************/
   public void setLinkPostText(String linkPostText)
   {
      this.linkPostText = linkPostText;
   }



   /*******************************************************************************
    ** Fluent setter for linkPostText
    **
    *******************************************************************************/
   public ProcessSummaryFilterLink withLinkPostText(String linkPostText)
   {
      this.linkPostText = linkPostText;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filter
    *******************************************************************************/
   public QQueryFilter getFilter()
   {
      return (this.filter);
   }



   /*******************************************************************************
    ** Setter for filter
    *******************************************************************************/
   public void setFilter(QQueryFilter filter)
   {
      this.filter = filter;
   }



   /*******************************************************************************
    ** Fluent setter for filter
    *******************************************************************************/
   public ProcessSummaryFilterLink withFilter(QQueryFilter filter)
   {
      this.filter = filter;
      return (this);
   }

}
