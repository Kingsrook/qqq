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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.filesystem.importer;


import java.io.Serializable;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.AbstractProcessMetaDataBuilder;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Process MetaData Builder for FilesystemImporter process.
 ** Meant to be used with (and actually is a parameter to the constructor of)
 ** {@link FilesystemImporterMetaDataTemplate}
 *******************************************************************************/
public class FilesystemImporterProcessMetaDataBuilder extends AbstractProcessMetaDataBuilder
{

   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder()
   {
      super(new QProcessMetaData()
         .addStep(new QBackendStepMetaData()
            .withName("sync")
            .withCode(new QCodeReference(FilesystemImporterStep.class))
            .withInputData(new QFunctionInputMetaData()
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_SOURCE_TABLE, QFieldType.STRING))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_FILE_FORMAT, QFieldType.STRING))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_IMPORT_FILE_TABLE, QFieldType.STRING))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_IMPORT_RECORD_TABLE, QFieldType.STRING))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_REMOVE_FILE_AFTER_IMPORT, QFieldType.BOOLEAN).withDefaultValue(true))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_UPDATE_FILE_IF_NAME_EXISTS, QFieldType.BOOLEAN).withDefaultValue(false))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_ARCHIVE_FILE_ENABLED, QFieldType.BOOLEAN).withDefaultValue(false))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_ARCHIVE_TABLE_NAME, QFieldType.STRING))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_ARCHIVE_PATH, QFieldType.STRING))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_IMPORT_SECURITY_FIELD_NAME, QFieldType.STRING))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_IMPORT_SECURITY_FIELD_VALUE, QFieldType.STRING))

               //////////////////////////////////////////////////////////////////////////////////////
               // define a QCodeReference - expected to be of type Function<QRecord, Serializable> //
               // make sure the QInstanceValidator knows that the QCodeReference should be a       //
               // Function (not a BackendStep, which is the default for process fields)            //
               //////////////////////////////////////////////////////////////////////////////////////
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_IMPORT_SECURITY_VALUE_SUPPLIER, QFieldType.STRING))
               .withField(new QFieldMetaData(FilesystemImporterStep.FIELD_IMPORT_SECURITY_VALUE_SUPPLIER + "_expectedType", QFieldType.STRING)
                  .withDefaultValue(Function.class.getName()))
            )));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withSourceTableName(String sourceTableName)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_SOURCE_TABLE, sourceTableName);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withFileFormat(String fileFormat)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_FILE_FORMAT, fileFormat);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withImportFileTable(String importFileTable)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_IMPORT_FILE_TABLE, importFileTable);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withImportRecordTable(String importRecordTable)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_IMPORT_RECORD_TABLE, importRecordTable);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withRemoveFileAfterImport(boolean removeFileAfterImport)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_REMOVE_FILE_AFTER_IMPORT, removeFileAfterImport);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withUpdateFileIfNameExists(boolean updateFileIfNameExists)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_UPDATE_FILE_IF_NAME_EXISTS, updateFileIfNameExists);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withArchiveFileEnabled(boolean archiveFileEnabled)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_ARCHIVE_FILE_ENABLED, archiveFileEnabled);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withArchiveTableName(String archiveTableName)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_ARCHIVE_TABLE_NAME, archiveTableName);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withArchivePath(String archivePath)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_ARCHIVE_PATH, archivePath);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withImportSecurityFieldName(String securityFieldName)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_IMPORT_SECURITY_FIELD_NAME, securityFieldName);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withImportSecurityFieldValue(Serializable securityFieldValue)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_IMPORT_SECURITY_FIELD_VALUE, securityFieldValue);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilesystemImporterProcessMetaDataBuilder withImportSecurityValueSupplierFunction(Class<? extends Function<QRecord, Serializable>> supplierFunction)
   {
      setInputFieldDefaultValue(FilesystemImporterStep.FIELD_IMPORT_SECURITY_VALUE_SUPPLIER, new QCodeReference(supplierFunction));
      return (this);
   }

}
