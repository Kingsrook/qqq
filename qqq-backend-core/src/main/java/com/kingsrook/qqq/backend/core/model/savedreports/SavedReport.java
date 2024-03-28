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

package com.kingsrook.qqq.backend.core.model.savedreports;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DynamicDefaultValueBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.tables.TablesPossibleValueSourceMetaDataProvider;


/*******************************************************************************
 ** Entity bean for the saved report table
 *******************************************************************************/
public class SavedReport extends QRecordEntity
{
   public static final String TABLE_NAME = "savedReport";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField(isRequired = true, maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String label;

   @QField(possibleValueSourceName = TablesPossibleValueSourceMetaDataProvider.NAME, maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String tableName; // todo - qqqTableId... ?

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.ERROR, dynamicDefaultValueBehavior = DynamicDefaultValueBehavior.USER_ID)
   private String userId;

   @QField(label = "Query Filter")
   private String queryFilterJson;

   @QField(label = "Columns")
   private String columnsJson;

   @QField(label = "Input Fields")
   private String inputFieldsJson;

   @QField(label = "Pivot Table")
   private String pivotTableJson;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SavedReport()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SavedReport(QRecord qRecord) throws QException
   {
      populateFromQRecord(qRecord);
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public Integer getId()
   {
      return id;
   }



   /*******************************************************************************
    ** Setter for id
    **
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Getter for createDate
    **
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return createDate;
   }



   /*******************************************************************************
    ** Setter for createDate
    **
    *******************************************************************************/
   public void setCreateDate(Instant createDate)
   {
      this.createDate = createDate;
   }



   /*******************************************************************************
    ** Getter for modifyDate
    **
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return modifyDate;
   }



   /*******************************************************************************
    ** Setter for modifyDate
    **
    *******************************************************************************/
   public void setModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
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
   public SavedReport withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    **
    *******************************************************************************/
   public SavedReport withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for userId
    **
    *******************************************************************************/
   public String getUserId()
   {
      return userId;
   }



   /*******************************************************************************
    ** Setter for userId
    **
    *******************************************************************************/
   public void setUserId(String userId)
   {
      this.userId = userId;
   }



   /*******************************************************************************
    ** Fluent setter for userId
    **
    *******************************************************************************/
   public SavedReport withUserId(String userId)
   {
      this.userId = userId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryFilterJson
    *******************************************************************************/
   public String getQueryFilterJson()
   {
      return (this.queryFilterJson);
   }



   /*******************************************************************************
    ** Setter for queryFilterJson
    *******************************************************************************/
   public void setQueryFilterJson(String queryFilterJson)
   {
      this.queryFilterJson = queryFilterJson;
   }



   /*******************************************************************************
    ** Fluent setter for queryFilterJson
    *******************************************************************************/
   public SavedReport withQueryFilterJson(String queryFilterJson)
   {
      this.queryFilterJson = queryFilterJson;
      return (this);
   }



   /*******************************************************************************
    ** Getter for columnsJson
    *******************************************************************************/
   public String getColumnsJson()
   {
      return (this.columnsJson);
   }



   /*******************************************************************************
    ** Setter for columnsJson
    *******************************************************************************/
   public void setColumnsJson(String columnsJson)
   {
      this.columnsJson = columnsJson;
   }



   /*******************************************************************************
    ** Fluent setter for columnsJson
    *******************************************************************************/
   public SavedReport withColumnsJson(String columnsJson)
   {
      this.columnsJson = columnsJson;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputFieldsJson
    *******************************************************************************/
   public String getInputFieldsJson()
   {
      return (this.inputFieldsJson);
   }



   /*******************************************************************************
    ** Setter for inputFieldsJson
    *******************************************************************************/
   public void setInputFieldsJson(String inputFieldsJson)
   {
      this.inputFieldsJson = inputFieldsJson;
   }



   /*******************************************************************************
    ** Fluent setter for inputFieldsJson
    *******************************************************************************/
   public SavedReport withInputFieldsJson(String inputFieldsJson)
   {
      this.inputFieldsJson = inputFieldsJson;
      return (this);
   }



   /*******************************************************************************
    ** Getter for pivotTableJson
    *******************************************************************************/
   public String getPivotTableJson()
   {
      return (this.pivotTableJson);
   }



   /*******************************************************************************
    ** Setter for pivotTableJson
    *******************************************************************************/
   public void setPivotTableJson(String pivotTableJson)
   {
      this.pivotTableJson = pivotTableJson;
   }



   /*******************************************************************************
    ** Fluent setter for pivotTableJson
    *******************************************************************************/
   public SavedReport withPivotTableJson(String pivotTableJson)
   {
      this.pivotTableJson = pivotTableJson;
      return (this);
   }


}
