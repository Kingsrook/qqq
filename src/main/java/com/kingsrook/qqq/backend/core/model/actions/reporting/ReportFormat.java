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

package com.kingsrook.qqq.backend.core.model.actions.reporting;


import java.util.Locale;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.reporting.CsvReportStreamer;
import com.kingsrook.qqq.backend.core.actions.reporting.ExcelReportStreamer;
import com.kingsrook.qqq.backend.core.actions.reporting.ReportStreamerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.dhatim.fastexcel.Worksheet;


/*******************************************************************************
 ** QQQ Report/export file formats
 *******************************************************************************/
public enum ReportFormat
{
   XLSX(Worksheet.MAX_ROWS, ExcelReportStreamer::new, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
   CSV(null, CsvReportStreamer::new, "text/csv");


   private final Integer maxRows;
   private final String  mimeType;

   private final Supplier<? extends ReportStreamerInterface> streamerConstructor;



   /*******************************************************************************
    **
    *******************************************************************************/
   ReportFormat(Integer maxRows, Supplier<? extends ReportStreamerInterface> streamerConstructor, String mimeType)
   {
      this.maxRows = maxRows;
      this.mimeType = mimeType;
      this.streamerConstructor = streamerConstructor;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ReportFormat fromString(String format) throws QUserFacingException
   {
      if(!StringUtils.hasContent(format))
      {
         throw (new QUserFacingException("Report format was not specified."));
      }

      try
      {
         return (ReportFormat.valueOf(format.toUpperCase(Locale.ROOT)));
      }
      catch(IllegalArgumentException iae)
      {
         throw (new QUserFacingException("Unsupported report format: " + format + "."));
      }
   }



   /*******************************************************************************
    ** Getter for maxRows
    **
    *******************************************************************************/
   public Integer getMaxRows()
   {
      return maxRows;
   }



   /*******************************************************************************
    ** Getter for mimeType
    **
    *******************************************************************************/
   public String getMimeType()
   {
      return mimeType;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ReportStreamerInterface newReportStreamer()
   {
      return (streamerConstructor.get());
   }
}
