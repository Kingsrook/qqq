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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Generic meta-data-producer, which should be instantiated (e.g., by
 ** MetaDataProducer Helper), to produce a QJoinMetaData, based on a
 ** QRecordEntity and a ChildTable sub-annotation.
 **
 ** e.g., Orders & LineItems - on the Order entity
 ** <code>
 @QMetaDataProducingEntity( childTables = { @ChildTable(
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

   private ChildJoin.OrderBy[] orderBys;

   private Class<?> sourceClass;



   /***************************************************************************
    **
    ***************************************************************************/
   public ChildJoinFromRecordEntityGenericMetaDataProducer(String childTableName, String parentTableName, String foreignKeyFieldName, ChildJoin.OrderBy[] orderBys)
   {
      Objects.requireNonNull(childTableName, "childTableName cannot be null");
      Objects.requireNonNull(parentTableName, "parentTableName cannot be null");
      Objects.requireNonNull(foreignKeyFieldName, "foreignKeyFieldName cannot be null");

      this.childTableName = childTableName;
      this.parentTableName = parentTableName;
      this.foreignKeyFieldName = foreignKeyFieldName;
      this.orderBys = orderBys;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QJoinMetaData produce(QInstance qInstance) throws QException
   {
      QTableMetaData parentTable = qInstance.getTable(parentTableName);
      if(parentTable == null)
      {
         throw (new QException("Could not find tableMetaData " + parentTableName));
      }

      QTableMetaData childTable = qInstance.getTable(childTableName);
      if(childTable == null)
      {
         throw (new QException("Could not find tableMetaData " + childTable));
      }

      QJoinMetaData join = new QJoinMetaData()
         .withLeftTable(parentTableName)
         .withRightTable(childTableName)
         .withInferredName()
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn(parentTable.getPrimaryKeyField(), foreignKeyFieldName));

      if(orderBys != null && orderBys.length > 0)
      {
         for(ChildJoin.OrderBy orderBy : orderBys)
         {
            join.withOrderBy(new QFilterOrderBy(orderBy.fieldName(), orderBy.isAscending()));
         }
      }
      else
      {
         //////////////////////////////////////////////////////////
         // by default, sort by the id of the child table... mmm //
         //////////////////////////////////////////////////////////
         join.withOrderBy(new QFilterOrderBy(childTable.getPrimaryKeyField()));
      }

      return (join);
   }



   /*******************************************************************************
    ** Getter for sourceClass
    **
    *******************************************************************************/
   public Class<?> getSourceClass()
   {
      return sourceClass;
   }



   /*******************************************************************************
    ** Setter for sourceClass
    **
    *******************************************************************************/
   public void setSourceClass(Class<?> sourceClass)
   {
      this.sourceClass = sourceClass;
   }



   /*******************************************************************************
    ** Fluent setter for sourceClass
    **
    *******************************************************************************/
   public ChildJoinFromRecordEntityGenericMetaDataProducer withSourceClass(Class<?> sourceClass)
   {
      this.sourceClass = sourceClass;
      return (this);
   }

}
