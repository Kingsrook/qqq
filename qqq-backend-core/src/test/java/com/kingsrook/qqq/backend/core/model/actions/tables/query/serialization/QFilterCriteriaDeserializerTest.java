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

package com.kingsrook.qqq.backend.core.model.actions.tables.query.serialization;


import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.AbstractFilterExpression;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.Now;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions.NowWithOffset;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator.EQUALS;
import static com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator.GREATER_THAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QFilterCriteriaDeserializer 
 *******************************************************************************/
class QFilterCriteriaDeserializerTest extends BaseTest
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDeserialize() throws IOException
   {
      {
         QFilterCriteria criteria = JsonUtils.toObject("""
            {"fieldName": "id", "operator": "EQUALS", "values": [1]}
            """, QFilterCriteria.class);
         assertEquals("id", criteria.getFieldName());
         assertEquals(EQUALS, criteria.getOperator());
         assertEquals(List.of(1), criteria.getValues());
      }

      {
         QFilterCriteria criteria = JsonUtils.toObject("""
            {"fieldName": "createDate", "operator": "GREATER_THAN", "expression":
               {"type": "NowWithOffset", "operator": "PLUS", "amount": 5, "timeUnit": "MINUTES"}
            }
            """, QFilterCriteria.class);
         assertEquals("createDate", criteria.getFieldName());
         assertEquals(GREATER_THAN, criteria.getOperator());
         assertNull(criteria.getValues());
         AbstractFilterExpression<?> expression = criteria.getExpression();
         assertThat(expression).isInstanceOf(NowWithOffset.class);
         NowWithOffset nowWithOffset = (NowWithOffset) expression;
         assertEquals(5, nowWithOffset.getAmount());
         assertEquals(NowWithOffset.Operator.PLUS, nowWithOffset.getOperator());
         assertEquals(ChronoUnit.MINUTES, nowWithOffset.getTimeUnit());
      }

      {
         QFilterCriteria criteria = JsonUtils.toObject("""
            {"fieldName": "orderDate", "operator": "EQUALS", "expression": {"type": "Now"} }
            """, QFilterCriteria.class);
         assertEquals("orderDate", criteria.getFieldName());
         assertEquals(EQUALS, criteria.getOperator());
         assertNull(criteria.getValues());
         AbstractFilterExpression<?> expression = criteria.getExpression();
         assertThat(expression).isInstanceOf(Now.class);
      }

   }
}