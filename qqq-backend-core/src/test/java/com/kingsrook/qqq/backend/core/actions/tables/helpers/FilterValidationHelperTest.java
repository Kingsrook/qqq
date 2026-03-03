/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.tables.helpers;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QVirtualFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.StringLengthFunction;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit test for FilterValidationHelper
 *******************************************************************************/
class FilterValidationHelperTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRegularFieldInFilterIsAccepted() throws Exception
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, "Darin")));

      assertDoesNotThrow(() -> FilterValidationHelper.validateFieldNamesInFilter(queryInput));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testVirtualFieldWithQueryCriteriaAllowedInFilter() throws Exception
   {
      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      table.withVirtualField(new QVirtualFieldMetaData("firstNameLength", QFieldType.INTEGER)
         .withIsQueryCriteria(true)
         .withFieldFunction(new FieldFunction()
            .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
            .withFieldName("firstName")));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria("firstNameLength", QCriteriaOperator.EQUALS, List.of(5))));

      assertDoesNotThrow(() -> FilterValidationHelper.validateFieldNamesInFilter(queryInput));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testVirtualFieldWithoutQueryCriteriaRejectedInFilter() throws Exception
   {
      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      table.withVirtualField(new QVirtualFieldMetaData("firstNameLength", QFieldType.INTEGER)
         .withIsQueryCriteria(false)
         .withFieldFunction(new FieldFunction()
            .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
            .withFieldName("firstName")));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria("firstNameLength", QCriteriaOperator.EQUALS, List.of(5))));

      assertThrows(QUserFacingException.class, () -> FilterValidationHelper.validateFieldNamesInFilter(queryInput));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUnknownFieldInFilterIsRejected()
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria("noSuchField", QCriteriaOperator.EQUALS, "x")));

      assertThrows(QUserFacingException.class, () -> FilterValidationHelper.validateFieldNamesInFilter(queryInput));
   }

}
