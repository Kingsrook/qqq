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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;


/*******************************************************************************
 ** Definition for Streamed ETL process that includes a frontend.
 **
 ** This process uses 2 backend steps, and 2 frontend steps, as follows:
 ** - preview (backend) - does just a little work (limited # of rows), to give the
 **      user a preview of what the final result will be - e.g., some data to seed the review screen
 ** - review (frontend) - a review screen
 ** - execute (backend) - processes all the rows, does all the work.
 ** - result (frontend) - a result screen
 **
 ** The preview & execute steps use additional BackendStep codes:
 ** - Extract - gets the rows to be processed.  Used in preview (but only for a
 **      limited number of rows), and execute (without limit)
 ** - Transform - do whatever transformation is needed to the rows.  Done on preview
 **      and execute.  Always works with a "page" of records at a time.
 ** - Load - store the records into the backend, as appropriate.  Always works
 **      with a "page" of records at a time.
 *******************************************************************************/
public class StreamedETLWithFrontendProcess
{
   public static final String PROCESS_NAME = "etl.streamedWithFrontend";

   public static final String STEP_NAME_PREVIEW = "preview";
   public static final String STEP_NAME_REVIEW  = "review";
   public static final String STEP_NAME_EXECUTE = "execute";
   public static final String STEP_NAME_RESULT  = "result";

   public static final String FIELD_EXTRACT_CODE   = "extract";
   public static final String FIELD_TRANSFORM_CODE = "transform";
   public static final String FIELD_LOAD_CODE      = "load";

   public static final String FIELD_SOURCE_TABLE         = "sourceTable";
   public static final String FIELD_DEFAULT_QUERY_FILTER = "defaultQueryFilter";
   public static final String FIELD_DESTINATION_TABLE    = "destinationTable";



   /*******************************************************************************
    **
    *******************************************************************************/
   public QProcessMetaData defineProcessMetaData(
      String sourceTableName,
      String destinationTableName,
      Class<? extends AbstractExtractStep> extractStepClass,
      Class<? extends AbstractTransformStep> transformStepClass,
      Class<? extends AbstractLoadStep> loadStepClass
   )
   {
      QStepMetaData previewStep = new QBackendStepMetaData()
         .withName(STEP_NAME_PREVIEW)
         .withCode(new QCodeReference(StreamedETLPreviewStep.class))
         .withInputData(new QFunctionInputMetaData()
            .withField(new QFieldMetaData().withName(FIELD_SOURCE_TABLE).withDefaultValue(sourceTableName))
            .withField(new QFieldMetaData().withName(FIELD_DEFAULT_QUERY_FILTER))
            .withField(new QFieldMetaData().withName(FIELD_EXTRACT_CODE).withDefaultValue(new QCodeReference(extractStepClass)))
            .withField(new QFieldMetaData().withName(FIELD_TRANSFORM_CODE).withDefaultValue(new QCodeReference(transformStepClass))));

      QFrontendStepMetaData reviewStep = new QFrontendStepMetaData()
         .withName(STEP_NAME_REVIEW);

      QStepMetaData executeStep = new QBackendStepMetaData()
         .withName(STEP_NAME_EXECUTE)
         .withCode(new QCodeReference(StreamedETLExecuteStep.class))
         .withInputData(new QFunctionInputMetaData()
            .withField(new QFieldMetaData().withName(FIELD_DESTINATION_TABLE).withDefaultValue(destinationTableName))
            .withField(new QFieldMetaData().withName(FIELD_LOAD_CODE).withDefaultValue(new QCodeReference(loadStepClass))));

      QFrontendStepMetaData resultStep = new QFrontendStepMetaData()
         .withName(STEP_NAME_RESULT);

      return new QProcessMetaData()
         .withName(PROCESS_NAME)
         .addStep(previewStep)
         .addStep(reviewStep)
         .addStep(executeStep)
         .addStep(resultStep);
   }
}
