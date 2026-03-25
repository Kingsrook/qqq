/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.middleware.javalin.specs.v1;


import com.kingsrook.qqq.backend.core.model.metadata.authentication.OAuth2AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.middleware.javalin.specs.AbstractEndpointSpec;
import com.kingsrook.qqq.middleware.javalin.specs.SpecTestBase;
import com.kingsrook.qqq.middleware.javalin.specs.v1.responses.AuthenticationMetaDataResponseV1;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for AuthenticationMetaDataSpecV1 
 *******************************************************************************/
class AuthenticationMetaDataSpecV1Test extends SpecTestBase
{


   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected AbstractEndpointSpec<?, ?, ?> getSpec()
   {
      return new AuthenticationMetaDataSpecV1();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected String getVersion()
   {
      return "v1";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      HttpResponse<String> response = Unirest.get(getBaseUrlAndPath() + "/metaData/authentication").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertTrue(jsonObject.has("name"));
      assertTrue(jsonObject.has("type"));
   }



   /*******************************************************************************
    ** Test that when externalBaseUrl is not set, the response uses baseUrl.
    *******************************************************************************/
   @Test
   void testOAuth2Response_noExternalBaseUrl_usesBaseUrl()
   {
      OAuth2AuthenticationMetaData metaData = new OAuth2AuthenticationMetaData()
         .withBaseUrl("http://internal.auth.local/oauth");
      metaData.setName("test");

      AuthenticationMetaDataResponseV1 response = new AuthenticationMetaDataResponseV1();
      response.setAuthenticationMetaData(metaData);

      AuthenticationMetaDataResponseV1.OAuth2Values values = (AuthenticationMetaDataResponseV1.OAuth2Values) response.getValues();
      assertEquals("http://internal.auth.local/oauth", values.getBaseUrl());
   }



   /*******************************************************************************
    ** Test that when externalBaseUrl is set, the response uses it instead of baseUrl.
    *******************************************************************************/
   @Test
   void testOAuth2Response_withExternalBaseUrl_usesExternalBaseUrl()
   {
      OAuth2AuthenticationMetaData metaData = new OAuth2AuthenticationMetaData()
         .withBaseUrl("http://internal.auth.local/oauth")
         .withExternalBaseUrl("https://auth.example.com/oauth");
      metaData.setName("test");

      AuthenticationMetaDataResponseV1 response = new AuthenticationMetaDataResponseV1();
      response.setAuthenticationMetaData(metaData);

      AuthenticationMetaDataResponseV1.OAuth2Values values = (AuthenticationMetaDataResponseV1.OAuth2Values) response.getValues();
      assertEquals("https://auth.example.com/oauth", values.getBaseUrl());
   }

}