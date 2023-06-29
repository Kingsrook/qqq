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

package com.kingsrook.qqq.backend.module.api.model.metadata;


import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.module.api.BaseTest;
import com.kingsrook.qqq.backend.module.api.model.AuthorizationType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for APIBackendMetaData
 *******************************************************************************/
class APIBackendMetaDataTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMissingBaseUrl()
   {
      APIBackendMetaData apiBackendMetaData = new APIBackendMetaData()
         .withName("test");
      QInstanceValidator qInstanceValidator = new QInstanceValidator();
      apiBackendMetaData.performValidation(qInstanceValidator);
      assertEquals(1, qInstanceValidator.getErrors().size());
      assertThat(qInstanceValidator.getErrors()).anyMatch(e -> e.contains("Missing baseUrl"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAuthorizationApiKeyQueryParam()
   {
      APIBackendMetaData apiBackendMetaData = new APIBackendMetaData()
         .withAuthorizationType(AuthorizationType.API_KEY_QUERY_PARAM)
         .withBaseUrl("http://localhost:8000/")
         .withName("test");
      QInstanceValidator qInstanceValidator = new QInstanceValidator();
      apiBackendMetaData.performValidation(qInstanceValidator);
      assertEquals(2, qInstanceValidator.getErrors().size());
      assertThat(qInstanceValidator.getErrors()).anyMatch(e -> e.contains("Missing apiKey for API backend"));
      assertThat(qInstanceValidator.getErrors()).anyMatch(e -> e.contains("Missing apiKeyQueryParamName for API backend"));

      apiBackendMetaData = new APIBackendMetaData()
         .withAuthorizationType(AuthorizationType.API_KEY_QUERY_PARAM)
         .withApiKey("ABC-123")
         .withApiKeyQueryParamName("key")
         .withBaseUrl("http://localhost:8000/")
         .withName("test");
      qInstanceValidator = new QInstanceValidator();
      apiBackendMetaData.performValidation(qInstanceValidator);
      assertEquals(0, qInstanceValidator.getErrors().size());

      apiBackendMetaData = new APIBackendMetaData()
         .withAuthorizationType(AuthorizationType.API_KEY_HEADER)
         .withApiKey("ABC-123")
         .withApiKeyQueryParamName("key")
         .withBaseUrl("http://localhost:8000/")
         .withName("test");
      qInstanceValidator = new QInstanceValidator();
      apiBackendMetaData.performValidation(qInstanceValidator);
      assertEquals(1, qInstanceValidator.getErrors().size());
      assertThat(qInstanceValidator.getErrors()).anyMatch(e -> e.contains("Unexpected apiKeyQueryParamName for API backend"));

   }

}