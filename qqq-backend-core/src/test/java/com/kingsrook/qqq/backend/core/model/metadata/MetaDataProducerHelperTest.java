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

package com.kingsrook.qqq.backend.core.model.metadata;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestAbstractMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestDisabledMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestImplementsMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestMetaDataProducingChildEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestMetaDataProducingPossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestNoInterfacesExtendsObject;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestNoValidConstructorMetaDataProducer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for MetaDataProducerHelper
 *******************************************************************************/
class MetaDataProducerHelperTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = new QInstance();
      MetaDataProducerHelper.processAllMetaDataProducersInPackage(qInstance, "com.kingsrook.qqq.backend.core.model.metadata.producers");
      assertTrue(qInstance.getTables().containsKey(TestMetaDataProducer.NAME));
      assertTrue(qInstance.getTables().containsKey(TestImplementsMetaDataProducer.NAME));
      assertFalse(qInstance.getTables().containsKey(TestNoValidConstructorMetaDataProducer.NAME));
      assertFalse(qInstance.getTables().containsKey(TestNoInterfacesExtendsObject.NAME));
      assertFalse(qInstance.getTables().containsKey(TestAbstractMetaDataProducer.NAME));
      assertFalse(qInstance.getTables().containsKey(TestDisabledMetaDataProducer.NAME));

      /////////////////////////////////////////////
      // annotation on PVS enum -> PVS meta data //
      /////////////////////////////////////////////
      assertTrue(qInstance.getPossibleValueSources().containsKey(TestMetaDataProducingPossibleValueEnum.class.getSimpleName()));
      QPossibleValueSource enumPVS = qInstance.getPossibleValueSource(TestMetaDataProducingPossibleValueEnum.class.getSimpleName());
      assertEquals(QPossibleValueSourceType.ENUM, enumPVS.getType());
      assertEquals(2, enumPVS.getEnumValues().size());
      assertEquals(new QPossibleValue<>(1, "One"), enumPVS.getEnumValues().get(0));

      //////////////////////////////////////////////
      // annotation on PVS table -> PVS meta data //
      //////////////////////////////////////////////
      assertTrue(qInstance.getPossibleValueSources().containsKey(TestMetaDataProducingEntity.TABLE_NAME));
      QPossibleValueSource tablePVS = qInstance.getPossibleValueSource(TestMetaDataProducingEntity.TABLE_NAME);
      assertEquals(QPossibleValueSourceType.TABLE, tablePVS.getType());
      assertEquals(TestMetaDataProducingEntity.TABLE_NAME, tablePVS.getTableName());

      //////////////////////////////////////////////////////////////////
      // annotation on parent table w/ joined child -> join meta data //
      //////////////////////////////////////////////////////////////////
      String joinName = QJoinMetaData.makeInferredJoinName(TestMetaDataProducingEntity.TABLE_NAME, TestMetaDataProducingChildEntity.TABLE_NAME);
      assertTrue(qInstance.getJoins().containsKey(joinName));
      QJoinMetaData join = qInstance.getJoin(joinName);
      assertEquals(TestMetaDataProducingEntity.TABLE_NAME, join.getLeftTable());
      assertEquals(TestMetaDataProducingChildEntity.TABLE_NAME, join.getRightTable());
      assertEquals(JoinType.ONE_TO_MANY, join.getType());
      assertEquals("id", join.getJoinOns().get(0).getLeftField());
      assertEquals("parentId", join.getJoinOns().get(0).getRightField());

      //////////////////////////////////////////////////////////////////////////////////////
      // annotation on parent table w/ joined child -> child record list widget meta data //
      //////////////////////////////////////////////////////////////////////////////////////
      assertTrue(qInstance.getWidgets().containsKey(joinName));
      QWidgetMetaDataInterface widget = qInstance.getWidget(joinName);
      assertEquals(WidgetType.CHILD_RECORD_LIST.getType(), widget.getType());
      assertEquals("Test Children", widget.getLabel());
      assertEquals(joinName, widget.getDefaultValues().get("joinName"));
      assertEquals(false, widget.getDefaultValues().get("canAddChildRecord"));
      assertNull(widget.getDefaultValues().get("manageAssociationName"));
      assertEquals(15, widget.getDefaultValues().get("maxRows"));

   }

}