/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.garbagecollector;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.NowWithOffset;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaDeleteStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import static com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator.LESS_THAN;


/*******************************************************************************
 ** Create a garbage collector process for a given table.
 **
 ** Process will be named:  tableName + "GarbageCollector"
 **
 ** It requires a dateTime field which is used in the query to find old records
 ** to be deleted.  This dateTime field is, by default, compared with the input
 ** 'nowWithOffset' (e.g., .minus(30, DAYS)).
 **
 ** Child join tables can also be GC'ed.  This behavior is controlled via the
 ** joinedTablesToAlsoDelete parameter, which behaves as follows:
 ** - if the value is "*", then ALL descendent joins are GC'ed from.
 ** - if the value is null, then NO descendent joins are GC'ed from.
 ** - else the value is split on commas, and only table names found in the split are GC'ed.
 **
 ** The process is, by default, associated with its associated table, so it can
 ** show up in UI's if permissed as such.  When ran in a UI, it presents a limitDate
 ** field, which users can use to override the default limit.
 **
 ** It does not get a schedule by default.
 **
 *******************************************************************************/
public class GarbageCollectorProcessMetaDataProducer
{

   /*******************************************************************************
    ** See class header for param descriptions.
    *******************************************************************************/
   public static QProcessMetaData createProcess(String tableName, String dateTimeField, NowWithOffset nowWithOffset, String joinedTablesToAlsoDelete)
   {
      QProcessMetaData processMetaData = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(tableName + "GarbageCollector")
         .withIcon(new QIcon().withName("auto_delete"))
         .withTableName(tableName)
         .withSourceTable(tableName)
         .withDestinationTable(tableName)
         .withExtractStepClass(GarbageCollectorExtractStep.class)
         .withTransformStepClass(GarbageCollectorTransformStep.class)
         .withLoadStepClass(LoadViaDeleteStep.class)
         .withTransactionLevelPage()
         .withPreviewMessage(StreamedETLWithFrontendProcess.DEFAULT_PREVIEW_MESSAGE_FOR_DELETE)
         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("id", QFieldType.INTEGER),
            new QFieldMetaData(dateTimeField, QFieldType.DATE_TIME)
         ))
         .withDefaultQueryFilter(new QQueryFilter(new QFilterCriteria(dateTimeField, LESS_THAN, nowWithOffset)))
         .getProcessMetaData();

      processMetaData.getBackendStep(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE)
         .withInputData(new QFunctionInputMetaData()
            .withField(new QFieldMetaData("joinedTablesToAlsoDelete", QFieldType.STRING).withDefaultValue(joinedTablesToAlsoDelete)));

      processMetaData.withStep(0, new QFrontendStepMetaData()
         .withName("input")
         .withLabel("Input")
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.HELP_TEXT).withValue("text", """
            You can specify a limit date, or let the system use its default.
            """))
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
         .withFormField(new QFieldMetaData("limitDate", QFieldType.DATE_TIME))
      );

      return (processMetaData);
   }

}
