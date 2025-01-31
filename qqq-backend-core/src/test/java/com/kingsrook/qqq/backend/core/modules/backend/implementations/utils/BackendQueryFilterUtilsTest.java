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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.utils;


import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.CriteriaOption;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for BackendQueryFilterUtils
 *******************************************************************************/
class BackendQueryFilterUtilsTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesRecordMatch_emptyFilters()
   {
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(null, new QRecord().withValue("a", 1)));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(new QQueryFilter(), new QRecord().withValue("a", 1)));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(new QQueryFilter().withSubFilters(ListBuilder.of(null)), new QRecord().withValue("a", 1)));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(new QQueryFilter().withSubFilters(List.of(new QQueryFilter())), new QRecord().withValue("a", 1)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesRecordMatch_singleAnd()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.AND)
         .withCriteria(new QFilterCriteria("a", QCriteriaOperator.EQUALS, 1));

      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 2)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesRecordMatch_singleOr()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR)
         .withCriteria(new QFilterCriteria("a", QCriteriaOperator.EQUALS, 1));

      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 2)));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testDoesRecordMatch_multipleAnd()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.AND)
         .withCriteria(new QFilterCriteria("a", QCriteriaOperator.EQUALS, 1))
         .withCriteria(new QFilterCriteria("b", QCriteriaOperator.EQUALS, 2));

      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("b", 2)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 2).withValue("b", 2)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("b", 1)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testDoesRecordMatch_multipleOr()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR)
         .withCriteria(new QFilterCriteria("a", QCriteriaOperator.EQUALS, 1))
         .withCriteria(new QFilterCriteria("b", QCriteriaOperator.EQUALS, 2));

      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("b", 2)));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 2).withValue("b", 2)));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("b", 1)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 3).withValue("b", 4)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testDoesRecordMatch_subFilterAnd()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.AND)
         .withSubFilters(List.of(
            new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.AND)
               .withCriteria(new QFilterCriteria("a", QCriteriaOperator.EQUALS, 1)),
            new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.AND)
               .withCriteria(new QFilterCriteria("b", QCriteriaOperator.EQUALS, 2))
         ));

      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("b", 2)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 2).withValue("b", 2)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("b", 1)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testDoesRecordMatch_subFilterOr()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR)
         .withSubFilters(List.of(
            new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR)
               .withCriteria(new QFilterCriteria("a", QCriteriaOperator.EQUALS, 1)),
            new QQueryFilter()
               .withCriteria(new QFilterCriteria("b", QCriteriaOperator.EQUALS, 2))
         ));

      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("b", 2)));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 2).withValue("b", 2)));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("b", 1)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 3).withValue("b", 4)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesRecordMatch_criteriaHasTableNameNoFieldsDo()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.AND)
         .withCriteria(new QFilterCriteria("t.a", QCriteriaOperator.EQUALS, 1));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesRecordMatch_criteriaHasTableNameSomeFieldsDo()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.AND)
         .withCriteria(new QFilterCriteria("t.a", QCriteriaOperator.EQUALS, 1));

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // shouldn't find the "a", because "some" fields in here have a prefix (e.g., 's' was a join table, selected with 't' as the main table, which didn't prefix) //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("a", 1).withValue("s.b", 2)));

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // but this case (contrasted with above) set the record's tableName to "t", so criteria on "t.a" should find field "a" //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withTableName("t").withValue("a", 1).withValue("s.b", 2)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesRecordMatch_criteriaHasTableNameMatchingField()
   {
      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.AND)
         .withCriteria(new QFilterCriteria("t.a", QCriteriaOperator.EQUALS, 1));
      assertTrue(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("t.a", 1)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("t.b", 1)));
      assertFalse(BackendQueryFilterUtils.doesRecordMatch(filter, new QRecord().withValue("s.a", 1)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesCriterionMatch()
   {
      //////////////////////////////////////////////
      // < and > w/ mix of numbers and strings... //
      //////////////////////////////////////////////
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, 1), "f", "2"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, "1"), "f", 2));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, 1), "f", "1"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, "1"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, 1), "f", "0"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, "1"), "f", 0));

      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, 1), "f", "2"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, "1"), "f", 2));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, 1), "f", "1"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, "1"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, 1), "f", "0"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, "1"), "f", 0));

      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, 2), "f", "1"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, "2"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, 1), "f", "1"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, "1"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, 0), "f", "1"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, "0"), "f", 1));

      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, 2), "f", "1"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, "2"), "f", 1));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, 1), "f", "1"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, "1"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, 0), "f", "1"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, "0"), "f", 1));

      /////////////////////////////
      // between and not-between //
      /////////////////////////////
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 0));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 1));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 2));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 3));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 4));

      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 0));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 2));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 3));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 4));

      ////////////////
      // like & not //
      ////////////////
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LIKE, "Test"), "f", "Test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LIKE, "T%"), "f", "Test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LIKE, "T_st"), "f", "Test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_LIKE, "Test"), "f", "Tst"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_LIKE, "T%"), "f", "Rest"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_LIKE, "T_st"), "f", "Toast"));

      //////////////
      // IN & NOT //
      //////////////
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.IN, "A"), "f", "A"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.IN, "A", "B"), "f", "A"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.IN, "A", "B"), "f", "B"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.IN, List.of()), "f", "A"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.IN, ListBuilder.of(null)), "f", "A"));

      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_IN, "A"), "f", "A"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_IN, "A", "B"), "f", "A"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_IN, "A", "B"), "f", "B"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_IN, List.of()), "f", "A"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_IN, ListBuilder.of(null)), "f", "A"));

      ///////////////////////////
      // NOT_EQUALS_OR_IS_NULL //
      ///////////////////////////
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_EQUALS_OR_IS_NULL, "A"), "f", "A"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_EQUALS_OR_IS_NULL, "A"), "f", "B"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_EQUALS_OR_IS_NULL, "A"), "f", null));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private QFilterCriteria newCaseInsensitiveCriteria(String fieldName, QCriteriaOperator operator, Serializable... values)
   {
      return new QFilterCriteria(fieldName, operator, values).withOption(CriteriaOption.CASE_INSENSITIVE);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private QFilterCriteria newCaseInsensitiveCriteria(String fieldName, QCriteriaOperator operator, List<Serializable> values)
   {
      return new QFilterCriteria(fieldName, operator, values).withOption(CriteriaOption.CASE_INSENSITIVE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesCriterionMatchCaseInsensitive()
   {
      ////////////////
      // like & not //
      ////////////////
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.LIKE, "Test"), "f", "test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.LIKE, "test"), "f", "Test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.LIKE, "T%"), "f", "test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.LIKE, "t%"), "f", "Test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.LIKE, "T_st"), "f", "test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.LIKE, "t_st"), "f", "Test"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_LIKE, "Test"), "f", "Tst"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_LIKE, "Test"), "f", "tst"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_LIKE, "T%"), "f", "Rest"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_LIKE, "T_st"), "f", "Toast"));

      //////////////
      // IN & NOT //
      //////////////
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.IN, "A"), "f", "a"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.IN, "a"), "f", "A"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.IN, "A", "B"), "f", "a"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.IN, "A", "b"), "f", "B"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.IN, List.of()), "f", "A"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.IN, ListBuilder.of(null)), "f", "A"));

      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_IN, "A"), "f", "A"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_IN, "A", "B"), "f", "a"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_IN, "A", "b"), "f", "B"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_IN, List.of()), "f", "A"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_IN, ListBuilder.of(null)), "f", "A"));

      ///////////////////////////
      // NOT_EQUALS_OR_IS_NULL //
      ///////////////////////////
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_EQUALS_OR_IS_NULL, "A"), "f", "A"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_EQUALS_OR_IS_NULL, "A"), "f", "a"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_EQUALS_OR_IS_NULL, "A"), "f", "B"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(newCaseInsensitiveCriteria("f", QCriteriaOperator.NOT_EQUALS_OR_IS_NULL, "A"), "f", null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLikeDarPercent()
   {
      String pattern = BackendQueryFilterUtils.sqlLikeToRegex("Dar%");
      assertTrue("Darin".matches(pattern));
      assertTrue("Dar".matches(pattern));
      assertFalse("Not Darin".matches(pattern));
      assertFalse("David".matches(pattern));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLikeDPercentIn()
   {
      String pattern = BackendQueryFilterUtils.sqlLikeToRegex("D%in");
      assertTrue("Darin".matches(pattern));
      assertFalse("Dar".matches(pattern));
      assertFalse("Not Darin".matches(pattern));
      assertTrue("Davin".matches(pattern));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLikeDPercentUnderscoreN()
   {
      String pattern = BackendQueryFilterUtils.sqlLikeToRegex("D%_n");
      assertTrue("Darin".matches(pattern));
      assertTrue("Daron".matches(pattern));
      assertTrue("Dan".matches(pattern));
      assertFalse("Dar".matches(pattern));
      assertFalse("Not Darin".matches(pattern));
      assertTrue("Davin".matches(pattern));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLikeDarUnderscore()
   {
      String pattern = BackendQueryFilterUtils.sqlLikeToRegex("Dar_");
      assertFalse("Darin".matches(pattern));
      assertFalse("Dar".matches(pattern));
      assertTrue("Dart".matches(pattern));
      assertFalse("Not Darin".matches(pattern));
      assertFalse("David".matches(pattern));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testApplyBooleanOperator()
   {
      /////////////////////////////
      // tests for operator: AND //
      /////////////////////////////
      {
         /////////////////////////////////////////////////////////////////////////////////////
         // old value was true; new value is true.                                          //
         // result should be true, and we should not be short-circuited (return value null) //
         /////////////////////////////////////////////////////////////////////////////////////
         AtomicBoolean accumulator = new AtomicBoolean(true);
         assertNull(BackendQueryFilterUtils.applyBooleanOperator(accumulator, true, QQueryFilter.BooleanOperator.AND));
         assertTrue(accumulator.getPlain());
      }
      {
         //////////////////////////////////////////////////////////////////////////////////////
         // old value was true; new value is false.                                          //
         // result should be false, and we should be short-circuited (return value not-null) //
         //////////////////////////////////////////////////////////////////////////////////////
         AtomicBoolean accumulator = new AtomicBoolean(true);
         assertEquals(Boolean.FALSE, BackendQueryFilterUtils.applyBooleanOperator(accumulator, false, QQueryFilter.BooleanOperator.AND));
         assertFalse(accumulator.getPlain());
      }
      {
         //////////////////////////////////////////////////////////////////////////////////////
         // old value was false; new value is true.                                          //
         // result should be false, and we should be short-circuited (return value not-null) //
         //////////////////////////////////////////////////////////////////////////////////////
         AtomicBoolean accumulator = new AtomicBoolean(false);
         assertEquals(Boolean.FALSE, BackendQueryFilterUtils.applyBooleanOperator(accumulator, true, QQueryFilter.BooleanOperator.AND));
         assertFalse(accumulator.getPlain());
      }
      {
         //////////////////////////////////////////////////////////////////////////////////////
         // old value was false; new value is false.                                         //
         // result should be false, and we should be short-circuited (return value not-null) //
         //////////////////////////////////////////////////////////////////////////////////////
         AtomicBoolean accumulator = new AtomicBoolean(false);
         assertEquals(Boolean.FALSE, BackendQueryFilterUtils.applyBooleanOperator(accumulator, false, QQueryFilter.BooleanOperator.AND));
         assertFalse(accumulator.getPlain());
      }

      ////////////////////////////
      // tests for operator: OR //
      ////////////////////////////
      {
         /////////////////////////////////////////////////////////////////////////////////////
         // old value was true; new value is true.                                          //
         // result should be true, and we should be short-circuited (return value not-null) //
         /////////////////////////////////////////////////////////////////////////////////////
         AtomicBoolean accumulator = new AtomicBoolean(true);
         assertEquals(Boolean.TRUE, BackendQueryFilterUtils.applyBooleanOperator(accumulator, true, QQueryFilter.BooleanOperator.OR));
         assertTrue(accumulator.getPlain());
      }
      {
         //////////////////////////////////////////////////////////////////////////////////////
         // old value was true; new value is false.                                          //
         // result should be true, and we should be short-circuited (return value not-null) //
         //////////////////////////////////////////////////////////////////////////////////////
         AtomicBoolean accumulator = new AtomicBoolean(true);
         assertEquals(Boolean.TRUE, BackendQueryFilterUtils.applyBooleanOperator(accumulator, false, QQueryFilter.BooleanOperator.OR));
         assertTrue(accumulator.getPlain());
      }
      {
         //////////////////////////////////////////////////////////////////////////////////////
         // old value was false; new value is true.                                          //
         // result should be false, and we should be short-circuited (return value not-null) //
         //////////////////////////////////////////////////////////////////////////////////////
         AtomicBoolean accumulator = new AtomicBoolean(false);
         assertEquals(Boolean.TRUE, BackendQueryFilterUtils.applyBooleanOperator(accumulator, true, QQueryFilter.BooleanOperator.OR));
         assertTrue(accumulator.getPlain());
      }
      {
         //////////////////////////////////////////////////////////////////////////////////////
         // old value was false; new value is false.                                         //
         // result should be false, and we should not be short-circuited (return value null) //
         //////////////////////////////////////////////////////////////////////////////////////
         AtomicBoolean accumulator = new AtomicBoolean(false);
         assertNull(BackendQueryFilterUtils.applyBooleanOperator(accumulator, false, QQueryFilter.BooleanOperator.OR));
         assertFalse(accumulator.getPlain());
      }
   }

}