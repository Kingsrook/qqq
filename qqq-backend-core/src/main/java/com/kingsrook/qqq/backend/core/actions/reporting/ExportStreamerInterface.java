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

package com.kingsrook.qqq.backend.core.actions.reporting;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportDestination;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportView;


/*******************************************************************************
 ** Interface for various export formats to implement.
 *******************************************************************************/
public interface ExportStreamerInterface
{

   /*******************************************************************************
    ** Called once, before any sheets are actually being produced.
    *******************************************************************************/
   default void preRun(ReportDestination reportDestination, List<QReportView> views) throws QReportingException
   {
      // noop in base class
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   default void setDisplayFormats(Map<String, String> displayFormats)
   {
      // noop in base class
   }

   /*******************************************************************************
    ** Called once per sheet, before any rows are available.  Meant to write a
    ** header, for example.
    **
    ** If multiple sheets are being created, there is no separate end-sheet call.
    ** Rather, a new one will just get started...
    *******************************************************************************/
   void start(ExportInput exportInput, List<QFieldMetaData> fields, String label, QReportView view) throws QReportingException;

   /*******************************************************************************
    ** Called as records flow into the pipe.
    ******************************************************************************/
   void addRecords(List<QRecord> recordList) throws QReportingException;

   /*******************************************************************************
    **
    *******************************************************************************/
   default void addTotalsRow(QRecord record) throws QReportingException
   {
      addRecords(List.of(record));
   }

   /*******************************************************************************
    ** Called after all sheets are complete.  Meant to do a final write, or close
    ** resources, for example.
    *******************************************************************************/
   void finish() throws QReportingException;

}
