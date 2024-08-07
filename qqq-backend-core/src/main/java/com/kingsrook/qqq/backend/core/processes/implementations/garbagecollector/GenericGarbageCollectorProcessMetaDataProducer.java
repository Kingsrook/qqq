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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Generic process that can perform garbage collection on any table (at least,
 ** any table with a date or date-time field).
 **
 ** When running, this process prompts for:
 ** - table name
 ** - field name (e.g., the date/date-time field on that table)
 ** - daysBack - any records older than that many days ago will be deleted.
 ** - maxPageSize - to avoid running "1 huge query", if there are more than
 ** this number of records between the min-date in the table and the max-date
 ** (based on daysBack), then the time range is partitioned recursively until
 ** pages smaller than this parameter are found.  The partitioning attempts to
 ** be smart (e.g., not just ÷ 2), by doing count / maxPageSize.
 *******************************************************************************/
public class GenericGarbageCollectorProcessMetaDataProducer extends MetaDataProducer<QProcessMetaData>
{
   public static final String NAME = "GenericGarbageCollector";



   /*******************************************************************************
    ** See class header for param descriptions.
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData processMetaData = new QProcessMetaData()
         .withName(NAME)
         .withIcon(new QIcon().withName("auto_delete"))
         .withStepList(List.of(
            new QFrontendStepMetaData()
               .withName("input")
               .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
               .withFormField(new QFieldMetaData("table", QFieldType.STRING))
               .withFormField(new QFieldMetaData("field", QFieldType.STRING))
               .withFormField(new QFieldMetaData("daysBack", QFieldType.INTEGER).withDefaultValue(90))
               .withFormField(new QFieldMetaData("maxPageSize", QFieldType.INTEGER).withDefaultValue(100000)),
            new QBackendStepMetaData()
               .withName("execute")
               .withCode(new QCodeReference(GenericGarbageCollectorExecuteStep.class)),
            new QFrontendStepMetaData()
               .withName("result")
               .withComponent(new QFrontendComponentMetaData().withType(QComponentType.PROCESS_SUMMARY_RESULTS))
         ));

      return (processMetaData);
   }

}
