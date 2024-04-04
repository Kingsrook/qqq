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

package com.kingsrook.qqq.backend.core.processes.implementations.savedreports;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormatPossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;


/*******************************************************************************
 ** define process for rendering saved reports!
 *******************************************************************************/
public class RenderSavedReportMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "renderSavedReport";

   public static final String FIELD_NAME_STORAGE_TABLE_NAME = "storageTableName";
   public static final String FIELD_NAME_REPORT_FORMAT      = "reportFormat";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData process = new QProcessMetaData()
         .withName(NAME)
         .withTableName(SavedReport.TABLE_NAME)
         .withIcon(new QIcon().withName("print"))
         .addStep(new QBackendStepMetaData()
            .withName("pre")
            .withInputData(new QFunctionInputMetaData()
               .withField(new QFieldMetaData(FIELD_NAME_STORAGE_TABLE_NAME, QFieldType.STRING))
               .withRecordListMetaData(new QRecordListMetaData().withTableName(SavedReport.TABLE_NAME)))
            .withCode(new QCodeReference(RenderSavedReportPreStep.class)))
         .addStep(new QFrontendStepMetaData()
            .withName("input")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData(FIELD_NAME_REPORT_FORMAT, QFieldType.STRING)
               .withPossibleValueSourceName(ReportFormatPossibleValueEnum.NAME)
               .withIsRequired(true)))
         .addStep(new QBackendStepMetaData()
            .withName("execute")
            .withInputData(new QFunctionInputMetaData().withRecordListMetaData(new QRecordListMetaData()
               .withTableName(SavedReport.TABLE_NAME)))
            .withCode(new QCodeReference(RenderSavedReportExecuteStep.class)))
         .addStep(new QFrontendStepMetaData()
            .withName("output")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.DOWNLOAD_FORM)));

      return (process);
   }

}
