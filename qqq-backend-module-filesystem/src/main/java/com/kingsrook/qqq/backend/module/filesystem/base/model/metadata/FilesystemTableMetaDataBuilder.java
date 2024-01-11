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

package com.kingsrook.qqq.backend.module.filesystem.base.model.metadata;


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.FilesystemBackendModule;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.filesystem.s3.S3BackendModule;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3TableBackendDetails;


/*******************************************************************************
 **
 *******************************************************************************/
public class FilesystemTableMetaDataBuilder
{
   private String           name;
   private QBackendMetaData backend;
   private String           basePath;
   private String           glob;



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:Indentation")
   public QTableMetaData buildStandardCardinalityOneTable()
   {
      AbstractFilesystemTableBackendDetails tableBackendDetails = switch(backend.getBackendType())
      {
         case S3BackendModule.BACKEND_TYPE -> new S3TableBackendDetails();
         case FilesystemBackendModule.BACKEND_TYPE -> new FilesystemTableBackendDetails();
         default -> throw new IllegalStateException("Unexpected value: " + backend.getBackendType());
      };

      return new QTableMetaData()
         .withName(name)
         .withIsHidden(true)
         .withBackendName(backend.getName())
         .withPrimaryKeyField("fileName")
         .withField(new QFieldMetaData("fileName", QFieldType.INTEGER))
         .withField(new QFieldMetaData("contents", QFieldType.STRING))
         .withBackendDetails(tableBackendDetails
            .withCardinality(Cardinality.ONE)
            .withFileNameFieldName("fileName")
            .withContentsFieldName("contents")
            .withBasePath(basePath)
            .withGlob(glob));
   }



   /*******************************************************************************
    ** Getter for backend
    *******************************************************************************/
   public QBackendMetaData getBackend()
   {
      return (this.backend);
   }



   /*******************************************************************************
    ** Setter for backend
    *******************************************************************************/
   public void setBackend(QBackendMetaData backend)
   {
      this.backend = backend;
   }



   /*******************************************************************************
    ** Fluent setter for backend
    *******************************************************************************/
   public FilesystemTableMetaDataBuilder withBackend(QBackendMetaData backend)
   {
      this.backend = backend;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public FilesystemTableMetaDataBuilder withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for basePath
    *******************************************************************************/
   public String getBasePath()
   {
      return (this.basePath);
   }



   /*******************************************************************************
    ** Setter for basePath
    *******************************************************************************/
   public void setBasePath(String basePath)
   {
      this.basePath = basePath;
   }



   /*******************************************************************************
    ** Fluent setter for basePath
    *******************************************************************************/
   public FilesystemTableMetaDataBuilder withBasePath(String basePath)
   {
      this.basePath = basePath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for glob
    *******************************************************************************/
   public String getGlob()
   {
      return (this.glob);
   }



   /*******************************************************************************
    ** Setter for glob
    *******************************************************************************/
   public void setGlob(String glob)
   {
      this.glob = glob;
   }



   /*******************************************************************************
    ** Fluent setter for glob
    *******************************************************************************/
   public FilesystemTableMetaDataBuilder withGlob(String glob)
   {
      this.glob = glob;
      return (this);
   }

}
