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

package com.kingsrook.qqq.api;


import java.util.List;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.Auth0AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestUtils
{
   public static final String MEMORY_BACKEND_NAME = "memory";
   public static final String TABLE_NAME_PERSON   = "person";

   public static final String V2023_Q1 = "2023.Q1";
   public static final String V2022_Q4 = "2022.Q4";
   public static final String V2023_Q2 = "2023.Q2";

   public static final String CURRENT_API_VERSION = V2023_Q1;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();

      qInstance.addBackend(defineMemoryBackend());
      qInstance.addTable(defineTablePerson());
      qInstance.setAuthentication(new Auth0AuthenticationMetaData().withType(QAuthenticationType.FULLY_ANONYMOUS).withName("anonymous"));

      qInstance.withMiddlewareMetaData(new ApiInstanceMetaData()
         .withName("TestAPI")
         .withDescription("QQQ Test API")
         .withContactEmail("contact@kingsrook.com")
         .withCurrentVersion(new APIVersion(CURRENT_API_VERSION))
         .withSupportedVersions(List.of(new APIVersion(V2022_Q4), new APIVersion(V2023_Q1)))
         .withPastVersions(List.of(new APIVersion(V2022_Q4)))
         .withFutureVersions(List.of(new APIVersion(V2023_Q2)))
      );

      return (qInstance);
   }



   /*******************************************************************************
    ** Define the in-memory backend used in standard tests
    *******************************************************************************/
   public static QBackendMetaData defineMemoryBackend()
   {
      return new QBackendMetaData()
         .withName(MEMORY_BACKEND_NAME)
         .withBackendType(MemoryBackendModule.class);
   }



   /*******************************************************************************
    ** Define the 'person' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_PERSON)
         .withLabel("Person")
         .withBackendName(MEMORY_BACKEND_NAME)
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withInitialVersion(V2022_Q4)

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // in 2022.Q4, this table had a "shoeCount" field. but for the 2023.Q1 version, we renamed it to noOfShoes! //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            .withRemovedApiField(new QFieldMetaData("shoeCount", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS)
               .withMiddlewareMetaData(new ApiFieldMetaData().withFinalVersion(V2022_Q4).withReplacedByFieldName("noOfShoes")))

         )
         .withPrimaryKeyField("id")
         .withUniqueKey(new UniqueKey("email"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE)
            .withMiddlewareMetaData(new ApiFieldMetaData().withApiFieldName("birthDay"))
         )
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         // .withField(new QFieldMetaData("homeStateId", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_STATE))
         // .withField(new QFieldMetaData("favoriteShapeId", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_SHAPE))
         // .withField(new QFieldMetaData("customValue", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_CUSTOM))
         .withField(new QFieldMetaData("noOfShoes", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS)
            .withMiddlewareMetaData(new ApiFieldMetaData().withInitialVersion(V2023_Q1)))

         /////////////////////////////////////////////////////////////////
         // 2 new fields - they'll appear in future versions of the API //
         /////////////////////////////////////////////////////////////////
         .withField(new QFieldMetaData("cost", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY)
            .withMiddlewareMetaData(new ApiFieldMetaData().withInitialVersion(V2023_Q2)))
         .withField(new QFieldMetaData("price", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY)
            .withMiddlewareMetaData(new ApiFieldMetaData().withIsExcluded(true)))
         ;
   }

}
