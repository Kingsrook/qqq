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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Interface for various report formats to implement.
 *******************************************************************************/
public interface ReportStreamerInterface
{
   /*******************************************************************************
    ** Called once, before any rows are available.  Meant to write a header, for example.
    *******************************************************************************/
   void start(ReportInput reportInput) throws QReportingException;

   /*******************************************************************************
    ** Called as records flow into the pipe.
    ******************************************************************************/
   int takeRecordsFromPipe(RecordPipe recordPipe) throws QReportingException;

   /*******************************************************************************
    ** Called once, after all rows are available.  Meant to write a footer, or close resources, for example.
    *******************************************************************************/
   void finish() throws QReportingException;

   /*******************************************************************************
    ** (Ideally, protected) method used within report streamer implementations, to
    ** map field names from reportInput into list of fieldMetaData.
    *******************************************************************************/
   default List<QFieldMetaData> setupFieldList(QTableMetaData table, ReportInput reportInput)
   {
      if(reportInput.getFieldNames() != null)
      {
         return (reportInput.getFieldNames().stream().map(table::getField).toList());
      }
      else
      {
         return (new ArrayList<>(table.getFields().values()));
      }
   }

}
