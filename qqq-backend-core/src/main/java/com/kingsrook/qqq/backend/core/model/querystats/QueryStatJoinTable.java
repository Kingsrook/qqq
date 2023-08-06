/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.querystats;


import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.tables.QQQTable;


/*******************************************************************************
 ** QRecord Entity for QueryStatJoinTable table
 *******************************************************************************/
public class QueryStatJoinTable extends QRecordEntity
{
   public static final String TABLE_NAME = "queryStatJoinTable"; // todo - lowercase the first letter

   @QField(isEditable = false)
   private Integer id;

   @QField(possibleValueSourceName = QueryStat.TABLE_NAME)
   private Integer queryStatId;

   @QField(possibleValueSourceName = QQQTable.TABLE_NAME)
   private Integer tableId;

   @QField(maxLength = 10, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String type;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public QueryStatJoinTable()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public QueryStatJoinTable(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public QueryStatJoinTable withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryStatId
    *******************************************************************************/
   public Integer getQueryStatId()
   {
      return (this.queryStatId);
   }



   /*******************************************************************************
    ** Setter for queryStatId
    *******************************************************************************/
   public void setQueryStatId(Integer queryStatId)
   {
      this.queryStatId = queryStatId;
   }



   /*******************************************************************************
    ** Fluent setter for queryStatId
    *******************************************************************************/
   public QueryStatJoinTable withQueryStatId(Integer queryStatId)
   {
      this.queryStatId = queryStatId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableId
    *******************************************************************************/
   public Integer getTableId()
   {
      return (this.tableId);
   }



   /*******************************************************************************
    ** Setter for tableId
    *******************************************************************************/
   public void setTableId(Integer tableId)
   {
      this.tableId = tableId;
   }



   /*******************************************************************************
    ** Fluent setter for tableId
    *******************************************************************************/
   public QueryStatJoinTable withTableId(Integer tableId)
   {
      this.tableId = tableId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    *******************************************************************************/
   public String getType()
   {
      return (this.type);
   }



   /*******************************************************************************
    ** Setter for type
    *******************************************************************************/
   public void setType(String type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    *******************************************************************************/
   public QueryStatJoinTable withType(String type)
   {
      this.type = type;
      return (this);
   }

}
