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

package com.kingsrook.qqq.backend.core.model.metadata.producers;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** QRecord Entity for TestMetaDataProducingEntity table
 *******************************************************************************/
public class TestMetaDataProducingChildEntity extends QRecordEntity implements MetaDataProducerInterface<QTableMetaData>
{
   public static final String TABLE_NAME = "testMetaDataProducingChildEntity";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(possibleValueSourceName = TestMetaDataProducingEntity.TABLE_NAME)
   private Integer parentId;


   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QTableMetaData produce(QInstance qInstance) throws QException
   {
      return new QTableMetaData()
         .withName(TABLE_NAME)
         .withFieldsFromEntity(TestMetaDataProducingChildEntity.class);
   }



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public TestMetaDataProducingChildEntity()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public TestMetaDataProducingChildEntity(QRecord record)
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
   public TestMetaDataProducingChildEntity withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for parentId
    *******************************************************************************/
   public Integer getParentId()
   {
      return (this.parentId);
   }



   /*******************************************************************************
    ** Setter for parentId
    *******************************************************************************/
   public void setParentId(Integer parentId)
   {
      this.parentId = parentId;
   }



   /*******************************************************************************
    ** Fluent setter for parentId
    *******************************************************************************/
   public TestMetaDataProducingChildEntity withParentId(Integer parentId)
   {
      this.parentId = parentId;
      return (this);
   }


}
