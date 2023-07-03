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
 ** QRecord Entity for QueryStatOrderByField table
 *******************************************************************************/
public class QueryStatOrderByField extends QRecordEntity
{
   public static final String TABLE_NAME = "queryStatOrderByField";

   @QField(isEditable = false)
   private Integer id;

   @QField(possibleValueSourceName = QueryStat.TABLE_NAME)
   private Integer queryStatId;

   @QField(label = "Table", possibleValueSourceName = QQQTable.TABLE_NAME)
   private Integer qqqTableId;

   @QField(maxLength = 50, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String name;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public QueryStatOrderByField()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public QueryStatOrderByField(QRecord record)
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
   public QueryStatOrderByField withId(Integer id)
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
   public QueryStatOrderByField withQueryStatId(Integer queryStatId)
   {
      this.queryStatId = queryStatId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for qqqTableId
    *******************************************************************************/
   public Integer getQqqTableId()
   {
      return (this.qqqTableId);
   }



   /*******************************************************************************
    ** Setter for qqqTableId
    *******************************************************************************/
   public void setQqqTableId(Integer qqqTableId)
   {
      this.qqqTableId = qqqTableId;
   }



   /*******************************************************************************
    ** Fluent setter for qqqTableId
    *******************************************************************************/
   public QueryStatOrderByField withQqqTableId(Integer qqqTableId)
   {
      this.qqqTableId = qqqTableId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public QueryStatOrderByField withName(String name)
   {
      this.name = name;
      return (this);
   }

}
