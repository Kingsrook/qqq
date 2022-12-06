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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Model containing data structure expected by frontend ChildRecordList widget
 **
 *******************************************************************************/
public class ChildRecordListData implements QWidget
{
   private String         title;
   private QueryOutput    queryOutput;
   private QTableMetaData childTableMetaData;

   private String tablePath;
   private String viewAllLink;

   private boolean                   canAddChildRecord = false;
   private Map<String, Serializable> defaultValuesForNewChildRecords;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ChildRecordListData(String title, QueryOutput queryOutput, QTableMetaData childTableMetaData, String tablePath, String viewAllLink)
   {
      this.title = title;
      this.queryOutput = queryOutput;
      this.childTableMetaData = childTableMetaData;
      this.tablePath = tablePath;
      this.viewAllLink = viewAllLink;
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

}
