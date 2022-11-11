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

package com.kingsrook.qqq.backend.core.processes.implementations.tablesync;


import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.basepull.ExtractViaBasepullQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertOrUpdateStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;


/*******************************************************************************
 ** Definition for Standard process to sync data from one table into another.
 **
 *******************************************************************************/
public class TableSyncProcess
{
   public static final String FIELD_SOURCE_TABLE_KEY_FIELD        = "sourceTableKeyField"; // String
   public static final String FIELD_DESTINATION_TABLE_FOREIGN_KEY = "destinationTableForeignKey"; // String



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Builder processMetaDataBuilder(boolean isBasePull)
   {
      return (Builder) new Builder(StreamedETLWithFrontendProcess.defineProcessMetaData(
         isBasePull ? ExtractViaBasepullQueryStep.class : ExtractViaQueryStep.class,
         null,
         LoadViaInsertOrUpdateStep.class,
         Collections.emptyMap()))
         .withPreviewStepInputFields(List.of(
            new QFieldMetaData(FIELD_SOURCE_TABLE_KEY_FIELD, QFieldType.STRING),
            new QFieldMetaData(FIELD_DESTINATION_TABLE_FOREIGN_KEY, QFieldType.STRING)
         ))
         .withPreviewMessage(StreamedETLWithFrontendProcess.DEFAULT_PREVIEW_MESSAGE_FOR_INSERT_OR_UPDATE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class Builder extends StreamedETLWithFrontendProcess.Builder
   {

      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public Builder(QProcessMetaData processMetaData)
      {
         super(processMetaData);
      }



      /*******************************************************************************
       ** Fluent setter for sourceTableKeyField
       **
       *******************************************************************************/
      public Builder withSourceTableKeyField(String sourceTableKeyField)
      {
         setInputFieldDefaultValue(FIELD_SOURCE_TABLE_KEY_FIELD, sourceTableKeyField);
         return (this);
      }



      /*******************************************************************************
       ** Fluent setter for destinationTableForeignKeyField
       **
       *******************************************************************************/
      public Builder withDestinationTableForeignKeyField(String destinationTableForeignKeyField)
      {
         setInputFieldDefaultValue(FIELD_DESTINATION_TABLE_FOREIGN_KEY, destinationTableForeignKeyField);
         return (this);
      }



      /*******************************************************************************
       ** Fluent setter for transformStepClass
       **
       *******************************************************************************/
      public Builder withSyncTransformStepClass(Class<? extends AbstractTableSyncTransformStep> transformStepClass)
      {
         setInputFieldDefaultValue(StreamedETLWithFrontendProcess.FIELD_TRANSFORM_CODE, new QCodeReference(transformStepClass));
         return (this);
      }



      /*******************************************************************************
       ** Fluent setter for sourceTable
       **
       *******************************************************************************/
      public Builder withSourceTable(String sourceTable)
      {
         setInputFieldDefaultValue(StreamedETLWithFrontendProcess.FIELD_SOURCE_TABLE, sourceTable);
         return (this);
      }



      /*******************************************************************************
       ** Fluent setter for destinationTable
       **
       *******************************************************************************/
      public Builder withDestinationTable(String destinationTable)
      {
         setInputFieldDefaultValue(StreamedETLWithFrontendProcess.FIELD_DESTINATION_TABLE, destinationTable);
         return (this);
      }



      /*******************************************************************************
       ** Fluent setter for name
       **
       *******************************************************************************/
      public Builder withName(String name)
      {
         processMetaData.setName(name);
         return (this);
      }



      /*******************************************************************************
       ** Fluent setter for label
       **
       *******************************************************************************/
      public Builder withLabel(String name)
      {
         processMetaData.setLabel(name);
         return (this);
      }



      /*******************************************************************************
       ** Fluent setter for tableName
       **
       *******************************************************************************/
      public Builder withTableName(String tableName)
      {
         processMetaData.setTableName(tableName);
         return (this);
      }



      /*******************************************************************************
       ** Fluent setter for icon
       **
       *******************************************************************************/
      public Builder withIcon(QIcon icon)
      {
         processMetaData.setIcon(icon);
         return (this);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      public Builder withReviewStepRecordFields(List<QFieldMetaData> fieldList)
      {
         QFrontendStepMetaData reviewStep = processMetaData.getFrontendStep(StreamedETLWithFrontendProcess.STEP_NAME_REVIEW);
         for(QFieldMetaData fieldMetaData : fieldList)
         {
            reviewStep.withRecordListField(fieldMetaData);
         }

         return (this);
      }
   }
}
