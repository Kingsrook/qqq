/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.metadata.personalization;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for TableMetaDataPersonalizerAction 
 *******************************************************************************/
class TableMetaDataPersonalizerActionTest extends BaseTest
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach()
   {
      ExamplePersonalizer.reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      ExamplePersonalizer.reset();
      QContext.getQInstance().getSupplementalCustomizers().remove(TableMetaDataPersonalizerInterface.CUSTOMIZER_TYPE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoCustomizer() throws QException
   {
      QContext.getQInstance().getSupplementalCustomizers().remove(TableMetaDataPersonalizerInterface.CUSTOMIZER_TYPE);
      QTableMetaData originalPersonTable = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON);
      QTableMetaData outputTable         = TableMetaDataPersonalizerAction.execute(new AbstractTableActionInput().withTableMetaData(originalPersonTable));
      assertThat(outputTable).isSameAs(originalPersonTable);
   }



   /*******************************************************************************
    * normally instance validation should prevent this, but let's assert what happens
    * just in case
    *******************************************************************************/
   @Test
   void testCustomizer() throws QException
   {
      QContext.getQInstance().getSupplementalCustomizers().remove(TableMetaDataPersonalizerInterface.CUSTOMIZER_TYPE);
      QContext.getQInstance().addSupplementalCustomizer(TableMetaDataPersonalizerInterface.CUSTOMIZER_TYPE, new QCodeReference(getClass()));

      QTableMetaData originalPersonTable = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON);
      assertThatThrownBy(() -> TableMetaDataPersonalizerAction.execute(new AbstractTableActionInput().withTableMetaData(originalPersonTable)))
         .hasMessageContaining("Error initializing tableMetaDataPersonalizer");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUsingExamplePersonalizer() throws QException
   {
      QTableMetaData originalPersonTable = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON);
      QTableMetaData originalShapeTable  = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_SHAPE);
      String         noOfSidesFieldName  = "noOfSides";

      QContext.getQInstance().addSupplementalCustomizer(TableMetaDataPersonalizerInterface.CUSTOMIZER_TYPE, new QCodeReference(ExamplePersonalizer.class));
      ExamplePersonalizer.addCustomizableTable(TestUtils.TABLE_NAME_SHAPE);
      ExamplePersonalizer.addFieldToRemoveForUserId(TestUtils.TABLE_NAME_SHAPE, noOfSidesFieldName, "jdoe");

      ////////////////////////////////////////////////
      // table that the personalizer doesn't handle //
      ////////////////////////////////////////////////
      QContext.getQSession().getUser().setIdReference("bhill");
      QTableMetaData outputPersonTable = TableMetaDataPersonalizerAction.execute(new AbstractTableActionInput().withTableName(TestUtils.TABLE_NAME_PERSON).withInputSource(QInputSource.USER));
      assertThat(outputPersonTable).isSameAs(originalPersonTable);

      ////////////////////////////////////////////////////////////////////////////////////////////////////
      // table that isn't personalized for current user - but still get cloned, just is the same after. //
      ////////////////////////////////////////////////////////////////////////////////////////////////////
      QContext.getQSession().getUser().setIdReference("bhill");
      QTableMetaData outputShapeTable = TableMetaDataPersonalizerAction.execute(new AbstractTableActionInput().withTableName(TestUtils.TABLE_NAME_SHAPE).withInputSource(QInputSource.USER));
      assertThat(outputShapeTable).isNotSameAs(originalShapeTable);
      assertTrue(outputShapeTable.getFields().containsKey(noOfSidesFieldName));

      /////////////////////////////////////////////////
      // table that IS personalized for current user //
      /////////////////////////////////////////////////
      QContext.getQSession().getUser().setIdReference("jdoe");
      outputShapeTable = TableMetaDataPersonalizerAction.execute(new AbstractTableActionInput().withTableName(TestUtils.TABLE_NAME_SHAPE).withInputSource(QInputSource.USER));
      assertThat(outputShapeTable).isNotSameAs(originalShapeTable);
      assertTrue(originalShapeTable.getFields().containsKey(noOfSidesFieldName));
      assertFalse(outputShapeTable.getFields().containsKey(noOfSidesFieldName));
   }

}