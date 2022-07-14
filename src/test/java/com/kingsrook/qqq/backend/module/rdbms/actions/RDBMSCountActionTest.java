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


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.count.CountRequest;
import com.kingsrook.qqq.backend.core.model.actions.count.CountResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSCountActionTest extends RDBMSActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      super.primeTestDatabase();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testUnfilteredCount() throws QException
   {
      CountRequest countRequest = initCountRequest();
      CountResult countResult = new RDBMSCountAction().execute(countRequest);
      Assertions.assertEquals(5, countResult.getCount(), "Unfiltered query should find all rows");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testEqualsQueryCount() throws QException
   {
      String email = "darin.kelkhoff@gmail.com";

      CountRequest countRequest = initCountRequest();
      countRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.EQUALS)
            .withValues(List.of(email)))
      );
      CountResult countResult = new RDBMSCountAction().execute(countRequest);
      Assertions.assertEquals(1, countResult.getCount(), "Expected # of rows");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testNotEqualsQuery() throws QException
   {
      String email = "darin.kelkhoff@gmail.com";

      CountRequest countRequest = initCountRequest();
      countRequest.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("email")
            .withOperator(QCriteriaOperator.NOT_EQUALS)
            .withValues(List.of(email)))
      );
      CountResult countResult = new RDBMSCountAction().execute(countRequest);
      Assertions.assertEquals(4, countResult.getCount(), "Expected # of rows");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private CountRequest initCountRequest()
   {
      CountRequest countRequest = new CountRequest();
      countRequest.setInstance(TestUtils.defineInstance());
      countRequest.setTableName(TestUtils.defineTablePerson().getName());
      return countRequest;
   }

}