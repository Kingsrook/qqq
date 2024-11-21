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
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.WidgetHtmlLine;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.NoCodeWidgetFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.model.savedreports.ScheduledReport;


/*******************************************************************************
 ** define process for rendering scheduled reports - that is - a thin layer on
 ** top of rendering a saved report.
 *******************************************************************************/
public class RunScheduledReportMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "runScheduledReport";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData process = new QProcessMetaData()
         .withName(NAME)
         .withLabel("Run Scheduled Report")
         .withTableName(ScheduledReport.TABLE_NAME)
         .withIcon(new QIcon().withName("print"))

         .addStep(new QBackendStepMetaData()
            .withName("execute")
            .withInputData(new QFunctionInputMetaData().withRecordListMetaData(new QRecordListMetaData()
               .withTableName(ScheduledReport.TABLE_NAME)))
            .withCode(new QCodeReference(RunScheduledReportExecuteStep.class)))

         .addStep(new QFrontendStepMetaData()
            .withName("results")
            .withComponent(new NoCodeWidgetFrontendComponentMetaData()
               .withOutput(new WidgetHtmlLine().withVelocityTemplate("Success")))); // todo!!!

      return (process);
   }

}
