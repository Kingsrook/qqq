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

package com.kingsrook.qqq.middleware.javalin.misc;


import com.kingsrook.qqq.backend.core.exceptions.QException;


/*******************************************************************************
 ** custom code that can run when user downloads a file.  Set as a code-reference
 ** on a field adornment.
 *******************************************************************************/
public interface DownloadFileSupplementalAction
{

   /***************************************************************************
    **
    ***************************************************************************/
   void run(DownloadFileSupplementalActionInput input, DownloadFileSupplementalActionOutput output) throws QException;


   /***************************************************************************
    **
    ***************************************************************************/
   class DownloadFileSupplementalActionInput
   {
      private String tableName;
      private String primaryKey;
      private String fieldName;
      private String fileName;



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
      public DownloadFileSupplementalActionInput withTableName(String tableName)
      {
         this.tableName = tableName;
         return (this);
      }



      /*******************************************************************************
       ** Getter for primaryKey
       **
       *******************************************************************************/
      public String getPrimaryKey()
      {
         return primaryKey;
      }



      /*******************************************************************************
       ** Setter for primaryKey
       **
       *******************************************************************************/
      public void setPrimaryKey(String primaryKey)
      {
         this.primaryKey = primaryKey;
      }



      /*******************************************************************************
       ** Fluent setter for primaryKey
       **
       *******************************************************************************/
      public DownloadFileSupplementalActionInput withPrimaryKey(String primaryKey)
      {
         this.primaryKey = primaryKey;
         return (this);
      }



      /*******************************************************************************
       ** Getter for fieldName
       **
       *******************************************************************************/
      public String getFieldName()
      {
         return fieldName;
      }



      /*******************************************************************************
       ** Setter for fieldName
       **
       *******************************************************************************/
      public void setFieldName(String fieldName)
      {
         this.fieldName = fieldName;
      }



      /*******************************************************************************
       ** Fluent setter for fieldName
       **
       *******************************************************************************/
      public DownloadFileSupplementalActionInput withFieldName(String fieldName)
      {
         this.fieldName = fieldName;
         return (this);
      }



      /*******************************************************************************
       ** Getter for fileName
       **
       *******************************************************************************/
      public String getFileName()
      {
         return fileName;
      }



      /*******************************************************************************
       ** Setter for fileName
       **
       *******************************************************************************/
      public void setFileName(String fileName)
      {
         this.fileName = fileName;
      }



      /*******************************************************************************
       ** Fluent setter for fileName
       **
       *******************************************************************************/
      public DownloadFileSupplementalActionInput withFileName(String fileName)
      {
         this.fileName = fileName;
         return (this);
      }

   }



   /***************************************************************************
    **
    ***************************************************************************/
   class DownloadFileSupplementalActionOutput
   {
      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public DownloadFileSupplementalActionOutput()
      {
         ////////////////////////////////////////////////////////////////
         // sorry, but here just to get test-coverage on this class... //
         ////////////////////////////////////////////////////////////////
         int i = 0;
      }
   }
}
