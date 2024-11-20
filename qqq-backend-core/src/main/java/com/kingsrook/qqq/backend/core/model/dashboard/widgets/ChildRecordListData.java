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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets;


import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Model containing data structure expected by frontend ChildRecordList widget
 **
 *******************************************************************************/
public class ChildRecordListData extends QWidgetData
{
   private String         title;
   private QueryOutput    queryOutput;
   private QTableMetaData childTableMetaData;

   private String  tableName;
   private String  tablePath;
   private String  viewAllLink;
   private Integer totalRows;
   private Boolean disableRowClick   = false;
   private Boolean allowRecordEdit   = false;
   private Boolean allowRecordDelete = false;

   private boolean                   canAddChildRecord = false;
   private Map<String, Serializable> defaultValuesForNewChildRecords;
   private Set<String>               disabledFieldsForNewChildRecords;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ChildRecordListData(String title, QueryOutput queryOutput, QTableMetaData childTableMetaData, String tablePath, String viewAllLink, Integer totalRows)
   {
      this.title = title;
      this.queryOutput = queryOutput;
      this.childTableMetaData = childTableMetaData;
      this.tablePath = tablePath;
      this.viewAllLink = viewAllLink;
      this.totalRows = totalRows;
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.CHILD_RECORD_LIST.getType();
   }



   /*******************************************************************************
    ** Getter for title
    **
    *******************************************************************************/
   public String getTitle()
   {
      return title;
   }



