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


import java.util.ArrayList;
import java.util.List;
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
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.AfterEach;
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
   private static final String DISABLE_NAME_TIEBREAKER_PROPERTY = "qqq.MetaDataProducerHelper.disableNameTiebreaker";



   /***************************************************************************
    *
    ***************************************************************************/
   @AfterEach
   void afterEach()
   {
      System.clearProperty(DISABLE_NAME_TIEBREAKER_PROPERTY);
   }



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

      ////////////////////////////////////////////
      // annotation on table -> table meta data //
      ////////////////////////////////////////////
      assertTrue(qInstance.getTables().containsKey(TestMetaDataProducingEntity.TABLE_NAME));
      QTableMetaData table = qInstance.getTables().get(TestMetaDataProducingEntity.TABLE_NAME);
      assertEquals(TestMetaDataProducingEntity.TABLE_NAME, table.getName());
      assertEquals("id", table.getPrimaryKeyField());
      assertEquals(2, table.getFields().size());
      assertTrue(table.getField("name").getIsRequired());
      assertEquals("Customized Label", table.getLabel());

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



   /*******************************************************************************
    ** Test that producers with the same sortOrder and type are sorted by class
    ** simple name (alphabetically), then by full class name, for deterministic ordering.
    *******************************************************************************/
   @Test
   void testSortByClassSimpleNameThenFullName()
   {
      ///////////////////////////////////////////////////////////////////////////
      // create producers with same sortOrder - they should sort by class name //
      ///////////////////////////////////////////////////////////////////////////
      MetaDataProducerInterface<?> producerZ = new TestProducerZebra();
      MetaDataProducerInterface<?> producerA = new TestProducerAlpha();
      MetaDataProducerInterface<?> producerM = new TestProducerMango();

      ///////////////////////////////////////////////////////
      // put them in an "unsorted" order and sort the list //
      ///////////////////////////////////////////////////////
      List<MetaDataProducerInterface<?>> producers = new ArrayList<>(List.of(producerZ, producerA, producerM));
      MetaDataProducerHelper.sortMetaDataProducers(producers);

      //////////////////////////////////////////////////////////////
      // verify they are now sorted alphabetically by simple name //
      //////////////////////////////////////////////////////////////
      assertEquals("TestProducerAlpha", producers.get(0).getClass().getSimpleName());
      assertEquals("TestProducerMango", producers.get(1).getClass().getSimpleName());
      assertEquals("TestProducerZebra", producers.get(2).getClass().getSimpleName());
   }



   /*******************************************************************************
    ** Test that the system property disables the class name tiebreaker, restoring
    ** the previous (undefined) behavior.
    *******************************************************************************/
   @Test
   void testDisableNameTiebreakerSystemProperty()
   {
      System.setProperty(DISABLE_NAME_TIEBREAKER_PROPERTY, "true");

      MetaDataProducerInterface<?> producerZ = new TestProducerZebra();
      MetaDataProducerInterface<?> producerA = new TestProducerAlpha();

      /////////////////////////////////////////////////////////////////////////////
      // with tiebreaker disabled, order should be based on insertion order      //
      // (since sortOrder and type are equal, and no further comparator applied) //
      /////////////////////////////////////////////////////////////////////////////
      List<MetaDataProducerInterface<?>> producers = new ArrayList<>(List.of(producerZ, producerA));
      MetaDataProducerHelper.sortMetaDataProducers(producers);

      //////////////////////////////////////////////////////////////////////////////
      // the order should remain as inserted (Z, A) since there's no tie-breaker. //
      // note: this relies on stable sort behavior in Java                        //
      //////////////////////////////////////////////////////////////////////////////
      assertEquals("TestProducerZebra", producers.get(0).getClass().getSimpleName());
      assertEquals("TestProducerAlpha", producers.get(1).getClass().getSimpleName());
   }



   /*******************************************************************************
    ** Test that sortOrder still takes precedence over class name.
    *******************************************************************************/
   @Test
   void testSortOrderTakesPrecedenceOverClassName()
   {
      ///////////////////////////////////////////////////////////////////////////
      // producerZ has lower sortOrder, so should come first despite "Z" > "A" //
      ///////////////////////////////////////////////////////////////////////////
      MetaDataProducerInterface<?> producerZ = new TestProducerZebra()
      {
         @Override
         public int getSortOrder()
         {
            return 100;
         }
      };
      MetaDataProducerInterface<?> producerA = new TestProducerAlpha()
      {
         @Override
         public int getSortOrder()
         {
            return 200;
         }
      };

      List<MetaDataProducerInterface<?>> producers = new ArrayList<>(List.of(producerA, producerZ));
      MetaDataProducerHelper.sortMetaDataProducers(producers);

      //////////////////////////////////////////////////////////////
      // Z should come first because it has lower sortOrder (100) //
      //////////////////////////////////////////////////////////////
      assertEquals(100, producers.get(0).getSortOrder());
      assertEquals(200, producers.get(1).getSortOrder());
   }



   /*******************************************************************************
    ** Test that when simple names are equal, full class name is used as tiebreaker.
    ** This tests the scenario mentioned in the review: com.foo.MyProducer vs com.bar.MyProducer
    *******************************************************************************/
   @Test
   void testFullClassNameTiebreakerWhenSimpleNamesMatch()
   {
      /////////////////////////////////////////////////////////////////////////////////
      // create two producers from different inner classes that have the same simple //
      // name pattern (anonymous classes extending the same base)                    //
      // We'll use the outer class structure to create predictable full names        //
      /////////////////////////////////////////////////////////////////////////////////
      MetaDataProducerInterface<?> producerFromAlpha = new TestProducerAlpha() {};
      MetaDataProducerInterface<?> producerFromZebra = new TestProducerZebra() {};

      //////////////////////////////////////////////////////////////////////////////
      // both are anonymous classes, so their simple names will be empty strings. //
      // the full name will include the outer class and a number suffix.          //
      // this exercises the full-name tiebreaker when simple names are equal.     //
      //////////////////////////////////////////////////////////////////////////////
      assertEquals(producerFromAlpha.getClass().getSimpleName(), producerFromZebra.getClass().getSimpleName());

      List<MetaDataProducerInterface<?>> producers = new ArrayList<>(List.of(producerFromZebra, producerFromAlpha));
      MetaDataProducerHelper.sortMetaDataProducers(producers);

      /////////////////////////////////////////////////////////////////////////////////////
      // after sorting, they should be in a deterministic order based on full class name //
      // the key assertion is that sorting is stable and deterministic                   //
      /////////////////////////////////////////////////////////////////////////////////////
      String firstName  = producers.get(0).getClass().getName();
      String secondName = producers.get(1).getClass().getName();
      assertTrue(firstName.compareTo(secondName) <= 0,
         "Expected first producer's full name [" + firstName + "] to sort before or equal to second [" + secondName + "]");
   }



   /***************************************************************************
    * Test producer class - named to sort first alphabetically
    ***************************************************************************/
   private static class TestProducerAlpha implements MetaDataProducerInterface<QTableMetaData>
   {
      @Override
      public QTableMetaData produce(QInstance qInstance)
      {
         return new QTableMetaData().withName("alpha");
      }
   }



   /***************************************************************************
    * Test producer class - named to sort in the middle alphabetically
    ***************************************************************************/
   private static class TestProducerMango implements MetaDataProducerInterface<QTableMetaData>
   {
      @Override
      public QTableMetaData produce(QInstance qInstance)
      {
         return new QTableMetaData().withName("mango");
      }
   }



   /***************************************************************************
    * Test producer class - named to sort last alphabetically
    ***************************************************************************/
   private static class TestProducerZebra implements MetaDataProducerInterface<QTableMetaData>
   {
      @Override
      public QTableMetaData produce(QInstance qInstance)
      {
         return new QTableMetaData().withName("zebra");
      }
   }

}