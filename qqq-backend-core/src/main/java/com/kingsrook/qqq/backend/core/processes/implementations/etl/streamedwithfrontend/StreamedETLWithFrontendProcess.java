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


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionOutputMetaData;
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
   public static final String STEP_NAME_PREVIEW  = "preview";
   public static final String STEP_NAME_REVIEW   = "review";
   public static final String STEP_NAME_VALIDATE = "validate";
   public static final String STEP_NAME_EXECUTE  = "execute";
   public static final String STEP_NAME_RESULT   = "result";

   public static final String FIELD_EXTRACT_CODE   = "extract"; // QCodeReference, of AbstractExtractStep
   public static final String FIELD_TRANSFORM_CODE = "transform"; // QCodeReference, of AbstractTransformStep
   public static final String FIELD_LOAD_CODE      = "load"; // QCodeReference, of AbstractLoadStep

   public static final String FIELD_SOURCE_TABLE         = "sourceTable"; // String
   public static final String FIELD_DESTINATION_TABLE    = "destinationTable"; // String
   public static final String FIELD_RECORD_COUNT         = "recordCount"; // Integer
   public static final String FIELD_DEFAULT_QUERY_FILTER = "defaultQueryFilter"; // QQueryFilter or String (json, of q QQueryFilter)

   public static final String FIELD_SUPPORTS_FULL_VALIDATION = "supportsFullValidation"; // Boolean
   public static final String FIELD_DO_FULL_VALIDATION       = "doFullValidation"; // Boolean
   public static final String FIELD_VALIDATION_SUMMARY       = "validationSummary"; // List<ProcessSummaryLine>
   public static final String FIELD_PROCESS_SUMMARY          = "processResults"; // List<ProcessSummaryLine>



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessMetaData defineProcessMetaData(
      String sourceTableName,
      String destinationTableName,
      Class<? extends AbstractExtractStep> extractStepClass,
      Class<? extends AbstractTransformStep> transformStepClass,
      Class<? extends AbstractLoadStep> loadStepClass
   )
   {
      Map<String, Serializable> defaultFieldValues = new HashMap<>();
      defaultFieldValues.put(FIELD_SOURCE_TABLE, sourceTableName);
      defaultFieldValues.put(FIELD_DESTINATION_TABLE, destinationTableName);
      return defineProcessMetaData(extractStepClass, transformStepClass, loadStepClass, defaultFieldValues);
   }



   /*******************************************************************************
    ** @param defaultFieldValues - expected to possibly contain values for the following field names:
    ** - FIELD_SOURCE_TABLE
    ** - FIELD_DESTINATION_TABLE
    ** - FIELD_SUPPORTS_FULL_VALIDATION
    ** - FIELD_DEFAULT_QUERY_FILTER
    ** - FIELD_DO_FULL_VALIDATION
    *******************************************************************************/
   public static QProcessMetaData defineProcessMetaData(
      Class<? extends AbstractExtractStep> extractStepClass,
      Class<? extends AbstractTransformStep> transformStepClass,
      Class<? extends AbstractLoadStep> loadStepClass,
      Map<String, Serializable> defaultFieldValues
   )
   {
      QStepMetaData previewStep = new QBackendStepMetaData()
         .withName(STEP_NAME_PREVIEW)
         .withCode(new QCodeReference(StreamedETLPreviewStep.class))
         .withInputData(new QFunctionInputMetaData()
            .withField(new QFieldMetaData(FIELD_SOURCE_TABLE, QFieldType.STRING).withDefaultValue(defaultFieldValues.get(FIELD_SOURCE_TABLE)))
            .withField(new QFieldMetaData(FIELD_SUPPORTS_FULL_VALIDATION, QFieldType.BOOLEAN).withDefaultValue(defaultFieldValues.getOrDefault(FIELD_SUPPORTS_FULL_VALIDATION, false)))
            .withField(new QFieldMetaData(FIELD_DEFAULT_QUERY_FILTER, QFieldType.STRING).withDefaultValue(defaultFieldValues.get(FIELD_DEFAULT_QUERY_FILTER)))
            .withField(new QFieldMetaData(FIELD_EXTRACT_CODE, QFieldType.STRING).withDefaultValue(new QCodeReference(extractStepClass)))
            .withField(new QFieldMetaData(FIELD_TRANSFORM_CODE, QFieldType.STRING).withDefaultValue(new QCodeReference(transformStepClass)))
         );

      QFrontendStepMetaData reviewStep = new QFrontendStepMetaData()
         .withName(STEP_NAME_REVIEW)
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VALIDATION_REVIEW_SCREEN));

      QStepMetaData validateStep = new QBackendStepMetaData()
         .withName(STEP_NAME_VALIDATE)
         .withCode(new QCodeReference(StreamedETLValidateStep.class))
         .withInputData(new QFunctionInputMetaData()
            .withField(new QFieldMetaData(FIELD_DO_FULL_VALIDATION, QFieldType.BOOLEAN).withDefaultValue(defaultFieldValues.get(FIELD_DO_FULL_VALIDATION))))
         .withOutputMetaData(new QFunctionOutputMetaData()
            .withField(new QFieldMetaData(FIELD_VALIDATION_SUMMARY, QFieldType.STRING))
         );

      QStepMetaData executeStep = new QBackendStepMetaData()
         .withName(STEP_NAME_EXECUTE)
         .withCode(new QCodeReference(StreamedETLExecuteStep.class))
         .withInputData(new QFunctionInputMetaData()
            .withField(new QFieldMetaData(FIELD_DESTINATION_TABLE, QFieldType.STRING).withDefaultValue(defaultFieldValues.get(FIELD_DESTINATION_TABLE)))
            .withField(new QFieldMetaData(FIELD_LOAD_CODE, QFieldType.STRING).withDefaultValue(new QCodeReference(loadStepClass))))
         .withOutputMetaData(new QFunctionOutputMetaData()
            .withField(new QFieldMetaData(FIELD_PROCESS_SUMMARY, QFieldType.STRING))
         );

      QFrontendStepMetaData resultStep = new QFrontendStepMetaData()
         .withName(STEP_NAME_RESULT)
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.PROCESS_SUMMARY_RESULTS));

      return new QProcessMetaData()
         .addStep(previewStep)
         .addStep(reviewStep)
         .addStep(validateStep)
         .addStep(executeStep)
         .addStep(resultStep);
   }
}
