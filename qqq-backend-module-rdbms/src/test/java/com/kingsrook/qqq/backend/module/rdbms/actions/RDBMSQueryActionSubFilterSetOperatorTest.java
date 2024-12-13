 /*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.rdbms.actions;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** test for subfilter set
 *******************************************************************************/
public class RDBMSQueryActionSubFilterSetOperatorTest extends RDBMSActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      super.primeTestDatabase();

      // AbstractRDBMSAction.setLogSQL(true, true, "system.out");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      AbstractRDBMSAction.setLogSQL(false);
      QContext.getQSession().removeValue(QSession.VALUE_KEY_USER_TIMEZONE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QueryInput initQueryRequest()
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      return queryInput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testUnion() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withSubFilterSetOperator(QQueryFilter.SubFilterSetOperator.UNION)
         .withSubFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 1, 2)))
         .withSubFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 2, 3)))
         .withOrderBy(new QFilterOrderBy("id", false))
      );

      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(3, queryOutput.getRecords().size(), "Expected # of rows");
      assertEquals(3, queryOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals(2, queryOutput.getRecords().get(1).getValueInteger("id"));
      assertEquals(1, queryOutput.getRecords().get(2).getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testUnionAll() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withSubFilterSetOperator(QQueryFilter.SubFilterSetOperator.UNION_ALL)
         .withSubFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 1, 2)))
         .withSubFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 2, 3)))
         .withOrderBy(new QFilterOrderBy("id", false))
      );

      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(4, queryOutput.getRecords().size(), "Expected # of rows");
      assertEquals(3, queryOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals(2, queryOutput.getRecords().get(1).getValueInteger("id"));
      assertEquals(2, queryOutput.getRecords().get(2).getValueInteger("id"));
      assertEquals(1, queryOutput.getRecords().get(3).getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testIntersect() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withSubFilterSetOperator(QQueryFilter.SubFilterSetOperator.INTERSECT)
         .withSubFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 1, 2)))
         .withSubFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 2, 3)))
         .withOrderBy(new QFilterOrderBy("id", false))
      );

      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size(), "Expected # of rows");
      assertEquals(2, queryOutput.getRecords().get(0).getValueInteger("id"));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testExcept() throws QException
   {
      QueryInput queryInput = initQueryRequest();
      queryInput.setFilter(new QQueryFilter()
         .withSubFilterSetOperator(QQueryFilter.SubFilterSetOperator.EXCEPT)
         .withSubFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 1, 2, 3)))
         .withSubFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 2)))
         .withOrderBy(new QFilterOrderBy("id", true))
      );

      QueryOutput queryOutput = new RDBMSQueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size(), "Expected # of rows");
      assertEquals(1, queryOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals(3, queryOutput.getRecords().get(1).getValueInteger("id"));
   }

}
