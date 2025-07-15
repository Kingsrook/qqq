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

package com.kingsrook.qqq.backend.core.processes.implementations.reports;


import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;


/*******************************************************************************
 ** Definition for Basic process to run a report.
 *******************************************************************************/
public class BasicRunReportProcess
{
   public static final String PROCESS_NAME = "reports.basic";

   public static final String STEP_NAME_PREPARE = "prepare";
   public static final String STEP_NAME_INPUT   = "input";
   public static final String STEP_NAME_EXECUTE = "execute";
   public static final String STEP_NAME_ACCESS  = "accessReport";

   public static final String FIELD_REPORT_NAME   = "reportName";
   public static final String FIELD_REPORT_FORMAT = "reportFormat";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessMetaData defineProcessMetaData()
   {
      QStepMetaData prepareStep = new QBackendStepMetaData()
         .withName(STEP_NAME_PREPARE)
         .withCode(new QCodeReference(PrepareReportStep.class))
         .withInputData(new QFunctionInputMetaData()
            .withField(new QFieldMetaData(FIELD_REPORT_NAME, QFieldType.STRING)));

      QStepMetaData inputStep = new QFrontendStepMetaData()
         .withName(STEP_NAME_INPUT)
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM));

      QStepMetaData executeStep = new QBackendStepMetaData()
         .withName(STEP_NAME_EXECUTE)
         .withCode(new QCodeReference(ExecuteReportStep.class))
         .withInputData(new QFunctionInputMetaData()
            .withField(new QFieldMetaData(FIELD_REPORT_NAME, QFieldType.STRING)));

      QStepMetaData accessStep = new QFrontendStepMetaData()
         .withName(STEP_NAME_ACCESS)
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.DOWNLOAD_FORM));
      // .withViewField(new QFieldMetaData("outputFile", QFieldType.STRING))
      // .withViewField(new QFieldMetaData("message", QFieldType.STRING));

      return new QProcessMetaData()
         .withName(PROCESS_NAME)
         .withIsHidden(true)
         .withStep(prepareStep)
         .withStep(inputStep)
         .withStep(executeStep)
         .withStep(accessStep);
   }

}