   /*******************************************************************************
    ** Setter for title
    **
    *******************************************************************************/
   public void setTitle(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Fluent setter for title
    **
    *******************************************************************************/
   public ChildRecordListData withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryOutput
    **
    *******************************************************************************/
   public QueryOutput getQueryOutput()
   {
      return queryOutput;
   }



   /*******************************************************************************
    ** Setter for queryOutput
    **
    *******************************************************************************/
   public void setQueryOutput(QueryOutput queryOutput)
   {
      this.queryOutput = queryOutput;
   }



   /*******************************************************************************
    ** Fluent setter for queryOutput
    **
    *******************************************************************************/
   public ChildRecordListData withQueryOutput(QueryOutput queryOutput)
   {
      this.queryOutput = queryOutput;
      return (this);
   }



   /*******************************************************************************
    ** Getter for childTableMetaData
    **
    *******************************************************************************/
   public QTableMetaData getChildTableMetaData()
   {
      return childTableMetaData;
   }



   /*******************************************************************************
    ** Setter for childTableMetaData
    **
    *******************************************************************************/
   public void setChildTableMetaData(QTableMetaData childTableMetaData)
   {
      this.childTableMetaData = childTableMetaData;
   }



   /*******************************************************************************
    ** Fluent setter for childTableMetaData
    **
    *******************************************************************************/
   public ChildRecordListData withChildTableMetaData(QTableMetaData childTableMetaData)
   {
      this.childTableMetaData = childTableMetaData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tablePath
    **
    *******************************************************************************/
   public String getTablePath()
   {
      return tablePath;
   }



   /*******************************************************************************
    ** Setter for tablePath
    **
    *******************************************************************************/
   public void setTablePath(String tablePath)
   {
      this.tablePath = tablePath;
   }



   /*******************************************************************************
    ** Getter for viewAllLink
    **
    *******************************************************************************/
   public String getViewAllLink()
   {
      return viewAllLink;
   }



   /*******************************************************************************
    ** Setter for viewAllLink
    **
    *******************************************************************************/
   public void setViewAllLink(String viewAllLink)
   {
      this.viewAllLink = viewAllLink;
   }



   /*******************************************************************************
    ** Getter for canAddChildRecord
    **
    *******************************************************************************/
   public boolean getCanAddChildRecord()
   {
      return canAddChildRecord;
   }



   /*******************************************************************************
    ** Setter for canAddChildRecord
    **
    *******************************************************************************/
   public void setCanAddChildRecord(boolean canAddChildRecord)
   {
      this.canAddChildRecord = canAddChildRecord;
   }



   /*******************************************************************************
    ** Fluent setter for canAddChildRecord
    **
    *******************************************************************************/
   public ChildRecordListData withCanAddChildRecord(boolean canAddChildRecord)
   {
      this.canAddChildRecord = canAddChildRecord;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultValuesForNewChildRecords
    **
    *******************************************************************************/
   public Map<String, Serializable> getDefaultValuesForNewChildRecords()
   {
      return defaultValuesForNewChildRecords;
   }



   /*******************************************************************************
    ** Setter for defaultValuesForNewChildRecords
    **
    *******************************************************************************/
   public void setDefaultValuesForNewChildRecords(Map<String, Serializable> defaultValuesForNewChildRecords)
   {
      this.defaultValuesForNewChildRecords = defaultValuesForNewChildRecords;
   }



   /*******************************************************************************
    ** Fluent setter for defaultValuesForNewChildRecords
    **
    *******************************************************************************/
   public ChildRecordListData withDefaultValuesForNewChildRecords(Map<String, Serializable> defaultValuesForNewChildRecords)
   {
      this.defaultValuesForNewChildRecords = defaultValuesForNewChildRecords;
      return (this);
   }



   /*******************************************************************************
    ** Getter for disabledFieldsForNewChildRecords
    **
    *******************************************************************************/
   public Set<String> getDisabledFieldsForNewChildRecords()
   {
      return disabledFieldsForNewChildRecords;
   }



   /*******************************************************************************
    ** Setter for disabledFieldsForNewChildRecords
    **
    *******************************************************************************/
   public void setDisabledFieldsForNewChildRecords(Set<String> disabledFieldsForNewChildRecords)
   {
      this.disabledFieldsForNewChildRecords = disabledFieldsForNewChildRecords;
   }



   /*******************************************************************************
    ** Fluent setter for disabledFieldsForNewChildRecords
    **
    *******************************************************************************/
   public ChildRecordListData withDisabledFieldsForNewChildRecords(Set<String> disabledFieldsForNewChildRecords)
   {
      this.disabledFieldsForNewChildRecords = disabledFieldsForNewChildRecords;
      return (this);
   }



   /*******************************************************************************
    ** Getter for totalRows
    *******************************************************************************/
   public Integer getTotalRows()
   {
      return (this.totalRows);
   }



   /*******************************************************************************
    ** Setter for totalRows
    *******************************************************************************/
   public void setTotalRows(Integer totalRows)
   {
      this.totalRows = totalRows;
   }



   /*******************************************************************************
    ** Fluent setter for totalRows
    *******************************************************************************/
   public ChildRecordListData withTotalRows(Integer totalRows)
   {
      this.totalRows = totalRows;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public ChildRecordListData withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for tablePath
    *******************************************************************************/
   public ChildRecordListData withTablePath(String tablePath)
   {
      this.tablePath = tablePath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for disableRowClick
    *******************************************************************************/
   public Boolean getDisableRowClick()
   {
      return (this.disableRowClick);
   }



   /*******************************************************************************
    ** Setter for disableRowClick
    *******************************************************************************/
   public void setDisableRowClick(Boolean disableRowClick)
   {
      this.disableRowClick = disableRowClick;
   }



   /*******************************************************************************
    ** Fluent setter for disableRowClick
    *******************************************************************************/
   public ChildRecordListData withDisableRowClick(Boolean disableRowClick)
   {
      this.disableRowClick = disableRowClick;
      return (this);
   }



   /*******************************************************************************
    ** Getter for allowRecordEdit
    *******************************************************************************/
   public Boolean getAllowRecordEdit()
   {
      return (this.allowRecordEdit);
   }



   /*******************************************************************************
    ** Setter for allowRecordEdit
    *******************************************************************************/
   public void setAllowRecordEdit(Boolean allowRecordEdit)
   {
      this.allowRecordEdit = allowRecordEdit;
   }



   /*******************************************************************************
    ** Fluent setter for allowRecordEdit
    *******************************************************************************/
   public ChildRecordListData withAllowRecordEdit(Boolean allowRecordEdit)
   {
      this.allowRecordEdit = allowRecordEdit;
      return (this);
   }



   /*******************************************************************************
    ** Getter for allowRecordDelete
    *******************************************************************************/
   public Boolean getAllowRecordDelete()
   {
      return (this.allowRecordDelete);
   }



   /*******************************************************************************
    ** Setter for allowRecordDelete
    *******************************************************************************/
   public void setAllowRecordDelete(Boolean allowRecordDelete)
   {
      this.allowRecordDelete = allowRecordDelete;
   }



   /*******************************************************************************
    ** Fluent setter for allowRecordDelete
    *******************************************************************************/
   public ChildRecordListData withAllowRecordDelete(Boolean allowRecordDelete)
   {
      this.allowRecordDelete = allowRecordDelete;
      return (this);
   }
}



