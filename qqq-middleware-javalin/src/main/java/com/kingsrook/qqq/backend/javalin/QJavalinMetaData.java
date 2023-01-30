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


/*******************************************************************************
 ** MetaData specific to a QQQ Javalin server.
 *******************************************************************************/
public class QJavalinMetaData
{
   private String uploadedFileArchiveTableName;

   private boolean logAllAccessStarts = true;
   private boolean logAllAccessEnds   = true;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QJavalinMetaData()
   {
      logAllAccessStarts = System.getProperty("qqq.javalin.logAllAccessStarts", "true").equals("true");
      logAllAccessEnds = System.getProperty("qqq.javalin.logAllAccessEnds", "true").equals("true");
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
    ** Getter for logAllAccessStarts
    *******************************************************************************/
   public boolean getLogAllAccessStarts()
   {
      return (this.logAllAccessStarts);
   }



   /*******************************************************************************
    ** Setter for logAllAccessStarts
    *******************************************************************************/
   public void setLogAllAccessStarts(boolean logAllAccessStarts)
   {
      this.logAllAccessStarts = logAllAccessStarts;
   }



   /*******************************************************************************
    ** Fluent setter for logAllAccessStarts
    *******************************************************************************/
   public QJavalinMetaData withLogAllAccessStarts(boolean logAllAccessStarts)
   {
      this.logAllAccessStarts = logAllAccessStarts;
      return (this);
   }



   /*******************************************************************************
    ** Getter for logAllAccessEnds
    *******************************************************************************/
   public boolean getLogAllAccessEnds()
   {
      return (this.logAllAccessEnds);
   }



   /*******************************************************************************
    ** Setter for logAllAccessEnds
    *******************************************************************************/
   public void setLogAllAccessEnds(boolean logAllAccessEnds)
   {
      this.logAllAccessEnds = logAllAccessEnds;
   }



   /*******************************************************************************
    ** Fluent setter for logAllAccessEnds
    *******************************************************************************/
   public QJavalinMetaData withLogAllAccessEnds(boolean logAllAccessEnds)
   {
      this.logAllAccessEnds = logAllAccessEnds;
      return (this);
   }

}
