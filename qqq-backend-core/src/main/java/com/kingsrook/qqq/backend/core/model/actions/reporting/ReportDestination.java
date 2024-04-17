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

package com.kingsrook.qqq.backend.core.model.actions.reporting;


import java.io.OutputStream;


/*******************************************************************************
 ** Member of report & export Inputs, that wraps details about the destination of
 ** where & how the report (or export) is being written.
 *******************************************************************************/
public class ReportDestination
{
   private String       filename;
   private ReportFormat reportFormat;
   private OutputStream reportOutputStream;



   /*******************************************************************************
    ** Getter for filename
    *******************************************************************************/
   public String getFilename()
   {
      return (this.filename);
   }



   /*******************************************************************************
    ** Setter for filename
    *******************************************************************************/
   public void setFilename(String filename)
   {
      this.filename = filename;
   }



   /*******************************************************************************
    ** Fluent setter for filename
    *******************************************************************************/
   public ReportDestination withFilename(String filename)
   {
      this.filename = filename;
      return (this);
   }



   /*******************************************************************************
    ** Getter for reportFormat
    *******************************************************************************/
   public ReportFormat getReportFormat()
   {
      return (this.reportFormat);
   }



   /*******************************************************************************
    ** Setter for reportFormat
    *******************************************************************************/
   public void setReportFormat(ReportFormat reportFormat)
   {
      this.reportFormat = reportFormat;
   }



   /*******************************************************************************
    ** Fluent setter for reportFormat
    *******************************************************************************/
   public ReportDestination withReportFormat(ReportFormat reportFormat)
   {
      this.reportFormat = reportFormat;
      return (this);
   }



   /*******************************************************************************
    ** Getter for reportOutputStream
    *******************************************************************************/
   public OutputStream getReportOutputStream()
   {
      return (this.reportOutputStream);
   }



   /*******************************************************************************
    ** Setter for reportOutputStream
    *******************************************************************************/
   public void setReportOutputStream(OutputStream reportOutputStream)
   {
      this.reportOutputStream = reportOutputStream;
   }



   /*******************************************************************************
    ** Fluent setter for reportOutputStream
    *******************************************************************************/
   public ReportDestination withReportOutputStream(OutputStream reportOutputStream)
   {
      this.reportOutputStream = reportOutputStream;
      return (this);
   }

}
