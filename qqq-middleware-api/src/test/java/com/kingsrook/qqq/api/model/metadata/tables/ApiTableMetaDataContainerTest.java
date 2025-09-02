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

package com.kingsrook.qqq.api.model.metadata.tables;


import java.util.Set;
import java.util.function.BiConsumer;
import com.kingsrook.qqq.api.BaseTest;
import com.kingsrook.qqq.api.model.metadata.ApiOperation;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for ApiTableMetaDataContainer 
 *******************************************************************************/
class ApiTableMetaDataContainerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testClone()
   {
      BiConsumer<QTableMetaData, String> setStrings = (table, s) ->
      {
         ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.ofOrWithNew(table);
         apiTableMetaDataContainer.withApis(MapBuilder.of(s, new ApiTableMetaData()
            .withInitialVersion(s)
            .withFinalVersion(s)
            .withEnabledOperations(Set.of(ApiOperation.DELETE))
            .withDisabledOperations(Set.of(ApiOperation.GET))
            .withApiAssociationMetaData(MapBuilder.of(s, new ApiAssociationMetaData().withInitialVersion(s)))
            .withRemovedApiFields(ListBuilder.of(new QFieldMetaData(s, QFieldType.INTEGER)))
         ));
      };

      BiConsumer<QTableMetaData, String> assertStrings = (table, s) ->
      {
         ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.of(table);
         ApiTableMetaData          apiTableMetaData          = apiTableMetaDataContainer.getApis().get(s);
         assertEquals(s, apiTableMetaData.getInitialVersion());
         assertEquals(s, apiTableMetaData.getFinalVersion());
         assertEquals(Set.of(ApiOperation.DELETE), apiTableMetaData.getEnabledOperations());
         assertEquals(Set.of(ApiOperation.GET), apiTableMetaData.getDisabledOperations());
         assertEquals(s, apiTableMetaData.getApiAssociationMetaData().get(s).getInitialVersion());
         assertEquals(s, apiTableMetaData.getRemovedApiFields().get(0).getName());
      };

      QTableMetaData tableMetaData = new QTableMetaData();
      setStrings.accept(tableMetaData, "a");
      assertStrings.accept(tableMetaData, "a");

      QTableMetaData clone = tableMetaData.clone();
      assertStrings.accept(tableMetaData, "a");
      assertStrings.accept(clone, "a");

      setStrings.accept(clone, "z");
      assertStrings.accept(tableMetaData, "a");
      assertStrings.accept(clone, "z");
   }

}