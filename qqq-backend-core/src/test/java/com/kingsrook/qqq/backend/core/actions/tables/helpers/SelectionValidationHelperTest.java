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
import java.util.Set;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QVirtualFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.StringLengthFunction;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for SelectionValidationHelper
 *******************************************************************************/
class SelectionValidationHelperTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRegularFieldIsSelectable() throws Exception
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);

      List<String> unrecognized = SelectionValidationHelper.getUnrecognizedFieldNames(queryInput, Set.of("firstName", "lastName"));
      assertTrue(unrecognized.isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testVirtualFieldWithQuerySelectableIsAccepted() throws Exception
   {
      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      table.withVirtualField(new QVirtualFieldMetaData("firstNameLength", QFieldType.INTEGER)
         .withIsQuerySelectable(true)
         .withFieldFunction(new FieldFunction()
            .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
            .withFieldName("firstName")));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);

      List<String> unrecognized = SelectionValidationHelper.getUnrecognizedFieldNames(queryInput, Set.of("firstName", "firstNameLength"));
      assertTrue(unrecognized.isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testVirtualFieldWithoutQuerySelectableIsRejected() throws Exception
   {
      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      table.withVirtualField(new QVirtualFieldMetaData("firstNameLength", QFieldType.INTEGER)
         .withIsQuerySelectable(false)
         .withFieldFunction(new FieldFunction()
            .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
            .withFieldName("firstName")));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);

      List<String> unrecognized = SelectionValidationHelper.getUnrecognizedFieldNames(queryInput, Set.of("firstNameLength"));
      assertEquals(1, unrecognized.size());
      assertEquals("firstNameLength", unrecognized.get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUnknownFieldIsRejected() throws Exception
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);

      List<String> unrecognized = SelectionValidationHelper.getUnrecognizedFieldNames(queryInput, Set.of("noSuchField"));
      assertEquals(1, unrecognized.size());
      assertEquals("noSuchField", unrecognized.get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testJoinTableVirtualFieldWithQuerySelectableIsAccepted() throws Exception
   {
      QTableMetaData lineItemTable = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_LINE_ITEM);
      lineItemTable.withVirtualField(new QVirtualFieldMetaData("skuLength", QFieldType.INTEGER)
         .withIsQuerySelectable(true)
         .withFieldFunction(new FieldFunction()
            .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
            .withFieldName("sku")));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      queryInput.setQueryJoins(List.of(new QueryJoin().withJoinTable(TestUtils.TABLE_NAME_LINE_ITEM).withSelect(true)));

      List<String> unrecognized = SelectionValidationHelper.getUnrecognizedFieldNames(queryInput, Set.of(TestUtils.TABLE_NAME_LINE_ITEM + ".skuLength"));
      assertTrue(unrecognized.isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testJoinTableVirtualFieldWithoutQuerySelectableIsRejected() throws Exception
   {
      QTableMetaData lineItemTable = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_LINE_ITEM);
      lineItemTable.withVirtualField(new QVirtualFieldMetaData("skuLength", QFieldType.INTEGER)
         .withIsQuerySelectable(false)
         .withFieldFunction(new FieldFunction()
            .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
            .withFieldName("sku")));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      queryInput.setQueryJoins(List.of(new QueryJoin().withJoinTable(TestUtils.TABLE_NAME_LINE_ITEM).withSelect(true)));

      List<String> unrecognized = SelectionValidationHelper.getUnrecognizedFieldNames(queryInput, Set.of(TestUtils.TABLE_NAME_LINE_ITEM + ".skuLength"));
      assertEquals(1, unrecognized.size());
      assertEquals(TestUtils.TABLE_NAME_LINE_ITEM + ".skuLength", unrecognized.get(0));
   }

}
