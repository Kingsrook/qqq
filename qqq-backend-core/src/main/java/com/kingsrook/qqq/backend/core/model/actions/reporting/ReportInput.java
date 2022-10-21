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


import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Input for an Export action
 *******************************************************************************/
public class ReportInput extends AbstractTableActionInput
{
   private String                    reportName;
   private Map<String, Serializable> inputValues;

   private String       filename;
   private ReportFormat reportFormat;
   private OutputStream reportOutputStream;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ReportInput()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ReportInput(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ReportInput(QInstance instance, QSession session)
   {
      super(instance);
      setSession(session);
   }



   /*******************************************************************************
    ** Getter for reportName
    **
    *******************************************************************************/
   public String getReportName()
   {
      return reportName;
   }



   /*******************************************************************************
    ** Setter for reportName
    **
    *******************************************************************************/
   public void setReportName(String reportName)
   {
      this.reportName = reportName;
   }



   /*******************************************************************************
    ** Getter for inputValues
    **
    *******************************************************************************/
   public Map<String, Serializable> getInputValues()
   {
      return inputValues;
   }



   /*******************************************************************************
    ** Setter for inputValues
    **
    *******************************************************************************/
   public void setInputValues(Map<String, Serializable> inputValues)
   {
      this.inputValues = inputValues;
   }



   /*******************************************************************************
    ** Getter for filename
    **
    *******************************************************************************/
   public String getFilename()
   {
      return filename;
   }



   /*******************************************************************************
    ** Setter for filename
    **
    *******************************************************************************/
   public void setFilename(String filename)
   {
      this.filename = filename;
   }



   /*******************************************************************************
    ** Getter for reportFormat
    **
    *******************************************************************************/
   public ReportFormat getReportFormat()
   {
      return reportFormat;
   }



   /*******************************************************************************
    ** Setter for reportFormat
    **
    *******************************************************************************/
   public void setReportFormat(ReportFormat reportFormat)
   {
      this.reportFormat = reportFormat;
   }



   /*******************************************************************************
    ** Getter for reportOutputStream
    **
    *******************************************************************************/
   public OutputStream getReportOutputStream()
   {
      return reportOutputStream;
   }



   /*******************************************************************************
    ** Setter for reportOutputStream
    **
    *******************************************************************************/
   public void setReportOutputStream(OutputStream reportOutputStream)
   {
      this.reportOutputStream = reportOutputStream;
   }
}
