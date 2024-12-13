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
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** QRecord Entity for TestMetaDataProducingEntity table
 *******************************************************************************/
@QMetaDataProducingEntity(producePossibleValueSource = true,
   childTables =
      {
         @ChildTable(childTableEntityClass = TestMetaDataProducingChildEntity.class,
            childJoin = @ChildJoin(enabled = true),
            childRecordListWidget = @ChildRecordListWidget(enabled = true, label = "Test Children", maxRows = 15))
      }
)
public class TestMetaDataProducingEntity extends QRecordEntity implements MetaDataProducerInterface<QTableMetaData>
{
   public static final String TABLE_NAME = "testMetaDataProducingEntity";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QTableMetaData produce(QInstance qInstance) throws QException
   {
      return new QTableMetaData()
         .withName(TABLE_NAME)
         .withFieldsFromEntity(TestMetaDataProducingEntity.class);
   }



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public TestMetaDataProducingEntity()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public TestMetaDataProducingEntity(QRecord record)
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
   public TestMetaDataProducingEntity withId(Integer id)
   {
      this.id = id;
      return (this);
   }

}
