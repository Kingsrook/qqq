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

package com.kingsrook.qqq.backend.javalin;


import java.util.function.Function;


/*******************************************************************************
 ** MetaData specific to a QQQ Javalin server.
 *******************************************************************************/
public class QJavalinMetaData
{
   private String uploadedFileArchiveTableName;

   private boolean loggerDisabled = false;

   private Function<QJavalinAccessLogger.LogEntry, Boolean> logFilter;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QJavalinMetaData()
   {
      loggerDisabled = System.getProperty(QJavalinAccessLogger.DISABLED_PROPERTY, "false").equals("true");
   }



   /*******************************************************************************
    ** Getter for uploadedFileArchiveTableName
    **
    *******************************************************************************/
   public String getUploadedFileArchiveTableName()
   {
      return uploadedFileArchiveTableName;
   }



   /*******************************************************************************
    ** Setter for uploadedFileArchiveTableName
    **
    *******************************************************************************/
   public void setUploadedFileArchiveTableName(String uploadedFileArchiveTableName)
   {
      this.uploadedFileArchiveTableName = uploadedFileArchiveTableName;
   }



   /*******************************************************************************
    ** Fluent setter for uploadedFileArchiveTableName
    **
    *******************************************************************************/
   public QJavalinMetaData withUploadedFileArchiveTableName(String uploadedFileArchiveTableName)
   {
      this.uploadedFileArchiveTableName = uploadedFileArchiveTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for loggerDisabled
    *******************************************************************************/
   public boolean getLoggerDisabled()
   {
      return (this.loggerDisabled);
   }



   /*******************************************************************************
    ** Setter for loggerDisabled
    *******************************************************************************/
   public void setLoggerDisabled(boolean loggerDisabled)
   {
      this.loggerDisabled = loggerDisabled;
   }



   /*******************************************************************************
    ** Fluent setter for loggerDisabled
    *******************************************************************************/
   public QJavalinMetaData withLoggerDisabled(boolean loggerDisabled)
   {
      this.loggerDisabled = loggerDisabled;
      return (this);
   }



   /*******************************************************************************
    ** Getter for logFilter
    *******************************************************************************/
   public Function<QJavalinAccessLogger.LogEntry, Boolean> getLogFilter()
   {
      return (this.logFilter);
   }



   /*******************************************************************************
    ** Setter for logFilter
    *******************************************************************************/
   public void setLogFilter(Function<QJavalinAccessLogger.LogEntry, Boolean> logFilter)
   {
      this.logFilter = logFilter;
   }



   /*******************************************************************************
    ** Fluent setter for logFilter
    *******************************************************************************/
   public QJavalinMetaData withLogFilter(Function<QJavalinAccessLogger.LogEntry, Boolean> logFilter)
   {
      this.logFilter = logFilter;
      return (this);
   }

}
