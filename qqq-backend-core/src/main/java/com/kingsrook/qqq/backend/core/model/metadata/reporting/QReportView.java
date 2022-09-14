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

package com.kingsrook.qqq.backend.core.model.metadata.reporting;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;


/*******************************************************************************
 **
 *******************************************************************************/
public class QReportView
{
   private String               name;
   private String               label;
   private ReportType           type;
   private String               titleFormat;
   private List<String>         titleFields;
   private List<String>         pivotFields;
   private boolean              totalRow = false;
   private List<QReportField>   columns;
   private List<QFilterOrderBy> orderByFields;



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    **
    *******************************************************************************/
   public QReportView withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    **
    *******************************************************************************/
   public QReportView withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public ReportType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(ReportType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    **
    *******************************************************************************/
   public QReportView withType(ReportType type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for titleFormat
    **
    *******************************************************************************/
   public String getTitleFormat()
   {
      return titleFormat;
   }



   /*******************************************************************************
    ** Setter for titleFormat
    **
    *******************************************************************************/
   public void setTitleFormat(String titleFormat)
   {
      this.titleFormat = titleFormat;
   }



   /*******************************************************************************
    ** Fluent setter for titleFormat
    **
    *******************************************************************************/
   public QReportView withTitleFormat(String titleFormat)
   {
      this.titleFormat = titleFormat;
      return (this);
   }



   /*******************************************************************************
    ** Getter for titleFields
    **
    *******************************************************************************/
   public List<String> getTitleFields()
   {
      return titleFields;
   }



   /*******************************************************************************
    ** Setter for titleFields
    **
    *******************************************************************************/
   public void setTitleFields(List<String> titleFields)
   {
      this.titleFields = titleFields;
   }



   /*******************************************************************************
    ** Fluent setter for titleFields
    **
    *******************************************************************************/
   public QReportView withTitleFields(List<String> titleFields)
   {
      this.titleFields = titleFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for pivotFields
    **
    *******************************************************************************/
   public List<String> getPivotFields()
   {
      return pivotFields;
   }



   /*******************************************************************************
    ** Setter for pivotFields
    **
    *******************************************************************************/
   public void setPivotFields(List<String> pivotFields)
   {
      this.pivotFields = pivotFields;
   }



   /*******************************************************************************
    ** Fluent setter for pivotFields
    **
    *******************************************************************************/
   public QReportView withPivotFields(List<String> pivotFields)
   {
      this.pivotFields = pivotFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for totalRow
    **
    *******************************************************************************/
   public boolean getTotalRow()
   {
      return totalRow;
   }



   /*******************************************************************************
    ** Setter for totalRow
    **
    *******************************************************************************/
   public void setTotalRow(boolean totalRow)
   {
      this.totalRow = totalRow;
   }



   /*******************************************************************************
    ** Fluent setter for totalRow
    **
    *******************************************************************************/
   public QReportView withTotalRow(boolean totalRow)
   {
      this.totalRow = totalRow;
      return (this);
   }



   /*******************************************************************************
    ** Getter for columns
    **
    *******************************************************************************/
   public List<QReportField> getColumns()
   {
      return columns;
   }



   /*******************************************************************************
    ** Setter for columns
    **
    *******************************************************************************/
   public void setColumns(List<QReportField> columns)
   {
      this.columns = columns;
   }



   /*******************************************************************************
    ** Fluent setter for columns
    **
    *******************************************************************************/
   public QReportView withColumns(List<QReportField> columns)
   {
      this.columns = columns;
      return (this);
   }



   /*******************************************************************************
    ** Getter for orderByFields
    **
    *******************************************************************************/
   public List<QFilterOrderBy> getOrderByFields()
   {
      return orderByFields;
   }



   /*******************************************************************************
    ** Setter for orderByFields
    **
    *******************************************************************************/
   public void setOrderByFields(List<QFilterOrderBy> orderByFields)
   {
      this.orderByFields = orderByFields;
   }



   /*******************************************************************************
    ** Fluent setter for orderByFields
    **
    *******************************************************************************/
   public QReportView withOrderByFields(List<QFilterOrderBy> orderByFields)
   {
      this.orderByFields = orderByFields;
      return (this);
   }

}
