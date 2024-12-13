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


import java.util.Objects;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Generic meta-data-producer, which should be instantiated (e.g., by
 ** MetaDataProducer Helper), to produce a QJoinMetaData, based on a
 ** QRecordEntity and a ChildTable sub-annotation.
 **
 ** e.g., Orders & LineItems - on the Order entity
 ** <code>
 @QMetaDataProducingEntity(
    childTables = { @ChildTable(
       childTableEntityClass = LineItem.class,
       childJoin = @ChildJoin(enabled = true),
       childRecordListWidget = @ChildRecordListWidget(enabled = true, label = "Order Lines"))
    }
 )
 public class Order extends QRecordEntity
 ** </code>
 **
 ** A Join will be made:
 ** - left: Order
 ** - right: LineItem
 ** - type:  ONE_TO_MANY (one order (parent table) has mny lines (child table))
 ** - joinOn:  order's primary key, lineItem's orderId field
 ** - name:  inferred, based on the table names orderJoinLineItem)
 *******************************************************************************/
public class ChildJoinFromRecordEntityGenericMetaDataProducer implements MetaDataProducerInterface<QJoinMetaData>
{
   private String childTableName; // e.g., lineItem
   private String parentTableName; // e.g., order
   private String foreignKeyFieldName; // e.g., orderId



   /***************************************************************************
    **
    ***************************************************************************/
   public ChildJoinFromRecordEntityGenericMetaDataProducer(String childTableName, String parentTableName, String foreignKeyFieldName)
   {
      Objects.requireNonNull(childTableName, "childTableName cannot be null");
      Objects.requireNonNull(parentTableName, "parentTableName cannot be null");
      Objects.requireNonNull(foreignKeyFieldName, "foreignKeyFieldName cannot be null");

      this.childTableName = childTableName;
      this.parentTableName = parentTableName;
      this.foreignKeyFieldName = foreignKeyFieldName;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QJoinMetaData produce(QInstance qInstance) throws QException
   {
      QTableMetaData possibleValueTable = qInstance.getTable(parentTableName);
      if(possibleValueTable == null)
      {
         throw (new QException("Could not find tableMetaData " + parentTableName));
      }

      QJoinMetaData join = new QJoinMetaData()
         .withLeftTable(parentTableName)
         .withRightTable(childTableName)
         .withInferredName()
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn(possibleValueTable.getPrimaryKeyField(), foreignKeyFieldName));

      return (join);
   }

}
