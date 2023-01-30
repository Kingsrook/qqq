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
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;


/*******************************************************************************
 ** Input for an Export action
 *******************************************************************************/
public class ExportInput extends AbstractTableActionInput
{
   private QQueryFilter queryFilter;
   private Integer      limit;
   private List<String> fieldNames;

   private String       filename;
   private ReportFormat reportFormat;
   private OutputStream reportOutputStream;
   private String       titleRow;
   private boolean      includeHeaderRow = true;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ExportInput()
   {
   }



   /*******************************************************************************
    ** Getter for queryFilter
    **
    *******************************************************************************/
   public QQueryFilter getQueryFilter()
   {
      return queryFilter;
   }



   /*******************************************************************************
    ** Setter for queryFilter
    **
    *******************************************************************************/
   public void setQueryFilter(QQueryFilter queryFilter)
   {
      this.queryFilter = queryFilter;
   }



   /*******************************************************************************
    ** Getter for limit
    **
    *******************************************************************************/
   public Integer getLimit()
   {
      return limit;
   }



   /*******************************************************************************
    ** Setter for limit
    **
    *******************************************************************************/
   public void setLimit(Integer limit)
   {
      this.limit = limit;
   }



   /*******************************************************************************
    ** Getter for fieldNames
    **
    *******************************************************************************/
   public List<String> getFieldNames()
   {
      return fieldNames;
   }



   /*******************************************************************************
    ** Setter for fieldNames
    **
    *******************************************************************************/
   public void setFieldNames(List<String> fieldNames)
   {
      this.fieldNames = fieldNames;
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



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getTitleRow()
   {
      return titleRow;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setTitleRow(String titleRow)
   {
      this.titleRow = titleRow;
   }



   /*******************************************************************************
    ** Getter for includeHeaderRow
    **
    *******************************************************************************/
   public boolean getIncludeHeaderRow()
   {
      return includeHeaderRow;
   }



   /*******************************************************************************
    ** Setter for includeHeaderRow
    **
    *******************************************************************************/
   public void setIncludeHeaderRow(boolean includeHeaderRow)
   {
      this.includeHeaderRow = includeHeaderRow;
   }

}
