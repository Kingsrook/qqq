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

package com.kingsrook.qqq.api.actions;


import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.kingsrook.qqq.api.BaseTest;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.RemovedApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for GetTableApiFieldsAction
 *******************************************************************************/
class GetTableApiFieldsActionTest extends BaseTest
{
   private final String TABLE_NAME = "testTable";

   Function<List<? extends QFieldMetaData>, Set<String>> fieldListToNameSet = l -> l.stream().map(f -> f.getName()).collect(Collectors.toSet());



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<? extends QFieldMetaData> getFields(String tableName, String version) throws QException
   {
      return new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(version)).getFields();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAdditions() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      qInstance.addTable(new QTableMetaData()
         .withName(TABLE_NAME)
         .withMiddlewareMetaData(new ApiTableMetaData().withInitialVersion("1"))
         .withField(new QFieldMetaData("a", STRING)) // inherit versionRange from the table
         .withField(new QFieldMetaData("b", STRING).withMiddlewareMetaData(new ApiFieldMetaData().withInitialVersion("1")))
         .withField(new QFieldMetaData("c", STRING).withMiddlewareMetaData(new ApiFieldMetaData().withInitialVersion("2")))
         .withField(new QFieldMetaData("d", STRING).withMiddlewareMetaData(new ApiFieldMetaData().withInitialVersion("3")))
      );
      new QInstanceEnricher(qInstance).enrich();

      assertEquals(Set.of("a", "b"), fieldListToNameSet.apply(getFields(TABLE_NAME, "1")));
      assertEquals(Set.of("a", "b", "c"), fieldListToNameSet.apply(getFields(TABLE_NAME, "2")));
      assertEquals(Set.of("a", "b", "c", "d"), fieldListToNameSet.apply(getFields(TABLE_NAME, "3")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRemoval() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      qInstance.addTable(new QTableMetaData()
         .withName(TABLE_NAME)
         .withMiddlewareMetaData(new ApiTableMetaData().withInitialVersion("1")
            .withRemovedApiField(((RemovedApiFieldMetaData) new RemovedApiFieldMetaData("c", STRING)
               .withMiddlewareMetaData(new ApiFieldMetaData().withInitialVersion("1")))
               .withFinalVersion("2"))
         )
         .withField(new QFieldMetaData("a", STRING)) // inherit versionRange from the table
         .withField(new QFieldMetaData("b", STRING).withMiddlewareMetaData(new ApiFieldMetaData().withInitialVersion("1")))
         // we used to have "c" here... now it's in the removed list above!
         .withField(new QFieldMetaData("d", STRING).withMiddlewareMetaData(new ApiFieldMetaData().withInitialVersion("3")))
      );
      new QInstanceEnricher(qInstance).enrich();

      assertEquals(Set.of("a", "b", "c"), fieldListToNameSet.apply(getFields(TABLE_NAME, "1")));
      assertEquals(Set.of("a", "b", "c"), fieldListToNameSet.apply(getFields(TABLE_NAME, "2")));
      assertEquals(Set.of("a", "b", "d"), fieldListToNameSet.apply(getFields(TABLE_NAME, "3")));
   }

}